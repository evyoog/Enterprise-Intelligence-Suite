package com.vyoog.eisplatform.modules.federation.repository;

import com.vyoog.eisplatform.modules.federation.model.SamlLoginRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SamlLoginRequestRepository extends JpaRepository<SamlLoginRequest, String> {

    List<SamlLoginRequest> findByOrganizationId(Long organizationId);
}
