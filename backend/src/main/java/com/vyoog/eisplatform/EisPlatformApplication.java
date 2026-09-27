package com.vyoog.eisplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/** {@code @EnableScheduling}: 07.04.01 Process expiry (sprint 2026.4.3) —
 * see {@code SubscriptionExpiryJob}, the only {@code @Scheduled} job so far. */
@SpringBootApplication
@EnableScheduling
public class EisPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(EisPlatformApplication.class, args);
    }
}
