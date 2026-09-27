package com.vyoog.eisplatform.modules.administration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * 15.01.01 Configure feature flags (sprint 2026.4.2) — a runtime, admin-
 * toggleable flag, distinct from a Spring config property like
 * {@code app.status-page.enabled} (set once at deploy time, not changeable
 * without a restart). {@code flagKey} is the stable identifier a module
 * checks by calling {@link com.vyoog.eisplatform.modules.administration.service.PlatformFeatureFlagService#isEnabled}.
 * "groups_enabled" (05.04.01 Groups, sprint 2026.4.1) is the first real
 * consumer, seeded true so nothing already built silently stops working.
 */
@Entity
@Table(name = "platform_feature_flag")
@Getter
@Setter
public class PlatformFeatureFlag {

    @Id
    @Column(name = "flag_key", length = 100)
    private String flagKey;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(length = 500)
    private String description;
}
