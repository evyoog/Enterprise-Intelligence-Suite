package com.vyoog.eisplatform.modules.billing.dto;

import java.util.List;

/** C97 BI-8: one gateway in the admin "Payment gateways" list. */
public record PaymentGatewayDto(String code, String name, boolean configured, boolean active, List<String> methods, GatewayStatusDto status) {
}
