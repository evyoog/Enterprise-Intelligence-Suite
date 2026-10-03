package com.vyoog.eisplatform.modules.renewal.service;

import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

/**
 * REQ-SUB-004.2 (C64): auto-renewal. Each due subscription is renewed in its
 * own transaction ({@link SubscriptionService#autoRenew}), so one failure
 * does not hold back the others.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RenewalService {

    private final ProductSubscriptionRepository subscriptionRepository;
    private final SubscriptionService subscriptionService;

    /**
     * BR-3: charging a saved payment method without the customer needs the
     * Razorpay Customer/Token integration (C47 follow-up), which is not
     * built — so every renewal issues a renewal invoice for now. This is the
     * one switch point once it exists.
     */
    public boolean canChargeAutomatically(ProductSubscription subscription) {
        return false;
    }

    public int renewDue() {
        List<Long> due = subscriptionRepository
            .findByStatusAndAutoRenewTrueAndExpiresAtLessThanEqual(SubscriptionStatus.ACTIVE, Instant.now()).stream()
            .map(ProductSubscription::getId).toList();
        int renewed = 0;
        for (Long id : due) {
            try {
                if (subscriptionService.autoRenew(id)) {
                    renewed++;
                }
            } catch (RuntimeException e) {
                log.warn("Auto-renewal of subscription {} failed: {}", id, e.getMessage());
            }
        }
        return renewed;
    }
}
