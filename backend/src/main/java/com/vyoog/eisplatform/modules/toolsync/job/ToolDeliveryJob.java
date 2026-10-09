package com.vyoog.eisplatform.modules.toolsync.job;

import com.vyoog.eisplatform.modules.toolsync.service.ToolDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Runs {@link ToolDeliveryService#deliverDue()} every few seconds, so a change reaches the tools within seconds. Off with {@code app.events.tool-delivery.enabled=false}. */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(prefix = "app.events.tool-delivery", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ToolDeliveryJob {

    private final ToolDeliveryService deliveryService;

    @Scheduled(fixedDelayString = "${app.events.tool-delivery.interval-ms:3000}", initialDelayString = "${app.events.tool-delivery.interval-ms:3000}")
    public void run() {
        try {
            int delivered = deliveryService.deliverDue();
            if (delivered > 0) {
                log.debug("Delivered {} message(s) to tools", delivered);
            }
        } catch (RuntimeException e) {
            log.warn("Tool delivery run failed: {}", e.toString());
        }
    }
}
