package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.permission.ModulePermissionProvider;
import com.thiagsilvadev.erpapi.common.security.AuditActorResolver;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.IamPermissions;
import com.thiagsilvadev.erpapi.iam.accesscontrol.api.TenantRoleCreatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.RolePermission;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.model.TenantRole;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.RolePermissionRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.repository.TenantRoleRepository;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.security.PermissionCatalog;

@Service
public class RoleTemplateService {

    public static final String OWNER = ModulePermissionProvider.ROLE_OWNER;
    public static final String ADMIN = ModulePermissionProvider.ROLE_ADMIN;
    public static final String MANAGER = ModulePermissionProvider.ROLE_MANAGER;
    public static final String OPERATOR = ModulePermissionProvider.ROLE_OPERATOR;
    public static final String DRIVER = ModulePermissionProvider.ROLE_DRIVER;

    private final TenantRoleRepository roleRepository;
    private final RolePermissionRepository permissionRepository;
    private final PermissionCatalog permissionCatalog;
    private final List<ModulePermissionProvider> permissionProviders;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditActorResolver actorResolver;

    public RoleTemplateService(
            TenantRoleRepository roleRepository,
            RolePermissionRepository permissionRepository,
            PermissionCatalog permissionCatalog,
            List<ModulePermissionProvider> permissionProviders,
            ApplicationEventPublisher eventPublisher,
            AuditActorResolver actorResolver) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.permissionCatalog = permissionCatalog;
        this.permissionProviders = permissionProviders;
        this.eventPublisher = eventPublisher;
        this.actorResolver = actorResolver;
    }

    @Transactional
    public void ensureDefaults(UUID companyId) {
        Map<String, Set<String>> templates = new LinkedHashMap<>();
        templates.put(OWNER, permissionCatalog.tenantPermissions());
        templates.put(
                ADMIN,
                permissionCatalog.tenantPermissions().stream()
                        .filter(permission -> !permission.equals(IamPermissions.OWNERSHIP_TRANSFER))
                        .collect(Collectors.toUnmodifiableSet()));
        Map<String, Set<String>> moduleMappings = new HashMap<>();
        for (ModulePermissionProvider provider : permissionProviders) {
            provider.defaultRoleMappings().forEach((roleCode, permissions) -> moduleMappings
                    .computeIfAbsent(roleCode, ignored -> new HashSet<>())
                    .addAll(permissions));
        }
        for (String roleCode : List.of(MANAGER, OPERATOR, DRIVER)) {
            Set<String> permissions = moduleMappings.get(roleCode);
            if (permissions != null) {
                templates.put(roleCode, permissions);
            }
        }

        templates.forEach((code, permissions) -> roleRepository
                .findByCompanyIdAndCode(companyId, code)
                .orElseGet(() -> createTemplate(companyId, code, permissions)));
    }

    private TenantRole createTemplate(UUID companyId, String code, Set<String> permissions) {
        TenantRole role = roleRepository.saveAndFlush(TenantRole.builtIn(
                companyId,
                code,
                displayName(code),
                "Built-in " + displayName(code).toLowerCase() + " role"));
        permissionRepository.saveAll(permissions.stream()
                .map(key -> RolePermission.create(role.getId(), key))
                .toList());
        eventPublisher.publishEvent(new TenantRoleCreatedEvent(
                companyId,
                role.getId(),
                role.getCode(),
                permissions,
                actorResolver.userId().orElse(null)));
        return role;
    }

    private static String displayName(String code) {
        return code.charAt(0) + code.substring(1).toLowerCase(Locale.ROOT);
    }
}
