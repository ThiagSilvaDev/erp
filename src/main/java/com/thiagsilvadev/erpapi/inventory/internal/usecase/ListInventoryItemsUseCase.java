package com.thiagsilvadev.erpapi.inventory.internal.usecase;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.InventoryItemResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryItemRepository;

@Component
public class ListInventoryItemsUseCase {

    private final InventoryItemRepository itemRepository;
    private final InventoryMapper mapper;

    public ListInventoryItemsUseCase(InventoryItemRepository itemRepository, InventoryMapper mapper) {
        this.itemRepository = itemRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<InventoryItemResponse> execute(Pageable pageable) {
        return itemRepository.findAll(pageable).map(mapper::toResponse);
    }
}
