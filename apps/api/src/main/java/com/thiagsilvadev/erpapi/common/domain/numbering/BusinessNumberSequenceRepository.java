package com.thiagsilvadev.erpapi.common.domain.numbering;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BusinessNumberSequenceRepository extends JpaRepository<BusinessNumberSequence, BusinessNumberType> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sequence from BusinessNumberSequence sequence where sequence.type = :type")
    Optional<BusinessNumberSequence> findWithLockingByType(@Param("type") BusinessNumberType type);
}
