package com.vyoog.eisplatform.modules.cart.dto;

import java.util.List;

public record CartValidationDto(boolean valid, List<CartIssueDto> issues) {
}
