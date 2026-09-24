package com.vyoog.eisplatform.modules.notification.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * One row per customer — which categories they've muted EMAIL for. In-app
 * notifications are always recorded regardless (the notification center is
 * the durable record of what happened; muting it entirely would defeat that),
 * so this only ever controls the "avoid unnecessary notification noise" email
 * side. Stored as a plain JSON array of category names, same pattern as
 * DashboardPreference's widget lists — a handful of category toggles doesn't
 * need a normalized join table.
 */
@Entity
@Table(name = "notification_preference")
@Getter
@Setter
public class NotificationPreference {

    @Id
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "email_disabled_categories_json", columnDefinition = "text")
    private String emailDisabledCategoriesJson;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
