package com.vyoog.eisplatform.modules.billing.service.gateway;

/** A provider's payment, in EIS's own status model. Display fields only, never card numbers (BR-BIL-001). */
public record GatewayPaymentInfo(Status status, String methodType, String methodNetwork, String methodLast4, String failureReason) {
    public enum Status { CAPTURED, FAILED }
}
