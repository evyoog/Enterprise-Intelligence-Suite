package com.vyoog.eisplatform.modules.auth.repository;

import com.vyoog.eisplatform.modules.auth.model.CustomerMfaCredential;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerMfaCredentialRepository extends JpaRepository<CustomerMfaCredential, Long> {
}
