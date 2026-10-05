package com.vyoog.eisplatform.modules.search.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** One full index build: the startup backfill or an admin "Rebuild index"
 * (C70). */
@Entity
@Table(name = "search_index_run")
@Getter
@Setter
public class SearchIndexRun {

    public enum Trigger { BACKFILL, ADMIN }

    public enum Status { RUNNING, DONE, FAILED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 10)
    private Trigger triggerType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    @Column(nullable = false)
    private int documents;

    @Column(nullable = false)
    private int chunks;

    @Column(nullable = false)
    private int embedded;

    @Column(length = 1000)
    private String error;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;
}
