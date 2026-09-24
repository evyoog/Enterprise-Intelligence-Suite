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

    public SmtpEmailService(JavaMailSender mailSender, @Value("${vyoog.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public void sendVerificationEmail(String toEmail, String recipientName, String verificationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Verify your Vyoog account");
        message.setText(
            "Hi " + recipientName + ",\n\n"
                + "Thanks for registering with Vyoog. Verify your email address to continue:\n\n"
                + verificationLink + "\n\n"
                + "This link expires soon and can only be used once. If you didn't request this, you can ignore this email.\n\n"
                + "— Vyoog"
        );
        send(message);
    }

    @Override
    public void sendDuplicateRegistrationNotice(String toEmail, String recipientName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Someone tried to register with your Vyoog email");
        message.setText(
            "Hi " + recipientName + ",\n\n"
                + "Someone just tried to register a new Vyoog account using this email address, "
                + "which already has an account. If this was you, please sign in instead.\n\n"
                + "If you didn't try to register, no action is needed — your account is unaffected.\n\n"
                + "— Vyoog"
        );
        send(message);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Reset your Vyoog password");
        message.setText(
            "Hi,\n\n"
                + "We received a request to reset the password for this Vyoog account. "
                + "If this was you, set a new password here:\n\n"
                + resetLink + "\n\n"
                + "This link expires soon and can only be used once. If you didn't request this, "
                + "you can safely ignore this email — your password will not be changed.\n\n"
                + "— Vyoog"
        );
        send(message);
    }

    @Override
    public void sendPasswordChangedNotice(String toEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Your Vyoog password was changed");
        message.setText(
            "Hi,\n\n"
                + "This is a confirmation that the password for this Vyoog account was just changed, "
                + "and you've been signed out everywhere as a precaution.\n\n"
                + "If you didn't make this change, contact your administrator immediately.\n\n"
                + "— Vyoog"
        );
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
