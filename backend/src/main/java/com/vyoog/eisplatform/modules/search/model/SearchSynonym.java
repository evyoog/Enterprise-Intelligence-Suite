package com.vyoog.eisplatform.modules.search.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** An admin-managed synonym group (C70): comma-separated terms that keyword
 * search treats as equal, for example "invoice, bill, factura". */
@Entity
@Table(name = "search_synonym")
@Getter
@Setter
public class SearchSynonym {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String terms;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}
