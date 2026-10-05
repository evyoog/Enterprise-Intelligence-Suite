package com.vyoog.eisplatform.modules.search.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** One search run from the results page, for search insights (C70). No user
 * identity is stored. */
@Entity
@Table(name = "search_query_log")
@Getter
@Setter
public class SearchQueryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String query;

    @Column(name = "result_count", nullable = false)
    private int resultCount;

    /** KEYWORD or HYBRID. */
    @Column(nullable = false, length = 10)
    private String mode;

    @Column(name = "semantic_used", nullable = false)
    private boolean semanticUsed;

    @Column(name = "took_ms", nullable = false)
    private int tookMs;

    @Column(name = "searched_at", nullable = false)
    private Instant searchedAt;

    /** Where the search ran: null = global search, KNOWLEDGE = the Knowledge
     * Center (REQ-KNW-006.2, used for knowledge gap detection). */
    @Column(length = 20)
    private String scope;
}
