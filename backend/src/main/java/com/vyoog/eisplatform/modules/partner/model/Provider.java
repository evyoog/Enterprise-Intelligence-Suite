package com.vyoog.eisplatform.modules.partner.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 14.01 Provider Onboarding (sprint 2027.2.1). A prospective partner's own
 * company/contact details — deliberately NOT a {@code Customer} (a provider
 * applies before it has any Vyoog account or Keycloak identity, the same way
 * an organization's own registration does, see {@code Organization}), and
 * not yet an owner of anything in the catalog (see decision C42 on why
 * 14.02 Publisher Management is out of scope this sprint).
 */
@Entity
@Table(name = "provider")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Provider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "contact_email", nullable = false)
    private String contactEmail;

    @Column(length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProviderStatus status = ProviderStatus.REGISTERED;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
