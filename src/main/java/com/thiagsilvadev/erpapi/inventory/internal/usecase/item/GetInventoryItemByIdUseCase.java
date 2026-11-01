package com.thiagsilvadev.erpapi.inventory.internal.usecase.item;

import static com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions.READ;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemResponse;
import com.thiagsilvadev.erpapi.inventory.internal.mapper.InventoryItemMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryItemRepository;

@Component
public class GetInventoryItemByIdUseCase {

    private final InventoryItemRepository itemRepository;
    private final InventoryItemMapper mapper;

    public GetInventoryItemByIdUseCase(InventoryItemRepository itemRepository, InventoryItemMapper mapper) {
        this.itemRepository = itemRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public InventoryItemResponse execute(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return mapper.toResponse(itemRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(InventoryItem.class, id)));
    }
}
