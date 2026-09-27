package com.vyoog.eisplatform.modules.registration.dto;

/** 07.01.02 Change plan (sprint 2026.4.3). Null clears the plan (back to
 * the product's flat price). */
public record ChangePlanRequest(Long planId) {
}
