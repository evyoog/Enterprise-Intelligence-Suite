package com.vyoog.eisplatform.modules.registration.job;

import com.vyoog.eisplatform.modules.registration.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 07.04.01 Process expiry (sprint 2026.4.3): runs hourly, flipping any
 * ACTIVE subscription whose {@code expiresAt} has passed to EXPIRED. See
 * {@link SubscriptionService#expireOverdueSubscriptions()} for the actual
 * work; this class is just the trigger.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionExpiryJob {

    private final SubscriptionService subscriptionService;

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void run() {
        int expired = subscriptionService.expireOverdueSubscriptions();
        if (expired > 0) {
            log.info("Expired {} overdue subscription(s)", expired);
        }
    }
}
