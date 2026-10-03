package com.vyoog.eisplatform.modules.renewal.job;

import com.vyoog.eisplatform.modules.renewal.service.RenewalReminderService;
import com.vyoog.eisplatform.modules.renewal.service.RenewalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** REQ-SUB-004 (C64): every 15 minutes, renew due auto-renewing
 * subscriptions, then send due renewal reminders. Off when
 * {@code app.renewals.job-enabled=false} (tests drive the services directly). */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.renewals.job-enabled", havingValue = "true", matchIfMissing = true)
public class RenewalJob {

    private final RenewalService renewalService;
    private final RenewalReminderService renewalReminderService;

    @Scheduled(fixedDelay = 15 * 60 * 1000, initialDelay = 60 * 1000)
    public void run() {
        int renewed = renewalService.renewDue();
        int reminded = renewalReminderService.sendDue();
        if (renewed > 0 || reminded > 0) {
            log.info("Auto-renewed {} subscription(s); sent {} renewal reminder(s)", renewed, reminded);
        }
    }
}
