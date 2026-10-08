package com.vyoog.eisplatform.modules.authorization.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * An individual grant (or removal) of one feature permission for one organization member — the
 * REQ-TEN-005 "individual override" design, built only as far as {@code INVITE_USERS} needs it
 * (REQ-TEN-008, C84): permission items only.
 */
@Entity
@Table(name = "member_access_override")
@Getter
@Setter
public class MemberAccessOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "organization_member_id", nullable = false)
    private Long organizationMemberId;

    @Column(name = "item_type", nullable = false, length = 20)
    private String itemType = "PERMISSION";

    @Column(name = "permission_code", nullable = false, length = 60)
    private String permissionCode;

    @Column(nullable = false)
    private boolean granted;

    @Column(name = "set_by_customer_id")
    private Long setByCustomerId;

    @Column(name = "set_at", nullable = false)
    private Instant setAt = Instant.now();
}
