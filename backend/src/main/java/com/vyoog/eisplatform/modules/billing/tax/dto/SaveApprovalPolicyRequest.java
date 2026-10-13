package com.vyoog.eisplatform.modules.billing.tax.dto;

import jakarta.validation.constraints.NotNull;

public record SaveApprovalPolicyRequest(@NotNull Boolean allowSelfApproval) {
}
