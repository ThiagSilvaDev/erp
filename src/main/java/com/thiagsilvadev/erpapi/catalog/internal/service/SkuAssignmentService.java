package com.thiagsilvadev.erpapi.catalog.internal.service;

import org.springframework.stereotype.Service;

import com.thiagsilvadev.erpapi.catalog.internal.exception.SkuAlreadyExistsException;
import com.thiagsilvadev.erpapi.catalog.internal.model.Sku;
import com.thiagsilvadev.erpapi.catalog.internal.model.SkuSequence;
import com.thiagsilvadev.erpapi.catalog.internal.repository.ProductRepository;
import com.thiagsilvadev.erpapi.catalog.internal.repository.SkuSequenceRepository;

@Service
public class SkuAssignmentService {
    private final SkuSequenceRepository sequences;
    private final ProductRepository products;
    private final SkuGenerationPolicy policy;

    public SkuAssignmentService(
            SkuSequenceRepository sequences, ProductRepository products, SkuGenerationPolicy policy) {
        this.sequences = sequences;
        this.products = products;
        this.policy = policy;
    }
    // The caller owns the transaction and holds the lock through product persistence.
    public Sku assign(String suppliedSku, SkuGenerationContext context) {
        Sku manual = suppliedSku == null ? null : Sku.of(suppliedSku);
        sequences.initialize(context.companyId());
        SkuSequence sequence = sequences.lockByCompanyId(context.companyId());
        if (manual != null) {
            if (products.existsByCompanyIdAndSkuValue(context.companyId(), manual.getValue())) {
                throw new SkuAlreadyExistsException(manual.getValue());
            }
            return manual;
        }
        Sku candidate;
        do {
            candidate = policy.generate(context, sequence.next());
        } while (products.existsByCompanyIdAndSkuValue(context.companyId(), candidate.getValue()));
        return candidate;
    }
}
