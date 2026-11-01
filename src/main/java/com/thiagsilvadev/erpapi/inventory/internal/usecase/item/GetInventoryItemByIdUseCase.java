package com.thiagsilvadev.erpapi.inventory.internal.usecase.item;

import static com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions.READ;

import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemResponse;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryItemRepository;
import com.thiagsilvadev.erpapi.inventory.internal.service.InventoryItemResponseAssembler;

@Component
public class GetInventoryItemByIdUseCase {

    private final InventoryItemRepository itemRepository;
    private final InventoryItemResponseAssembler assembler;

    public GetInventoryItemByIdUseCase(
            InventoryItemRepository itemRepository, InventoryItemResponseAssembler assembler) {
        this.itemRepository = itemRepository;
        this.assembler = assembler;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public InventoryItemResponse execute(UUID id) {
        var companyId = TenantContext.requireTenantId();
        return assembler.toResponse(itemRepository
                .findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException(InventoryItem.class, id)));
    }
}
