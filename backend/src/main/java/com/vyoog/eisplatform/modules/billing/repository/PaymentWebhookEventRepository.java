package com.vyoog.eisplatform.modules.billing.repository;

import com.vyoog.eisplatform.modules.billing.model.PaymentWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentWebhookEventRepository extends JpaRepository<PaymentWebhookEvent, Long> {
    /** C98: idempotency is per provider. */
    Optional<PaymentWebhookEvent> findByProviderAndProviderEventId(String provider, String providerEventId);

    List<PaymentWebhookEvent> findTop20ByProviderOrderByReceivedAtDesc(String provider);
}
