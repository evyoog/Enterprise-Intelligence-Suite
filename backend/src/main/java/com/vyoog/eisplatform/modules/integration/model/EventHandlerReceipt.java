package com.vyoog.eisplatform.modules.integration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** REQ-INT-002.4: a handler has processed an event; a repeat delivery to the
 * same handler is skipped (unique handler + event). */
@Entity
@Table(name = "event_handler_receipt",
    uniqueConstraints = @UniqueConstraint(name = "uq_event_handler_receipt", columnNames = {"handler_name", "event_id"}))
@Getter
@Setter
public class EventHandlerReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "handler_name", nullable = false, length = 100)
    private String handlerName;

    @Column(name = "event_id", nullable = false, length = 36)
    private String eventId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;
}
