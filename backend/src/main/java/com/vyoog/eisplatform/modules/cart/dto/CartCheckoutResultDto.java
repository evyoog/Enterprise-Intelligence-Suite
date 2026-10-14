package com.vyoog.eisplatform.modules.cart.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/** {@code kind} INVOICE (individual: open {@code /checkout?invoiceId=}) or
 * ORDER (organization member: one multi-item purchase request, REQ-ORD-001, C100). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CartCheckoutResultDto(String kind, Long invoiceId, List<Long> orderIds) {
}
