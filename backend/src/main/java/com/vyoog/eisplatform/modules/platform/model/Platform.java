package com.vyoog.eisplatform.modules.platform.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * A "high-level platform" (e.g. Thittam) — a brand an admin groups apps
 * under. The Product <-> Platform relationship lives on the Product side
 * (see Product.platforms); this entity has no back-reference to its
 * products, since every screen that needs "apps in this platform" already
 * has the full admin product list in hand and filters it client-side.
 */
@Entity
@Table(name = "platforms")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Platform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    private String imageUrl;

    /** C66: the platform's showcase colour (#RRGGBB). Null = the default EIS
     * indigo. Drives the catalog card accent, never the whole card. */
    @Column(name = "primary_color", length = 7)
    private String primaryColor;

    /** C66: ACTIVE platforms with {@link #showInCatalog} appear in the
     * public Product Catalog; INACTIVE ones are kept but hidden. */
    @Column(nullable = false, length = 10)
    private String status = "ACTIVE";

    @Column(name = "show_in_catalog", nullable = false)
    private boolean showInCatalog = true;

    /** C66: catalog order, ascending (ties by name). */
    @Column(name = "display_order", nullable = false)
    private int displayOrder = 0;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
