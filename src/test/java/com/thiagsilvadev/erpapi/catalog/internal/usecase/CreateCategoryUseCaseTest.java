package com.thiagsilvadev.erpapi.catalog.internal.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thiagsilvadev.erpapi.catalog.internal.usecase.category.CreateCategoryUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.thiagsilvadev.erpapi.catalog.internal.dto.category.CreateCategoryRequest;
import com.thiagsilvadev.erpapi.catalog.internal.exception.CategoryCodeAlreadyExistsException;
import com.thiagsilvadev.erpapi.catalog.internal.mapper.CatalogMapper;
import com.thiagsilvadev.erpapi.catalog.internal.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CreateCategoryUseCaseTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CatalogMapper catalogMapper;

    @InjectMocks
    private CreateCategoryUseCase useCase;

    @Test
    void shouldRejectDuplicatedCategoryCode() {
        CreateCategoryRequest request = new CreateCategoryRequest(" raw ", "Raw materials", null);
        when(categoryRepository.existsByCode("RAW")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(CategoryCodeAlreadyExistsException.class);

        verify(categoryRepository, never()).saveAndFlush(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldTranslateDuplicateCategoryCodeRace() {
        CreateCategoryRequest request = new CreateCategoryRequest(" raw ", "Raw materials", null);
        when(categoryRepository.existsByCode("RAW")).thenReturn(false);
        when(categoryRepository.saveAndFlush(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new DataIntegrityViolationException("uk_catalog_category_code"));

        assertThatThrownBy(() -> useCase.execute(request)).isInstanceOf(CategoryCodeAlreadyExistsException.class);
    }
}
