package com.vyoog.eisplatform.modules.servicestatus.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * REQ-PRT-001.2: an incident a platform admin posts for one product. Open
 * while {@link #endedAt} is null. Details are shown only to customers who
 * purchased the product (C26). Moves to Incident & Problem Management (12.04)
 * in sprint 2027.1.3 (C20).
 */
@Entity
@Table(name = "service_incident")
@Getter
@Setter
public class ServiceIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 4000)
    private String message;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by_keycloak_sub")
    private String createdByKeycloakSub;
}
