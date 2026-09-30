package com.vyoog.eisplatform.modules.billing.repository;

import com.vyoog.eisplatform.modules.billing.model.PaymentMethod;
import com.vyoog.eisplatform.modules.billing.model.PaymentMethodStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {
    List<PaymentMethod> findByOwnerCustomerIdAndStatus(Long ownerCustomerId, PaymentMethodStatus status);
    List<PaymentMethod> findByOwnerOrganizationIdAndStatus(Long ownerOrganizationId, PaymentMethodStatus status);
    Optional<PaymentMethod> findByOwnerCustomerIdAndIsDefaultTrueAndStatus(Long ownerCustomerId, PaymentMethodStatus status);
    Optional<PaymentMethod> findByOwnerOrganizationIdAndIsDefaultTrueAndStatus(Long ownerOrganizationId, PaymentMethodStatus status);
}
