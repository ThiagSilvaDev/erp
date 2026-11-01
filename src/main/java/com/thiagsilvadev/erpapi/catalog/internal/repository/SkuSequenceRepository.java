package com.thiagsilvadev.erpapi.catalog.internal.repository;

import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.thiagsilvadev.erpapi.catalog.internal.model.SkuSequence;

public interface SkuSequenceRepository extends JpaRepository<SkuSequence, UUID> {
    @Modifying
    @Query(
            value =
                    "insert into catalog_sku_sequences (company_id, current_value) values (:companyId, 0) on conflict (company_id) do nothing",
            nativeQuery = true)
    void initialize(@Param("companyId") UUID companyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SkuSequence s where s.companyId = :companyId")
    SkuSequence lockByCompanyId(@Param("companyId") UUID companyId);
}
