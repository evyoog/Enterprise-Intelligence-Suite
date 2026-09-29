package com.vyoog.eisplatform.modules.registration.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** 05.04.01.02/.03 Add/remove member (sprint 2026.4.1) — one row per
 * (group, organization member). Removing a member from a group deletes this
 * row outright; unlike {@link OrganizationMember} itself, there is no
 * history to preserve here. */
@Entity
@Table(name = "organization_group_member")
@Getter
@Setter
public class OrganizationGroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "organization_member_id", nullable = false)
    private Long organizationMemberId;

    @Column(nullable = false)
    private Instant addedAt = Instant.now();
}
