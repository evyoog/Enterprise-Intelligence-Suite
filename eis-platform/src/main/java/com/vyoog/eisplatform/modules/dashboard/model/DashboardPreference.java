package com.vyoog.eisplatform.modules.dashboard.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Phase 16: one row per customer — "Customization: widget visibility, widget
 * ordering." Stored as two plain JSON-array text columns (widget ids are
 * frontend-defined strings, e.g. "products", "alerts", "organization") since
 * a fully normalized widget schema would be over-built for what's actually
 * needed: an ordered list plus a hidden-set. {@link #customerId} is the
 * primary key directly — at most one preference row per customer, so there's
 * nothing to key by id separately.
 */
@Entity
@Table(name = "dashboard_preference")
@Getter
@Setter
public class DashboardPreference {

    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "widget_order_json", columnDefinition = "text")
    private String widgetOrderJson;

    @Column(name = "hidden_widgets_json", columnDefinition = "text")
    private String hiddenWidgetsJson;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
