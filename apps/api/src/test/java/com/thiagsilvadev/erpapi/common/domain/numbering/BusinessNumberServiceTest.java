package com.thiagsilvadev.erpapi.common.domain.numbering;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessNumberServiceTest {

    @Mock
    private BusinessNumberSequenceRepository sequenceRepository;

    @Test
    void shouldIncrementExistingSequenceAndReturnNewValue() {
        BusinessNumberSequence sequence = BusinessNumberSequence.create(BusinessNumberType.SALES_ORDER);
        when(sequenceRepository.findWithLockingByType(BusinessNumberType.SALES_ORDER))
                .thenReturn(Optional.of(sequence));

        BusinessNumberService service = new BusinessNumberService(sequenceRepository);

        Long nextValue = service.next(BusinessNumberType.SALES_ORDER);

        assertThat(nextValue).isEqualTo(1L);
        assertThat(sequence.getCurrentValue()).isEqualTo(1L);
    }

    @Test
    void shouldCreateMissingSequenceWithCurrentValueZeroBeforeIncrementing() {
        BusinessNumberSequence sequence = BusinessNumberSequence.create(BusinessNumberType.INVOICE);
        when(sequenceRepository.findWithLockingByType(BusinessNumberType.INVOICE))
                .thenReturn(Optional.empty());
        when(sequenceRepository.saveAndFlush(org.mockito.ArgumentMatchers.any(BusinessNumberSequence.class)))
                .thenReturn(sequence);

        BusinessNumberService service = new BusinessNumberService(sequenceRepository);

        Long nextValue = service.next(BusinessNumberType.INVOICE);

        assertThat(nextValue).isEqualTo(1L);
        assertThat(sequence.getCurrentValue()).isEqualTo(1L);
        verify(sequenceRepository).saveAndFlush(org.mockito.ArgumentMatchers.any(BusinessNumberSequence.class));
    }
}
