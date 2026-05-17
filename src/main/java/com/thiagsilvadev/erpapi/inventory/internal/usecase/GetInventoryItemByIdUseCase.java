package com.thiagsilvadev.erpapi.inventory.internal.usecase;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryDtos.InventoryItemResponse;
import com.thiagsilvadev.erpapi.inventory.internal.dto.InventoryMapper;
import com.thiagsilvadev.erpapi.inventory.internal.model.InventoryItem;
import com.thiagsilvadev.erpapi.inventory.internal.repository.InventoryItemRepository;

@Component
public class GetInventoryItemByIdUseCase {

    private final InventoryItemRepository itemRepository;
    private final InventoryMapper mapper;

    public GetInventoryItemByIdUseCase(InventoryItemRepository itemRepository, InventoryMapper mapper) {
        this.itemRepository = itemRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse execute(UUID id) {
        return mapper.toResponse(
                itemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(InventoryItem.class, id)));
    }
}
