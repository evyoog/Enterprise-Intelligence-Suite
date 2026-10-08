package com.vyoog.eisplatform.modules.offering.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** The audience rule of one product (a row exists only when it differs from BOTH). */
@Entity
@Table(name = "product_eligibility")
@Getter
@Setter
public class ProductEligibility {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductAudience audience = ProductAudience.BOTH;
}
