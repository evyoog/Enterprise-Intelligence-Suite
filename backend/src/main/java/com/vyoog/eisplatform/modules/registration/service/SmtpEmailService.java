package com.vyoog.eisplatform.modules.registration.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * First email-sending capability in this backend — plain SMTP via Spring
 * Mail, configured entirely through vyoog.mail.* (see application.yml), no
 * credentials hardcoded anywhere. Deliberately plain-text/simple messages;
 * swap in a templating engine later if the content needs to grow richer,
 * without touching the EmailService contract.
 */
@Service
@Slf4j
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final com.vyoog.eisplatform.modules.template.service.MessageTemplateService templateService;
    private final com.vyoog.eisplatform.modules.template.service.MessageLanguageResolver languageResolver;

    public SmtpEmailService(JavaMailSender mailSender, @Value("${vyoog.mail.from}") String fromAddress,
                            com.vyoog.eisplatform.modules.template.service.MessageTemplateService templateService,
                            com.vyoog.eisplatform.modules.template.service.MessageLanguageResolver languageResolver) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.templateService = templateService;
        this.languageResolver = languageResolver;
    }

    /** C92 (TM-1 A): the administrator-editable text of a message type, in the recipient's language. */
    private com.vyoog.eisplatform.modules.template.service.MessageTemplateService.Rendered text(
            String code, String toEmail, java.util.Map<String, String> values) {
        return templateService.render(code, languageResolver.forEmail(toEmail), values);
    }

    @Override
    public void sendVerificationEmail(String toEmail, String recipientName, String verificationLink) {
        var text = text("EMAIL_VERIFY", toEmail, java.util.Map.of("name", String.valueOf(recipientName), "link", verificationLink));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(text.subject());
        message.setText(text.body());
        send(message);
    }

    @Override
    public void sendDuplicateRegistrationNotice(String toEmail, String recipientName) {
        var text = text("EMAIL_DUPLICATE_REGISTRATION", toEmail, java.util.Map.of("name", String.valueOf(recipientName)));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(text.subject());
        message.setText(text.body());
        send(message);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        var text = text("EMAIL_PASSWORD_RESET", toEmail, java.util.Map.of("link", resetLink));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(text.subject());
        message.setText(text.body());
        send(message);
    }

    @Override
    public void sendPasswordChangedNotice(String toEmail) {
        var text = text("EMAIL_PASSWORD_CHANGED", toEmail, java.util.Map.of());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(text.subject());
        message.setText(text.body());
        send(message);
    }

    @Override
    public void sendNotificationEmail(String toEmail, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromAddress);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message + "\n\n— Vyoog");
        send(mailMessage);
    }

    @Override
    public boolean sendInvitationEmail(String toEmail, String organizationName, String inviterName, String roleLabel,
                                       String structurePlacement, java.time.Instant expiresAt, String invitationLink) {
        String expires = java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy").withZone(java.time.ZoneOffset.UTC).format(expiresAt);
        String details = "Role: " + roleLabel + "\n" + (structurePlacement == null ? "" : "Placement: " + structurePlacement + "\n");
        java.util.Map<String, String> values = new java.util.HashMap<>();
        values.put("organization", organizationName);
        values.put("inviter", inviterName);
        values.put("details", details);
        values.put("expires", expires);
        values.put("link", invitationLink);
        var text = text("EMAIL_INVITATION", toEmail, values);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject(text.subject());
        message.setText(text.body());
        try {
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            log.error("Failed to send invitation email to {}: {}", toEmail, e.getMessage());
            return false;
        }
    }

    private void send(SimpleMailMessage message) {
        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Never let an email-delivery failure surface to the caller as a
            // registration failure (SMTP being down shouldn't 500 the whole
            // request) — log it server-side and let the registration itself
            // still succeed; the token still exists for a manual resend.
            log.error("Failed to send email to {}: {}", java.util.Arrays.toString(message.getTo()), e.getMessage());
        }
    }
}
