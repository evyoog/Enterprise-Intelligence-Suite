package com.vyoog.eisplatform.modules.integration.controller;

import com.vyoog.eisplatform.modules.integration.dto.AdminApiKeyPageDto;
import com.vyoog.eisplatform.modules.integration.service.ApiKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REQ-INT-001.4: per-key usage for administrators ({@code MANAGE_INTEGRATIONS}). */
@RestController
@RequestMapping("/admin/api-keys")
@RequiredArgsConstructor
public class AdminApiKeyController {

    private final ApiKeyService apiKeyService;

    @GetMapping
    public AdminApiKeyPageDto list(@RequestParam(defaultValue = "0") int page) {
        return apiKeyService.adminList(page);
    }
}
