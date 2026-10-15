package com.vyoog.eisplatform.modules.registration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 09.01 Order Management (sprint 2027.1.1): an organization member's request
 * to subscribe their organization to a product, requiring an ORG_ADMIN
 * decision (MANAGE_ORDERS) before it takes effect. Deliberately
 * organization-only — an individual customer already gets instant self-serve
 * activation via {@code SubscriptionService#subscribe}, which an approval
 * step would only add friction to; see OrderService's own javadoc.
 */
@Entity
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "requested_by_customer_id", nullable = false)
    private Long requestedByCustomerId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    /** Null means the product's flat price — same meaning as
     * {@code ProductSubscription#planId}. */
    @Column(name = "plan_id")
    private Long planId;

    /** C100 CA-1 B: every product of the order; the order's own product and plan repeat the first. */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private java.util.List<OrderItem> items = new java.util.ArrayList<>();

    /** C100 CA-2: why the member asks for the purchase; shown to the administrator who decides. */
    @Column(name = "request_reason", length = 500)
    private String requestReason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.SUBMITTED;

    @Column(name = "decided_by_customer_id")
    private Long decidedByCustomerId;

    private Instant decidedAt;

    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    /** C103 / REQ-TEN-010: when the 3-day pending reminder was sent to the organization's approvers (once; null = not yet due or already decided). */
    @Column(name = "reminded_at")
    private Instant remindedAt;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
