package com.vyoog.eisplatform.modules.registration.dto;

import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;

import java.time.Instant;

public record OrgMemberDto(
    Long organizationMemberId,
    Long customerId,
    String firstName,
    String lastName,
    String email,
    OrgRole orgRole,
    MembershipStatus status,
    Instant lastReviewedAt
) {
}
