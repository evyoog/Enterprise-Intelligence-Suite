package com.vyoog.eisplatform.modules.partner.job;

import com.vyoog.eisplatform.modules.partner.service.PartnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** 14.01.02.03 Track expiration — same pattern as {@code SubscriptionExpiryJob}
 * (07.04.01, sprint 2026.4.3). */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContractExpiryJob {

    private final PartnerService partnerService;

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void run() {
        int expired = partnerService.expireOverdueContracts();
        if (expired > 0) {
            log.info("Expired {} overdue partner contract(s)", expired);
        }
    }
}
