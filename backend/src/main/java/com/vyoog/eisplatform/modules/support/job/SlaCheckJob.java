package com.vyoog.eisplatform.modules.support.job;

import com.vyoog.eisplatform.modules.support.service.SlaService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/** C101 / REQ-SUP-002: every minute, warns and escalates tickets by their SLA deadlines. Off with {@code app.sla.job-enabled=false} (tests). */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.sla.job-enabled", havingValue = "true", matchIfMissing = true)
public class SlaCheckJob {

    private static final Logger log = LoggerFactory.getLogger(SlaCheckJob.class);
    private final SlaService slaService;

    @Scheduled(fixedDelayString = "${app.sla.interval-ms:60000}", initialDelayString = "${app.sla.initial-delay-ms:30000}")
    public void run() {
        try {
            int[] result = slaService.check(Instant.now());
            if (result[0] > 0 || result[1] > 0) {
                log.info("SLA check: {} warning(s), {} breach(es)", result[0], result[1]);
            }
        } catch (RuntimeException e) {
            log.warn("SLA check failed: {}", e.toString());
        }
    }
}
