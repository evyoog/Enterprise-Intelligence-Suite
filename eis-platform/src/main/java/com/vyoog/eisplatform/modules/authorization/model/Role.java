package com.vyoog.eisplatform.modules.authorization.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * A named bundle of {@link Permission}s, scoped as either PLATFORM or
 * ORGANIZATION (see {@link RoleScope}). This is the piece that used to be a
 * hardcoded {@code hasRole("ADMIN")} string in SecurityConfig, or a raw
 * {@code orgRole != ORG_ADMIN} comparison in OrganizationSelfService — both
 * now go through {@code AuthorizationService} asking "does the caller hold a
 * Role, in the right scope, that grants this specific Permission" instead.
 *
 * Deliberately NOT where "who holds this role" is decided — that stays with
 * whatever already decided it before this phase (Keycloak's client-role claim
 * for PLATFORM roles, {@code OrganizationMember.orgRole} for ORGANIZATION
 * roles). This table only answers "what is a role allowed to do," which is
 * what makes permissions genuinely DB-editable without a code change or
 * redeploy, without touching who's authenticated as what.
 */
@Entity
@Table(name = "role")
@Getter
@Setter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleScope scope;

    @Column(length = 255)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permission",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}
