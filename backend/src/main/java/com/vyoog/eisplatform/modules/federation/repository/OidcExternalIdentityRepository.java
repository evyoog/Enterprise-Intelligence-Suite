package com.vyoog.eisplatform.modules.federation.repository;

import com.vyoog.eisplatform.modules.federation.model.OidcExternalIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OidcExternalIdentityRepository extends JpaRepository<OidcExternalIdentity, Long> {

    Optional<OidcExternalIdentity> findByOrganizationIdAndIssuerAndSubject(Long organizationId, String issuer, String subject);
}
