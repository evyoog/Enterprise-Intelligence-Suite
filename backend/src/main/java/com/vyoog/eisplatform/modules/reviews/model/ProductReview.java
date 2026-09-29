package com.vyoog.eisplatform.modules.reviews.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 03.04.01 Reviews & Ratings (sprint 2027.1.3). At most one review per
 * (product, customer) — submitting again edits the existing one rather than
 * creating a second (enforced by a unique index — see schema.sql). "Submit
 * review" and "Rate product" are the same action here: one record carries
 * both a star rating and optional free-text comment.
 */
@Entity
@Table(name = "product_review")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 2000)
    private String comment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewStatus status = ReviewStatus.PENDING;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
