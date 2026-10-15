package com.vyoog.eisplatform.modules.dashboard.job;

import com.vyoog.eisplatform.modules.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** C104 / AN-1 (16 Analytics & data platform): deletes product-launch history rows older than 13 months. Off with {@code app.analytics.job-enabled=false} (tests). */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.analytics.job-enabled", havingValue = "true", matchIfMissing = true)
public class ProductLaunchHistoryCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(ProductLaunchHistoryCleanupJob.class);
    private final DashboardService dashboardService;

    @Scheduled(fixedDelayString = "${app.analytics.cleanup-interval-ms:86400000}", initialDelayString = "${app.analytics.cleanup-initial-delay-ms:300000}")
    public void run() {
        try {
            int removed = dashboardService.purgeOldLaunchHistory();
            if (removed > 0) {
                log.info("Product launch history rows purged: {}", removed);
            }
        } catch (RuntimeException e) {
            log.warn("Product launch history cleanup job failed: {}", e.toString());
        }
    }
}
