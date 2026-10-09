package com.vyoog.eisplatform.modules.toolsync.job;

import com.vyoog.eisplatform.modules.toolsync.service.ToolIdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Removes the stored results of tools' calls once they are older than the retention period (at least 7 days, contract v1 section 5). Daily. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ToolSyncMaintenanceJob {

    private final ToolIdempotencyService idempotency;

    @Scheduled(cron = "${app.sync.maintenance-cron:0 15 3 * * *}")
    public void purgeIdempotencyRecords() {
        int removed = idempotency.purgeOlderThanRetention();
        if (removed > 0) {
            log.info("Removed {} old idempotency record(s) of tool calls", removed);
        }
    }
}
