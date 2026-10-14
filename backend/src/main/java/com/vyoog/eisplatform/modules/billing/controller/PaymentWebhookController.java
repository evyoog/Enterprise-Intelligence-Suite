package com.vyoog.eisplatform.modules.billing.controller;

import com.vyoog.eisplatform.modules.billing.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/** REQ-BIL-001.8, REQ-BIL-003.10 (C98): one endpoint per provider, {@code POST /webhooks/{provider}}. No user JWT — permitted without auth in
 * SecurityConfig, trusted only via the provider's own signature (BR-5/BR-6), verified against the RAW request body (never a re-serialized one,
 * which could differ byte-for-byte and always fail signature verification). The existing {@code /webhooks/razorpay} URL is unchanged. */
@RestController
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhooks/{provider}")
    public ResponseEntity<Void> receive(@PathVariable String provider, @RequestBody String rawBody, @RequestHeader HttpHeaders headers) {
        Map<String, String> flat = new LinkedHashMap<>();
        headers.forEach((name, values) -> flat.put(name, values.isEmpty() ? "" : values.get(0)));
        try {
            paymentService.processWebhook(provider, rawBody, flat);
        } catch (DataIntegrityViolationException duplicate) {
            // Two deliveries of the same event raced; the other one won. Acknowledge so the provider stops retrying.
        }
        return ResponseEntity.ok().build();
    }
}
