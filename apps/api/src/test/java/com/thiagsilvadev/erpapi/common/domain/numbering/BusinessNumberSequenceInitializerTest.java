package com.thiagsilvadev.erpapi.common.domain.numbering;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessNumberSequenceInitializerTest {

    @Mock
    private BusinessNumberSequenceRepository sequenceRepository;

    @Test
    void shouldCreateMissingSequencesWithoutResettingExistingOnes() {
        when(sequenceRepository.existsById(BusinessNumberType.COMPANY)).thenReturn(true);

        BusinessNumberSequenceInitializer initializer = new BusinessNumberSequenceInitializer(sequenceRepository);

        initializer.run(null);

        verify(sequenceRepository, never()).save(BusinessNumberSequence.create(BusinessNumberType.COMPANY));
        verify(sequenceRepository)
                .save(org.mockito.ArgumentMatchers.argThat(
                        sequence -> sequence.getType() == BusinessNumberType.SALES_ORDER
                                && sequence.getCurrentValue().equals(0L)));
    }
}
