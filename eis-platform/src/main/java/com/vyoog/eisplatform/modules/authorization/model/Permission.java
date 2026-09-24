package com.vyoog.eisplatform.modules.authorization.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * A single named capability (e.g. "MANAGE_CATALOG", "MANAGE_USERS") — the
 * actual unit an API/screen-level check tests for, never a role name
 * directly (see {@link Role}'s own javadoc for why that distinction matters).
 */
@Entity
@Table(name = "permission")
@Getter
@Setter
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Column(length = 255)
    private String description;
}
