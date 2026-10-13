package com.vyoog.eisplatform.modules.billing.job;

import com.vyoog.eisplatform.modules.billing.service.InvoiceReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** C97 BI-2: every 30 minutes, send the invoice payment reminders that are due. Off when {@code app.renewals.job-enabled=false}. */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.renewals.job-enabled", havingValue = "true", matchIfMissing = true)
public class InvoiceReminderJob {

    private final InvoiceReminderService service;

    @Scheduled(fixedDelay = 30 * 60 * 1000, initialDelay = 2 * 60 * 1000)
    public void run() {
        int sent = service.sendDue();
        if (sent > 0) {
            log.info("Sent {} invoice payment reminder(s)", sent);
        }
    }
}
