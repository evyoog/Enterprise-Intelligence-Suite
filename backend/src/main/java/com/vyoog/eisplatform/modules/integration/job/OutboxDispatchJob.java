package com.vyoog.eisplatform.modules.integration.job;

import com.vyoog.eisplatform.modules.integration.service.OutboxDispatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** REQ-INT-002.3: runs the outbox dispatcher every few seconds
 * ({@code app.events.dispatcher.interval-ms}, default 5 s). Off when
 * {@code app.events.dispatcher.enabled=false} (tests). */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.events.dispatcher.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxDispatchJob {

    private final OutboxDispatcher outboxDispatcher;

    @Scheduled(fixedDelayString = "${app.events.dispatcher.interval-ms:5000}", initialDelayString = "${app.events.dispatcher.interval-ms:5000}")
    public void run() {
        outboxDispatcher.dispatchDue();
    }
}
