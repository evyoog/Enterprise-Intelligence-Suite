package com.vyoog.eisplatform.modules.integration.controller;

import com.vyoog.eisplatform.modules.integration.dto.EventPlatformSettingsDto;
import com.vyoog.eisplatform.modules.integration.dto.EventSummaryCountsDto;
import com.vyoog.eisplatform.modules.integration.dto.OutboxEventDetailDto;
import com.vyoog.eisplatform.modules.integration.dto.OutboxEventPageDto;
import com.vyoog.eisplatform.modules.integration.dto.ReplayRequest;
import com.vyoog.eisplatform.modules.integration.dto.ReplayResultDto;
import com.vyoog.eisplatform.modules.integration.dto.UpdateEventPlatformSettingsRequest;
import com.vyoog.eisplatform.modules.integration.service.AdminEventService;
import com.vyoog.eisplatform.modules.integration.service.EventPlatformSettingsService;
import com.vyoog.eisplatform.modules.integration.service.EventReplayService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/** REQ-INT-002.6: platform events for administrators. Gated by
 * {@code MANAGE_INTEGRATIONS} in SecurityConfig (C62). */
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {

    private final AdminEventService adminEventService;
    private final EventReplayService eventReplayService;
    private final EventPlatformSettingsService settingsService;

    @GetMapping
    public OutboxEventPageDto list(@RequestParam(required = false) String type,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                   @RequestParam(defaultValue = "0") int page) {
        return adminEventService.list(type, status, from, to, page);
    }

    @GetMapping("/types")
    public List<String> types() {
        return adminEventService.eventTypes();
    }

    @GetMapping("/{id}")
    public OutboxEventDetailDto get(@PathVariable Long id) {
        return adminEventService.get(id);
    }

    @PostMapping("/{id}/retry")
    public OutboxEventDetailDto retry(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return adminEventService.retry(id, jwt.getSubject());
    }

    /** REQ-INT-002 (C91): how many events wait or failed for good — the banner on the events screen. */
    @GetMapping("/summary")
    public EventSummaryCountsDto summary() {
        return adminEventService.counts();
    }

    /** REQ-INT-002.9 (EV-1 B): the receivers a replay can target. */
    @GetMapping("/receivers")
    public List<String> receivers() {
        return eventReplayService.receivers();
    }

    /** REQ-INT-002.9: re-send the delivered events of a time range to one receiver ({@code preview} only counts them). */
    @PostMapping("/replay")
    public ReplayResultDto replay(@RequestBody ReplayRequest request, @AuthenticationPrincipal Jwt jwt) {
        return eventReplayService.replay(request, jwt.getSubject());
    }

    /** REQ-INT-002 (EV-3, EV-4): retention and failure alerts. */
    @GetMapping("/settings")
    public EventPlatformSettingsDto settings() {
        return settingsService.get();
    }

    @PutMapping("/settings")
    public EventPlatformSettingsDto updateSettings(@RequestBody UpdateEventPlatformSettingsRequest request, @AuthenticationPrincipal Jwt jwt) {
        return settingsService.update(request, jwt.getSubject());
    }
}
