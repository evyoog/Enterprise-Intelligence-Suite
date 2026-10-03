package com.vyoog.eisplatform.modules.integration.controller;

import com.vyoog.eisplatform.modules.integration.dto.OutboxEventDetailDto;
import com.vyoog.eisplatform.modules.integration.dto.OutboxEventPageDto;
import com.vyoog.eisplatform.modules.integration.service.AdminEventService;
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
}
