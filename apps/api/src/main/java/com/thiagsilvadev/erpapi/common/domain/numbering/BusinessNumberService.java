package com.thiagsilvadev.erpapi.common.domain.numbering;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BusinessNumberService {

    private final BusinessNumberSequenceRepository sequenceRepository;

    public BusinessNumberService(BusinessNumberSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    @Transactional
    public Long next(BusinessNumberType type) {
        BusinessNumberSequence sequence = sequenceRepository
                .findWithLockingByType(type)
                .orElseGet(() -> sequenceRepository.saveAndFlush(BusinessNumberSequence.create(type)));
        return sequence.next();
    }
}
