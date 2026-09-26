package com.vyoog.eisplatform.modules.federation.repository;

import com.vyoog.eisplatform.modules.federation.model.OidcIdentityProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OidcIdentityProviderRepository extends JpaRepository<OidcIdentityProvider, Long> {

    List<OidcIdentityProvider> findByOrganizationIdOrderByCreatedAtAsc(Long organizationId);

    Optional<OidcIdentityProvider> findByOrganizationIdAndEnabledTrue(Long organizationId);
}
