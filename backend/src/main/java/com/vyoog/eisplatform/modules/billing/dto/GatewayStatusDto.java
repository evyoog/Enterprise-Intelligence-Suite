package com.vyoog.eisplatform.modules.billing.dto;

import java.time.Instant;

public record GatewayStatusDto(
    String provider,
    boolean configured,
    boolean liveMode,
    String maskedKeyId,
    boolean keySecretSet,
    boolean webhookSecretSet,
    String webhookUrl,
    Instant lastWebhookReceivedAt,
    String lastWebhookEventType
) {
}
