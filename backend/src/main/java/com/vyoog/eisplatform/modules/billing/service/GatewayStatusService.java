package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.PaymentGatewayNotConfiguredException;
import com.vyoog.eisplatform.modules.billing.dto.GatewayStatusDto;
import com.vyoog.eisplatform.modules.billing.repository.PaymentWebhookEventRepository;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayClient;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** REQ-BIL-001.13/.14 — read-only; never edits a credential (BR-SEC-001). */
@Service
@RequiredArgsConstructor
public class GatewayStatusService {

    private final RazorpayProperties properties;
    private final RazorpayClient razorpayClient;
    private final PaymentWebhookEventRepository webhookEventRepository;

    @Value("${app.backend-url}")
    private String backendUrl;

    public GatewayStatusDto getStatus() {
        var lastEvent = webhookEventRepository.findFirstByOrderByReceivedAtDesc();
        return new GatewayStatusDto(
            "Razorpay",
            properties.isConfigured(),
            properties.isLiveMode(),
            properties.maskedKeyId(),
            !properties.getKeySecret().isBlank(),
            !properties.getWebhookSecret().isBlank(),
            backendUrl + "/webhooks/razorpay",
            lastEvent != null ? lastEvent.getReceivedAt() : null,
            lastEvent != null ? lastEvent.getEventType() : null
        );
    }

    public void testConnection() {
        if (!properties.isConfigured()) {
            throw new PaymentGatewayNotConfiguredException();
        }
        razorpayClient.testConnection();
    }
}
