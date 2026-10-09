package com.vyoog.eisplatform.modules.toolsync.job;

import com.vyoog.eisplatform.modules.toolsync.service.ToolReconcileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Compares every READY tenant with its tool and repairs drift (contract v1 section 8). Off by default ({@code app.sync.reconcile.enabled})
 * until the tools answer {@code get_state_digest}; the schedule is {@code app.sync.reconcile.cron} (default: every night at 02:30).
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.sync.reconcile.enabled", havingValue = "true")
public class ToolReconcileJob {

    private final ToolReconcileService reconcile;

    @Scheduled(cron = "${app.sync.reconcile.cron:0 30 2 * * *}")
    public void run() {
        reconcile.reconcileAllAndRepair().forEach(r -> log.warn("Reconcile {} / organization {}: {}, resent {}", r.productCode(), r.organizationId(),
            r.reachable() ? "drift" : r.problem(), r.resent()));
    }
}
