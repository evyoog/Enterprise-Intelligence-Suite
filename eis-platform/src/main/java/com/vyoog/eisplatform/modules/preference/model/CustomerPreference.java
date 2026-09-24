package com.vyoog.eisplatform.modules.preference.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Phase 6 (2026.3.3): one row per customer — see this table's own schema.sql
 * comment for why this is a distinct concept from {@code DashboardPreference}
 * (widget layout). Every column is nullable except {@link #reducedMotion} —
 * null means "not set, fall back to the browser/OS default," which is a
 * different, deliberate state from an explicit value (see
 * PreferenceService#toDto for how each null is resolved).
 */
@Entity
@Table(name = "customer_preference")
@Getter
@Setter
public class CustomerPreference {

    @Id
    @Column(name = "customer_id")
    private Long customerId;

    /** UI string language — one of the codes this app's own i18n bundle
     * actually ships (see SUPPORTED_LANGUAGES on the frontend); never
     * validated against IANA/ISO exhaustively, only against what this app
     * can actually render, so a stored value always has real translations
     * behind it. */
    @Column(length = 10)
    private String language;

    /** A locale used for Intl date/number FORMATTING conventions only (e.g.
     * "en-IN" vs "en-US") — deliberately distinct from {@link #language}
     * (which strings are shown) and from {@link #timeZone} (which clock is
     * used); see PreferenceService's own javadoc for why these three were
     * previously conflated. */
    @Column(length = 20)
    private String region;

    /** An IANA time zone id (e.g. "Asia/Kolkata") — which clock, never which
     * language or formatting convention. */
    @Column(name = "time_zone", length = 64)
    private String timeZone;

    /** One of "light" / "dark" / "system". */
    @Column(name = "theme_mode", length = 10)
    private String themeMode;

    /** A manual override on top of the OS-level {@code prefers-reduced-motion}
     * media query — for a user who can't easily change that OS setting
     * (e.g. a shared or managed machine) but still wants this app's own
     * animations reduced. Never itself reports the OS preference; the
     * frontend still separately honors {@code prefers-reduced-motion}
     * regardless of this flag. */
    @Column(name = "reduced_motion", nullable = false)
    private boolean reducedMotion;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
