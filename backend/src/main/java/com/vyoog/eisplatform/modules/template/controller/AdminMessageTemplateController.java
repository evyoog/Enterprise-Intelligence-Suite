package com.vyoog.eisplatform.modules.template.controller;

import com.vyoog.eisplatform.modules.template.dto.MessageTemplateDtos.*;
import com.vyoog.eisplatform.modules.template.service.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** REQ-GOV-002 (C92, TM-1 A): message templates for platform administrators. Gated by
 * {@code MANAGE_PLATFORM_SETTINGS} through the {@code /admin/platform-settings/**} rule in SecurityConfig. */
@RestController
@RequestMapping("/admin/platform-settings/message-templates")
@RequiredArgsConstructor
public class AdminMessageTemplateController {

    private final MessageTemplateService service;

    @GetMapping
    public List<TemplateTypeDto> list() {
        return service.listTypes();
    }

    @GetMapping("/{code}")
    public TemplateTypeDto get(@PathVariable String code) {
        return service.getType(code);
    }

    @PutMapping("/{code}/{language}")
    public TemplateTypeDto save(@PathVariable String code, @PathVariable String language, @RequestBody SaveTemplateRequest request,
                                @AuthenticationPrincipal Jwt jwt) {
        return service.save(code, language, request, jwt.getSubject());
    }

    @DeleteMapping("/{code}/{language}")
    public TemplateTypeDto reset(@PathVariable String code, @PathVariable String language, @AuthenticationPrincipal Jwt jwt) {
        return service.reset(code, language, jwt.getSubject());
    }

    @PostMapping("/{code}/{language}/preview")
    public PreviewDto preview(@PathVariable String code, @PathVariable String language, @RequestBody SaveTemplateRequest request) {
        return service.preview(code, language, request);
    }
}
