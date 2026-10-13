package com.vyoog.eisplatform.modules.template.dto;

import java.time.Instant;
import java.util.List;

/** REQ-GOV-002 (C92): shapes of the message template administration API. */
public final class MessageTemplateDtos {

    private MessageTemplateDtos() {
    }

    public record PlaceholderDto(String name, String description, String example, boolean required) {
    }

    /** One language of a message type: the text in use, whether it is the administrator's own, and the built-in text. */
    public record TemplateVariantDto(String language, boolean customized, String subject, String body,
                                     String defaultSubject, String defaultBody, Instant updatedAt) {
    }

    public record TemplateTypeDto(String code, String name, String description, String channel,
                                  List<PlaceholderDto> placeholders, List<TemplateVariantDto> variants) {
    }

    public record SaveTemplateRequest(String subject, String body) {
    }

    public record PreviewDto(String subject, String body) {
    }
}
