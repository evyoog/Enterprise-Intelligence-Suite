package com.vyoog.eisplatform.modules.servicestatus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * REQ-PRT-001 (C20 interim status page): the current, manually posted status
 * of one catalog product. A product with no row is OPERATIONAL. Replaced by
 * Health Monitoring (10.04) in sprint 2027.1.1 (C20).
 */
@Entity
@Table(name = "product_service_status")
@Getter
@Setter
public class ProductServiceStatus {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceStatusValue status = ServiceStatusValue.OPERATIONAL;

    @Column(length = 500)
    private String note;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "updated_by_keycloak_sub")
    private String updatedByKeycloakSub;
}
