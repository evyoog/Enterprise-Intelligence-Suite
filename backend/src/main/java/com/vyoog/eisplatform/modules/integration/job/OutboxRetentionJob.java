package com.vyoog.eisplatform.modules.integration.job;

import com.vyoog.eisplatform.modules.integration.service.OutboxDispatcher;
import com.vyoog.eisplatform.modules.integration.service.EventPlatformSettingsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** REQ-INT-002.7: daily removal of DELIVERED events older than the retention period set on the event platform
 * settings screen (default 30 days, configurable, EV-3 / C91). */
@Component
@Slf4j
public class OutboxRetentionJob {

    private final OutboxDispatcher outboxDispatcher;
    private final EventPlatformSettingsService settingsService;

    public OutboxRetentionJob(OutboxDispatcher outboxDispatcher, EventPlatformSettingsService settingsService) {
        this.outboxDispatcher = outboxDispatcher;
        this.settingsService = settingsService;
    }

    @Scheduled(cron = "0 30 3 * * *")
    public void run() {
        int retentionDays = settingsService.retentionDays();
        int removed = outboxDispatcher.purgeDelivered(retentionDays);
        if (removed > 0) {
            log.info("Removed {} delivered event(s) older than {} days", removed, retentionDays);
        }
    }
}
