package com.vyoog.eisplatform.modules.federation.repository;

import com.vyoog.eisplatform.modules.federation.model.SamlIdentityProvider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SamlIdentityProviderRepository extends JpaRepository<SamlIdentityProvider, Long> {

    List<SamlIdentityProvider> findByOrganizationId(Long organizationId);

    /** Phase 5 will use this to find the one unambiguous IdP to redirect a
     * login attempt to — see the entity's own javadoc on why at most one
     * enabled row per organization is guaranteed. */
    Optional<SamlIdentityProvider> findByOrganizationIdAndEnabledTrue(Long organizationId);
}
