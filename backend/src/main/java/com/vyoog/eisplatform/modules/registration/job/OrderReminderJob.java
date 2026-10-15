package com.vyoog.eisplatform.modules.registration.job;

import com.vyoog.eisplatform.modules.registration.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** C103 / REQ-TEN-010: reminds the organization's admins once for every purchase request still pending after 3 days. Off with {@code app.orders.job-enabled=false} (tests). */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.orders.job-enabled", havingValue = "true", matchIfMissing = true)
public class OrderReminderJob {

    private static final Logger log = LoggerFactory.getLogger(OrderReminderJob.class);
    private final OrderService orderService;

    @Scheduled(fixedDelayString = "${app.orders.reminder-interval-ms:3600000}", initialDelayString = "${app.orders.reminder-initial-delay-ms:120000}")
    public void run() {
        try {
            int sent = orderService.remindPendingOrders();
            if (sent > 0) {
                log.info("Order reminders sent: {}", sent);
            }
        } catch (RuntimeException e) {
            log.warn("Order reminder job failed: {}", e.toString());
        }
    }
}
