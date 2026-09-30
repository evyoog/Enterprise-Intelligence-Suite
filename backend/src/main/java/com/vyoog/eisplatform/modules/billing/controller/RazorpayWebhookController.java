package com.vyoog.eisplatform.modules.billing.controller;

import com.vyoog.eisplatform.modules.billing.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** REQ-BIL-001.8. No user JWT — permitted without auth in SecurityConfig,
 * trusted only via the Razorpay webhook signature (BR-5/BR-6), verified
 * against the RAW request body (never a re-serialized one, which could
 * differ byte-for-byte and always fail signature verification). */
@RestController
@RequiredArgsConstructor
public class RazorpayWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/webhooks/razorpay")
    public ResponseEntity<Void> receive(@RequestBody String rawBody, @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.processWebhook(rawBody, signature);
        return ResponseEntity.ok().build();
    }
}
