package com.vyoog.eisplatform.modules.template.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 15.01.02 Manage templates (REQ-GOV-002, C92, TM-1 A): an administrator's own wording of one message type in one
 * language. No row means the built-in text of {@code MessageCatalog} applies; deleting a row resets it.
 */
@Entity
@Table(name = "message_template", uniqueConstraints = @UniqueConstraint(name = "uq_message_template", columnNames = {"code", "language"}))
@Getter
@Setter
public class MessageTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String code;

    @Column(nullable = false, length = 10)
    private String language;

    /** The email subject, or the notification title. */
    @Column(nullable = false, length = 200)
    private String subject;

    /** The email body, or the notification message. Plain text with {placeholders}. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
