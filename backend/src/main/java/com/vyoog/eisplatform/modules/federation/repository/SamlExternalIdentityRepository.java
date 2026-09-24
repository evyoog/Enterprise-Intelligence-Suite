package com.vyoog.eisplatform.modules.federation.repository;

import com.vyoog.eisplatform.modules.federation.model.SamlExternalIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SamlExternalIdentityRepository extends JpaRepository<SamlExternalIdentity, Long> {

    /** The stable identity lookup — see {@link SamlExternalIdentity}'s own
     * javadoc for why this exact triple, and nothing else, is the key. */
    Optional<SamlExternalIdentity> findByOrganizationIdAndIdpEntityIdAndNameId(
        Long organizationId, String idpEntityId, String nameId);
}
