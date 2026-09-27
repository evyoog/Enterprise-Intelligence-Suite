package com.vyoog.eisplatform.modules.administration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 15.01.02 Configure regions (sprint 2026.4.2) — freely admin-defined, not
 * a fixed geography list (nothing in any source document specifies which
 * regions this platform must support). 05.02.01.03 Assign region (Tenant
 * Lifecycle) points an {@link com.vyoog.eisplatform.modules.registration.model.Organization}
 * at one of these by id.
 */
@Entity
@Table(name = "platform_region")
@Getter
@Setter
public class PlatformRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private boolean enabled = true;
}
