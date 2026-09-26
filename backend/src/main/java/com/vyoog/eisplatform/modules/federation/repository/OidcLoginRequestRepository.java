package com.vyoog.eisplatform.modules.federation.repository;

import com.vyoog.eisplatform.modules.federation.model.OidcLoginRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OidcLoginRequestRepository extends JpaRepository<OidcLoginRequest, String> {

    List<OidcLoginRequest> findByOrganizationId(Long organizationId);
}
