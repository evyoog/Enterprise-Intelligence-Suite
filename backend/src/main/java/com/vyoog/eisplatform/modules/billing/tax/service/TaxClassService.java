package com.vyoog.eisplatform.modules.billing.tax.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.billing.tax.dto.AssignTaxClassRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.ProductTaxClassDto;
import com.vyoog.eisplatform.modules.billing.tax.dto.SaveTaxClassRequest;
import com.vyoog.eisplatform.modules.billing.tax.dto.TaxClassDto;
import com.vyoog.eisplatform.modules.billing.tax.model.TaxClass;
import com.vyoog.eisplatform.modules.billing.tax.repository.TaxClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/** REQ-BIL-002.19: the tax classes (for example a SAC code) and which class each product has. */
@Service
@RequiredArgsConstructor
public class TaxClassService {

    private final TaxClassRepository classRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<TaxClassDto> list() {
        Map<String, Long> counts = productRepository.findAll().stream().filter(p -> p.getTaxClassCode() != null)
            .collect(Collectors.groupingBy(Product::getTaxClassCode, Collectors.counting()));
        return classRepository.findAll().stream().sorted((a, b) -> a.getCode().compareToIgnoreCase(b.getCode()))
            .map(c -> toDto(c, counts.getOrDefault(c.getCode(), 0L))).toList();
    }

    @Transactional
    public TaxClassDto create(String actorSub, SaveTaxClassRequest r) {
        String code = r.code().trim().toUpperCase(Locale.ROOT);
        if (classRepository.existsByCode(code)) {
            throw new DuplicateResourceException("A tax class with code " + code + " already exists.");
        }
        TaxClass c = new TaxClass();
        c.setCode(code);
        apply(c, r);
        c = classRepository.save(c);
        audit(actorSub, "TAX_CLASS_CREATED", c, "Tax class " + code + " created");
        return toDto(c, 0);
    }

    @Transactional
    public TaxClassDto update(String actorSub, Long id, SaveTaxClassRequest r) {
        TaxClass c = classRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Tax class not found"));
        apply(c, r);   // the code never changes: products and rules point at it
        TaxClass saved = classRepository.save(c);
        audit(actorSub, "TAX_CLASS_UPDATED", saved, "Tax class " + saved.getCode() + " updated");
        return toDto(saved, productRepository.findAll().stream().filter(p -> saved.getCode().equals(p.getTaxClassCode())).count());
    }

    @Transactional(readOnly = true)
    public List<ProductTaxClassDto> products() {
        return productRepository.findAll().stream().sorted((a, b) -> a.getName().compareToIgnoreCase(b.getName()))
            .map(p -> new ProductTaxClassDto(p.getId(), p.getName(), p.getStatus().name(), p.getCategory(), p.getTaxClassCode())).toList();
    }

    @Transactional
    public ProductTaxClassDto assign(String actorSub, Long productId, AssignTaxClassRequest r) {
        Product p = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        String code = r.taxClassCode() == null || r.taxClassCode().isBlank() ? null : r.taxClassCode().trim().toUpperCase(Locale.ROOT);
        if (code != null) {
            TaxClass c = classRepository.findByCode(code).orElseThrow(() -> new IllegalArgumentException("Unknown tax class: " + code));
            if (!c.isEnabled()) {
                throw new IllegalArgumentException("Tax class " + code + " is disabled.");
            }
        }
        String before = p.getTaxClassCode();
        p.setTaxClassCode(code);
        productRepository.save(p);
        auditService.recordSuccess("PRODUCT_TAX_CLASS_CHANGED", actorSub, null, null, "Product", String.valueOf(p.getId()), null,
            "Tax class of " + p.getName() + " changed from " + (before == null ? "none" : before) + " to " + (code == null ? "none" : code));
        return new ProductTaxClassDto(p.getId(), p.getName(), p.getStatus().name(), p.getCategory(), p.getTaxClassCode());
    }

    /** The class row for a product, or null. Used when an invoice line is created. */
    @Transactional(readOnly = true)
    public TaxClass classOf(Product product) {
        return product.getTaxClassCode() == null ? null : classRepository.findByCode(product.getTaxClassCode()).orElse(null);
    }

    private void apply(TaxClass c, SaveTaxClassRequest r) {
        c.setName(r.name().trim());
        c.setHsnSacCode(r.hsnSacCode() == null || r.hsnSacCode().isBlank() ? null : r.hsnSacCode().trim());
        c.setDescription(r.description() == null || r.description().isBlank() ? null : r.description().trim());
        c.setEnabled(r.enabled() == null || r.enabled());
        c.setUpdatedAt(Instant.now());
    }

    private void audit(String actorSub, String action, TaxClass c, String detail) {
        auditService.recordSuccess(action, actorSub, null, null, "TaxClass", String.valueOf(c.getId()), null, detail);
    }

    private TaxClassDto toDto(TaxClass c, long productCount) {
        return new TaxClassDto(c.getId(), c.getCode(), c.getName(), c.getHsnSacCode(), c.getDescription(), c.isEnabled(), productCount);
    }
}
