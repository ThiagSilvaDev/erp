package com.thiagsilvadev.erpapi.iam.accesscontrol.internal.listener;

import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import com.thiagsilvadev.erpapi.company.api.CompanyCreatedEvent;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service.RoleTemplateService;
import com.thiagsilvadev.erpapi.iam.accesscontrol.internal.service.TenantMembershipService;

@Component
public class CompanyAuthorizationListener {

    private final RoleTemplateService roleTemplateService;
    private final TenantMembershipService membershipService;

    public CompanyAuthorizationListener(
            RoleTemplateService roleTemplateService, TenantMembershipService membershipService) {
        this.roleTemplateService = roleTemplateService;
        this.membershipService = membershipService;
    }

    @ApplicationModuleListener
    public void on(CompanyCreatedEvent event) {
        roleTemplateService.ensureDefaults(event.companyId());
        if (event.createdByUserId() != null) {
            membershipService.provisionBuiltIn(event.createdByUserId(), event.companyId(), RoleTemplateService.OWNER);
        }
    }
}
