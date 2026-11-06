package com.thiagsilvadev.erpapi.common.domain.numbering;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BusinessNumberSequenceInitializer implements ApplicationRunner {

    private final BusinessNumberSequenceRepository sequenceRepository;

    public BusinessNumberSequenceInitializer(BusinessNumberSequenceRepository sequenceRepository) {
        this.sequenceRepository = sequenceRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (BusinessNumberType type : BusinessNumberType.values()) {
            if (!sequenceRepository.existsById(type)) {
                sequenceRepository.save(BusinessNumberSequence.create(type));
            }
        }
    }
}
