package com.vyoog.eisplatform.modules.auth.repository;

import com.vyoog.eisplatform.modules.auth.model.MfaRecoveryCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MfaRecoveryCodeRepository extends JpaRepository<MfaRecoveryCode, Long> {

    List<MfaRecoveryCode> findByCustomerId(Long customerId);

    Optional<MfaRecoveryCode> findByCustomerIdAndCodeHashAndUsedAtIsNull(Long customerId, String codeHash);

    void deleteByCustomerId(Long customerId);

    long countByCustomerIdAndUsedAtIsNull(Long customerId);
}
