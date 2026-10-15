package com.vyoog.eisplatform.modules.registration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * C103 / REQ-TEN-010: a member asks for their own access to a product the organization already subscribes to, when they
 * do not have it. Separate from {@link Order} (which asks the organization to buy a subscription) and from
 * {@link OrganizationProductAccess} (which is the access itself, granted directly by an administrator or by this
 * request once approved). One pending request per member and product (DB unique index).
 */
@Entity
@Table(name = "access_request")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class AccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_member_id", nullable = false)
    private Long organizationMemberId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccessRequestStatus status = AccessRequestStatus.PENDING;

    @Column(length = 500)
    private String message;

    @Column(name = "decided_by_customer_id")
    private Long decidedByCustomerId;

    @Column(name = "decided_at")
    private Instant decidedAt;

    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
