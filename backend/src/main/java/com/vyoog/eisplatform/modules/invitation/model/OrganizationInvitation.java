package com.vyoog.eisplatform.modules.invitation.model;

import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** One invitation of one email to one organization (REQ-TEN-008). Only the hash of the link token is stored. */
@Entity
@Table(name = "organization_invitation")
@Getter
@Setter
public class OrganizationInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "normalized_email", nullable = false, length = 255)
    private String normalizedEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "org_role", nullable = false, length = 20)
    private OrgRole orgRole;

    @Column(name = "org_node_id")
    private Long orgNodeId;

    @Column(name = "token_hash", nullable = false, length = 64, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InvitationStatus status = InvitationStatus.PENDING;

    @Column(name = "invited_by_customer_id", nullable = false)
    private Long invitedByCustomerId;

    @Column(name = "accepted_by_customer_id")
    private Long acceptedByCustomerId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "declined_at")
    private Instant declinedAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "last_sent_at", nullable = false)
    private Instant lastSentAt = Instant.now();

    @Column(name = "send_count", nullable = false)
    private int sendCount = 1;
}
