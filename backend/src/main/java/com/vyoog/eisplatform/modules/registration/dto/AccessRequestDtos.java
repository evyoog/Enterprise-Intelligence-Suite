package com.vyoog.eisplatform.modules.registration.dto;

import java.time.Instant;

/** C103 / REQ-TEN-010: a member's request for their own access to a product the organization already subscribes to. */
public final class AccessRequestDtos {

    private AccessRequestDtos() {
    }

    public record AccessRequestDto(Long id, Long productId, String productName, Long memberId, String memberName,
                                   String status, String message, Instant createdAt, String decidedByName, Instant decidedAt,
                                   String decisionNote) {
    }

    public record CreateAccessRequest(Long productId, String message) {
    }

    public record DecideAccessRequest(String note) {
    }
}
