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

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
