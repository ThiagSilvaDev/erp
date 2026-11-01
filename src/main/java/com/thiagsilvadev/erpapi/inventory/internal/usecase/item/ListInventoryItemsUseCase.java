package com.thiagsilvadev.erpapi.inventory.internal.usecase.item;

import static com.thiagsilvadev.erpapi.inventory.api.InventoryPermissions.READ;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.tenant.TenantContext;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemMapper;
import com.thiagsilvadev.erpapi.inventory.internal.dto.item.InventoryItemResponse;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryItemRepository;

@Component
public class ListInventoryItemsUseCase {

    private final InventoryItemRepository itemRepository;
    private final InventoryItemMapper mapper;

    public ListInventoryItemsUseCase(InventoryItemRepository itemRepository, InventoryItemMapper mapper) {
        this.itemRepository = itemRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + READ + "')")
    public Page<InventoryItemResponse> execute(Pageable pageable) {
        var companyId = TenantContext.requireTenantId();
        return itemRepository.findAllByCompanyId(companyId, pageable).map(mapper::toResponse);
    }
}
