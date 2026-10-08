package com.vyoog.eisplatform.modules.offering.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.offering.dto.OfferingDtos.*;
import com.vyoog.eisplatform.modules.offering.model.Offering;
import com.vyoog.eisplatform.modules.offering.model.OfferingStatus;
import com.vyoog.eisplatform.modules.offering.repository.OfferingRepository;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.product.service.ProductUsageGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REQ-CAT-005 Offering management: the platform administrator groups catalog products into offerings;
 * customers browse the ACTIVE ones. An offering carries no price; each product keeps the plans and
 * prices the administrator set on it.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OfferingService implements ProductUsageGuard {

    private final OfferingRepository offeringRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<OfferingDto> adminList() {
        Map<Long, Product> products = productsById();
        return offeringRepository.findAllByOrderByNameAsc().stream().map(o -> toDto(o, products, false)).toList();
    }

    @Transactional(readOnly = true)
    public OfferingDto adminGet(Long id) {
        return toDto(require(id), productsById(), false);
    }

    public OfferingDto create(String actorSub, String actorEmail, OfferingRequest request) {
        Offering o = new Offering();
        apply(o, request, null);
        o = offeringRepository.save(o);
        audit("OFFERING_CREATED", actorSub, actorEmail, o, "Created offering " + o.getName());
        return toDto(o, productsById(), false);
    }

    public OfferingDto update(String actorSub, String actorEmail, Long id, OfferingRequest request) {
        Offering o = require(id);
        apply(o, request, id);
        o.setUpdatedAt(Instant.now());
        o = offeringRepository.save(o);
        audit("OFFERING_UPDATED", actorSub, actorEmail, o, "Updated offering " + o.getName() + " (" + o.getStatus() + ")");
        return toDto(o, productsById(), false);
    }

    /** BR-OFR-003: only a draft is deleted; an offering that was sold or shown is retired instead. */
    public void delete(String actorSub, String actorEmail, Long id) {
        Offering o = require(id);
        if (o.getStatus() != OfferingStatus.DRAFT) {
            throw new InvalidStateException("Only a draft offering can be deleted. Retire it instead.");
        }
        offeringRepository.delete(o);
        audit("OFFERING_DELETED", actorSub, actorEmail, o, "Deleted offering " + o.getName());
    }

    @Transactional(readOnly = true)
    public List<OfferingDto> publicList() {
        Map<Long, Product> products = productsById();
        return offeringRepository.findByStatusOrderByNameAsc(OfferingStatus.ACTIVE).stream()
            .map(o -> toDto(o, products, true)).filter(d -> !d.products().isEmpty()).toList();
    }

    @Transactional(readOnly = true)
    public OfferingDto publicGet(Long id) {
        Offering o = offeringRepository.findById(id).filter(x -> x.getStatus() == OfferingStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Offering not found"));
        OfferingDto dto = toDto(o, productsById(), true);
        if (dto.products().isEmpty()) {
            throw new ResourceNotFoundException("Offering not found");
        }
        return dto;
    }

    /** A product that is part of an offering cannot be hard-deleted (BR-OFR-005). */
    @Override
    @Transactional(readOnly = true)
    public Optional<String> blockingReason(Long productId) {
        return offeringRepository.findAll().stream().anyMatch(o -> o.getProductIds().contains(productId))
            ? Optional.of("an offering") : Optional.empty();
    }

    private void apply(Offering o, OfferingRequest request, Long selfId) {
        String name = request.name() == null ? "" : request.name().trim();
        if (name.isEmpty() || name.length() > 150) {
            throw new IllegalArgumentException("Name is required (up to 150 characters).");
        }
        boolean taken = offeringRepository.findAll().stream()
            .anyMatch(x -> x.getName().equalsIgnoreCase(name) && !x.getId().equals(selfId));
        if (taken) {
            throw new DuplicateResourceException("An offering named " + name + " already exists.");
        }
        String description = request.description() == null || request.description().isBlank() ? null : request.description().trim();
        if (description != null && description.length() > 1000) {
            throw new IllegalArgumentException("Description is limited to 1000 characters.");
        }
        List<Long> ids = request.productIds() == null ? List.of() : request.productIds().stream().distinct().toList();
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("An offering needs at least one product.");
        }
        Map<Long, Product> found = productRepository.findAllById(ids).stream().collect(Collectors.toMap(Product::getId, p -> p));
        if (found.size() != ids.size()) {
            throw new IllegalArgumentException("One of the products does not exist.");
        }
        OfferingStatus status;
        try {
            status = request.status() == null ? OfferingStatus.DRAFT : OfferingStatus.valueOf(request.status().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status must be DRAFT, ACTIVE or RETIRED.");
        }
        if (status == OfferingStatus.ACTIVE && found.values().stream().noneMatch(p -> p.getStatus() == ProductStatus.ACTIVE)) {
            throw new InvalidStateException("An offering can be published only when at least one of its products is active.");
        }
        o.setName(name);
        o.setDescription(description);
        o.setStatus(status);
        o.setProductIds(new ArrayList<>(ids));
    }

    private Offering require(Long id) {
        return offeringRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Offering not found"));
    }

    private Map<Long, Product> productsById() {
        return productRepository.findAll().stream().collect(Collectors.toMap(Product::getId, p -> p));
    }

    private OfferingDto toDto(Offering o, Map<Long, Product> products, boolean activeOnly) {
        List<ProductRef> refs = o.getProductIds().stream().map(products::get).filter(Objects::nonNull)
            .filter(p -> !activeOnly || p.getStatus() == ProductStatus.ACTIVE)
            .map(p -> new ProductRef(p.getId(), p.getName(), p.getStatus().name(), p.getImageUrl())).toList();
        return new OfferingDto(o.getId(), o.getName(), o.getDescription(), o.getStatus().name(), refs, o.getCreatedAt(), o.getUpdatedAt());
    }

    private void audit(String action, String sub, String email, Offering o, String detail) {
        auditService.recordSuccess(action, sub, null, email, "Offering", String.valueOf(o.getId()), null, detail);
    }
}
