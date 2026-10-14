package com.vyoog.eisplatform.modules.billing.tax.service;

import org.springframework.stereotype.Component;

/** The wait between provider attempts (BR-34), separate so tests do not sleep. */
@Component
public class TaxRetrySleeper {

    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
