package com.vyoog.eisplatform.modules.registration.model;

import com.vyoog.eisplatform.modules.authorization.model.OrganizationOwnedResource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * 05.04.01 Groups (sprint 2026.4.1): a named grouping of an organization's
 * own members — e.g. "Engineering" or "Finance" — used today only to
 * organize members for {@link OrganizationMember} lookups (05.03.02 Assign
 * group). It carries no permissions or product access of its own; that
 * still comes from {@link OrgRole} and {@link OrganizationProductAccess}.
 */
@Entity
@Table(name = "organization_group")
@Getter
@Setter
public class OrganizationGroup implements OrganizationOwnedResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Override
    public Long organizationId() {
        return organizationId;
    }
}
