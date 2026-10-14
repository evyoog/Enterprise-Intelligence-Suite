package com.vyoog.eisplatform.modules.serviceinstance.job;

import com.vyoog.eisplatform.modules.serviceinstance.service.ServiceInstanceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** C101: every 15 minutes, brings each service instance in step with its subscription (a safety net for the hook in the subscription flow). */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.service-instances.job-enabled", havingValue = "true", matchIfMissing = true)
public class ServiceInstanceReconcileJob {

    private static final Logger log = LoggerFactory.getLogger(ServiceInstanceReconcileJob.class);
    private final ServiceInstanceService service;

    @Scheduled(fixedDelayString = "${app.service-instances.reconcile-interval-ms:900000}", initialDelayString = "${app.service-instances.reconcile-initial-delay-ms:60000}")
    public void run() {
        try {
            int changed = service.reconcile();
            if (changed > 0) {
                log.info("Service instances reconciled: {} changed", changed);
            }
        } catch (RuntimeException e) {
            log.warn("Service instance reconcile failed: {}", e.toString());
        }
    }
}
