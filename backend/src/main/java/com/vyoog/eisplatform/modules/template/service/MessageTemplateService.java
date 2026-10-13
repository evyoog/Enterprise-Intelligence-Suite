package com.vyoog.eisplatform.modules.template.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.template.dto.MessageTemplateDtos.*;
import com.vyoog.eisplatform.modules.template.model.MessageTemplate;
import com.vyoog.eisplatform.modules.template.repository.MessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 15.01.02 Manage templates (REQ-GOV-002, C92, TM-1 A): admin-editable wording of EIS's emails and notifications, one
 * per message type and language, with {placeholders}. {@link #render} never fails: it uses the administrator's text, else
 * the built-in text of the language, else the English text, and a missing value becomes an empty string.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageTemplateService {

    private static final Logger log = LoggerFactory.getLogger(MessageTemplateService.class);
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([A-Za-z][A-Za-z0-9]*)}");
    public static final int MAX_SUBJECT = 200;
    public static final int MAX_BODY = 5000;

    private final MessageTemplateRepository repository;
    private final AuditService auditService;

    // ------------------------------------------------------------------ rendering (used by the senders)

    public record Rendered(String subject, String body) {
    }

    /** The text of one message in one language with the values filled in. */
    public Rendered render(String code, String language, Map<String, String> values) {
        MessageCatalog.Text text = effective(code, language).text();
        return new Rendered(fill(text.subject(), values), fill(text.body(), values));
    }

    private record Effective(MessageCatalog.Text text, boolean customized) {
    }

    private Effective effective(String code, String language) {
        MessageCatalog.Type type = MessageCatalog.find(code).orElseThrow(() -> new IllegalArgumentException("Unknown message type: " + code));
        String lang = MessageCatalog.LANGUAGES.contains(language) ? language : MessageCatalog.DEFAULT_LANGUAGE;
        Optional<MessageTemplate> own = lookup(code, lang);
        if (own.isPresent()) {
            return new Effective(new MessageCatalog.Text(own.get().getSubject(), own.get().getBody()), true);
        }
        if (type.defaults().containsKey(lang)) {
            return new Effective(type.defaults().get(lang), false);
        }
        Optional<MessageTemplate> english = lookup(code, MessageCatalog.DEFAULT_LANGUAGE);
        return english.map(t -> new Effective(new MessageCatalog.Text(t.getSubject(), t.getBody()), true))
            .orElseGet(() -> new Effective(type.defaults().get(MessageCatalog.DEFAULT_LANGUAGE), false));
    }

    private Optional<MessageTemplate> lookup(String code, String language) {
        try {
            return repository.findByCodeAndLanguage(code, language);
        } catch (RuntimeException e) {
            log.warn("Could not read the template {} ({}): {}; the built-in text is used", code, language, e.getMessage());
            return Optional.empty();
        }
    }

    static String fill(String template, Map<String, String> values) {
        Matcher m = PLACEHOLDER.matcher(template);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String value = values == null ? null : values.get(m.group(1));
            m.appendReplacement(out, Matcher.quoteReplacement(value == null ? "" : value));
        }
        m.appendTail(out);
        return out.toString();
    }

    // ------------------------------------------------------------------ administration

    public List<TemplateTypeDto> listTypes() {
        return MessageCatalog.TYPES.stream().map(this::toDto).toList();
    }

    public TemplateTypeDto getType(String code) {
        return toDto(type(code));
    }

    @Transactional
    public TemplateTypeDto save(String code, String language, SaveTemplateRequest request, String actorKeycloakSub) {
        MessageCatalog.Type type = type(code);
        String lang = language(language);
        String subject = request.subject() == null ? "" : request.subject().trim();
        String body = request.body() == null ? "" : request.body().strip();
        validate(type, subject, body);
        MessageTemplate row = repository.findByCodeAndLanguage(type.code(), lang).orElseGet(MessageTemplate::new);
        row.setCode(type.code());
        row.setLanguage(lang);
        row.setSubject(subject);
        row.setBody(body);
        row.setUpdatedBy(actorKeycloakSub);
        row.setUpdatedAt(Instant.now());
        repository.save(row);
        auditService.recordSuccess("MESSAGE_TEMPLATE_CHANGED", actorKeycloakSub, null, null, "MessageTemplate", type.code() + ":" + lang, null,
            "Template " + type.code() + " (" + lang + ") changed");
        return toDto(type);
    }

    @Transactional
    public TemplateTypeDto reset(String code, String language, String actorKeycloakSub) {
        MessageCatalog.Type type = type(code);
        String lang = language(language);
        repository.findByCodeAndLanguage(type.code(), lang).ifPresent(row -> {
            repository.delete(row);
            auditService.recordSuccess("MESSAGE_TEMPLATE_RESET", actorKeycloakSub, null, null, "MessageTemplate", type.code() + ":" + lang, null,
                "Template " + type.code() + " (" + lang + ") reset to the built-in text");
        });
        return toDto(type);
    }

    /** What the text would look like with example values; the text is validated but not saved. */
    public PreviewDto preview(String code, String language, SaveTemplateRequest request) {
        MessageCatalog.Type type = type(code);
        language(language);
        String subject = request.subject() == null ? "" : request.subject().trim();
        String body = request.body() == null ? "" : request.body().strip();
        validate(type, subject, body);
        Map<String, String> examples = new HashMap<>();
        type.placeholders().forEach(p -> examples.put(p.name(), p.example()));
        return new PreviewDto(fill(subject, examples), fill(body, examples));
    }

    private void validate(MessageCatalog.Type type, String subject, String body) {
        if (subject.isEmpty() || subject.length() > MAX_SUBJECT) {
            throw new IllegalArgumentException("The subject must be 1 to " + MAX_SUBJECT + " characters.");
        }
        if (body.isEmpty() || body.length() > MAX_BODY) {
            throw new IllegalArgumentException("The text must be 1 to " + MAX_BODY + " characters.");
        }
        Set<String> used = new LinkedHashSet<>();
        for (String text : List.of(subject, body)) {
            Matcher m = PLACEHOLDER.matcher(text);
            while (m.find()) {
                used.add(m.group(1));
            }
        }
        for (String name : used) {
            if (type.placeholder(name).isEmpty()) {
                throw new IllegalArgumentException("{" + name + "} is not available in this message. You can use: "
                    + String.join(", ", type.placeholders().stream().map(p -> "{" + p.name() + "}").toList()) + ".");
            }
        }
        for (MessageCatalog.Placeholder p : type.placeholders()) {
            if (p.required() && !used.contains(p.name())) {
                throw new IllegalArgumentException("The text must contain {" + p.name() + "} (" + p.description() + ").");
            }
        }
    }

    private MessageCatalog.Type type(String code) {
        return MessageCatalog.find(code).orElseThrow(() -> new ResourceNotFoundException("Message type not found"));
    }

    private static String language(String language) {
        if (language == null || !MessageCatalog.LANGUAGES.contains(language)) {
            throw new IllegalArgumentException("Language must be one of " + MessageCatalog.LANGUAGES + ".");
        }
        return language;
    }

    private TemplateTypeDto toDto(MessageCatalog.Type type) {
        Map<String, MessageTemplate> own = new HashMap<>();
        repository.findByCode(type.code()).forEach(t -> own.put(t.getLanguage(), t));
        List<TemplateVariantDto> variants = new ArrayList<>();
        for (String lang : MessageCatalog.LANGUAGES) {
            MessageCatalog.Text builtIn = type.defaults().getOrDefault(lang, type.defaults().get(MessageCatalog.DEFAULT_LANGUAGE));
            MessageTemplate t = own.get(lang);
            variants.add(new TemplateVariantDto(lang, t != null, t != null ? t.getSubject() : builtIn.subject(),
                t != null ? t.getBody() : builtIn.body(), builtIn.subject(), builtIn.body(), t == null ? null : t.getUpdatedAt()));
        }
        return new TemplateTypeDto(type.code(), type.name(), type.description(), type.channel().name(),
            type.placeholders().stream().map(p -> new PlaceholderDto(p.name(), p.description(), p.example(), p.required())).toList(), variants);
    }
}
