package com.vyoog.eisplatform.modules.renewal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** C100 RR-5: a person an organization administrator added to the renewal reminders of the organization's subscriptions, with the channels
 * (email, in-app) they get them on. Organization admins and people with Manage billing always get them (RR-2 B) and need no row. */
@Entity
@Table(name = "renewal_reminder_recipient")
@Getter
@Setter
public class RenewalReminderRecipient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "email_enabled", nullable = false)
    private boolean emailEnabled = true;

    @Column(name = "in_app_enabled", nullable = false)
    private boolean inAppEnabled = true;

    @Column(name = "created_by_customer_id")
    private Long createdByCustomerId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
