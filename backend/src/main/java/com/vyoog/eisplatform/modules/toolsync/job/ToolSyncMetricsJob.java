package com.vyoog.eisplatform.modules.toolsync.job;

import com.vyoog.eisplatform.modules.toolsync.service.ToolSyncMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Keeps the synchronization gauges (waiting, failed, oldest waiting) up to date. */
@Component
@RequiredArgsConstructor
public class ToolSyncMetricsJob {

    private final ToolSyncMetrics metrics;

    @Scheduled(fixedDelayString = "${app.sync.metrics-interval-ms:30000}", initialDelayString = "${app.sync.metrics-interval-ms:30000}")
    public void refresh() {
        try {
            metrics.refreshGauges();
        } catch (RuntimeException ignored) {
            // metrics never break the application; the next run tries again
        }
    }
}
