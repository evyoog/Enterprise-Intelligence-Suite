package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.modules.billing.event.InvoiceActivationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** C97 BI-1: a purchase waiting for payment starts when its invoice is paid, or when pay by invoice is chosen. Runs inside the payment
 * transaction, so the subscription is active by the time the response is built. */
@Component
@RequiredArgsConstructor
public class PendingSubscriptionActivator {

    private final SubscriptionService subscriptionService;

    @EventListener
    public void onInvoiceActivation(InvoiceActivationEvent event) {
        for (Long subscriptionId : event.subscriptionIds()) {
            subscriptionService.activatePending(subscriptionId, event.reason());
        }
    }
}
