package com.vyoog.eisplatform.modules.integration.dto;

/** How many events are waiting or failed for good, for the banner on the events screen. */
public record EventSummaryCountsDto(long pending, long failed) {
}
