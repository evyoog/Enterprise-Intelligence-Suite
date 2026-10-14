package com.vyoog.eisplatform.modules.billing.repository;

import com.vyoog.eisplatform.modules.billing.model.PaymentGatewayConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentGatewayConfigRepository extends JpaRepository<PaymentGatewayConfig, Long> {
    Optional<PaymentGatewayConfig> findByProviderCode(String providerCode);

    List<PaymentGatewayConfig> findByCustomTrueOrderByProviderCode();
}
