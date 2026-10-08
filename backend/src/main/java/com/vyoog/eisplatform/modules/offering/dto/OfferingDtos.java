package com.vyoog.eisplatform.modules.offering.dto;

import java.time.Instant;
import java.util.List;

/** REQ-CAT-005 payloads. Prices are not part of an offering. */
public final class OfferingDtos {

    private OfferingDtos() {
    }

    public record OfferingRequest(String name, String description, String status, List<Long> productIds) {
    }

    public record ProductRef(Long id, String name, String status, String imageUrl) {
    }

    public record OfferingDto(Long id, String name, String description, String status, List<ProductRef> products,
                              Instant createdAt, Instant updatedAt) {
    }

    public record ProductRuleDto(Long productId, String productName, String productStatus, String audience,
                                 List<Long> worksWithProductIds) {
    }

    public record ProductRuleRequest(String audience, List<Long> worksWithProductIds) {
    }

    public record WorksWithDto(Long id, String name) {
    }
}
