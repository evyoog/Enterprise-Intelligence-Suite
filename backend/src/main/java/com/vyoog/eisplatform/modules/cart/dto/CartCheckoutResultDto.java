package com.vyoog.eisplatform.modules.cart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** {@code kind} INVOICE (individual: open {@code /checkout?invoiceId=}) or
 * ORDER (organization member: one order per item, REQ-ORD-001). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartCheckoutResultDto(String kind, Long invoiceId, List<Long> orderIds) {
}
