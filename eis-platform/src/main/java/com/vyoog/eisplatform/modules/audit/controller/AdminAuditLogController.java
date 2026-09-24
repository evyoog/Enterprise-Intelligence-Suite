package com.vyoog.eisplatform.modules.audit.controller;

import com.vyoog.eisplatform.modules.audit.dto.AuditLogPageDto;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/** ADMIN-only (VIEW_AUDIT_LOG) — enforced by SecurityConfig, the real
 * security boundary, same as every other admin-only endpoint in this app. */
@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

    private final AuditService auditService;

    @GetMapping
    public AuditLogPageDto search(
            @RequestParam(required = false) Long organizationId,
            @RequestParam(required = false) Long actorCustomerId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return auditService.search(organizationId, actorCustomerId, action, from, to, page, size);
    }
}
