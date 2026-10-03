package com.vyoog.eisplatform.modules.renewal.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

/** REQ-SUB-004.7 (BR-6): one sent reminder. The unique (subscription,
 * recipient, local date) row is written before the email, so a reminder is
 * never sent twice on the same day. */
@Entity
@Table(name = "renewal_reminder_log",
    uniqueConstraints = @UniqueConstraint(name = "uq_renewal_reminder_log", columnNames = {"subscription_id", "recipient_customer_id", "local_date"}),
    indexes = @Index(name = "idx_renewal_reminder_log_subscription", columnList = "subscription_id"))
@Getter
@Setter
public class RenewalReminderLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @Column(name = "recipient_customer_id", nullable = false)
    private Long recipientCustomerId;

    @Column(name = "local_date", nullable = false)
    private LocalDate localDate;

    @Column(name = "renewal_date", nullable = false)
    private Instant renewalDate;

    @Column(name = "days_before", nullable = false)
    private int daysBefore;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;
}
