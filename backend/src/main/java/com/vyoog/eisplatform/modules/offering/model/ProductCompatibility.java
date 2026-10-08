package com.vyoog.eisplatform.modules.offering.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** "Works with" (OF-7): product {@code productId} works with {@code worksWithProductId}; one-directional. */
@Entity
@Table(name = "product_compatibility")
@Getter
@Setter
public class ProductCompatibility {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "works_with_product_id", nullable = false)
    private Long worksWithProductId;
}
