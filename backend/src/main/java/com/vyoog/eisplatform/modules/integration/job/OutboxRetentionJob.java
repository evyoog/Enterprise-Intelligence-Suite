package com.vyoog.eisplatform.modules.integration.job;

import com.vyoog.eisplatform.modules.integration.service.OutboxDispatcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** REQ-INT-002.7: daily removal of DELIVERED events older than
 * {@code app.events.retention-days} (engineering default 30, OQ 2). */
@Component
@Slf4j
public class OutboxRetentionJob {

    private final OutboxDispatcher outboxDispatcher;
    private final int retentionDays;

    public OutboxRetentionJob(OutboxDispatcher outboxDispatcher, @Value("${app.events.retention-days:30}") int retentionDays) {
        this.outboxDispatcher = outboxDispatcher;
        this.retentionDays = retentionDays;
    }

    @Scheduled(cron = "0 30 3 * * *")
    public void run() {
        int removed = outboxDispatcher.purgeDelivered(retentionDays);
        if (removed > 0) {
            log.info("Removed {} delivered event(s) older than {} days", removed, retentionDays);
        }
    }
}
