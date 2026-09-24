package com.vyoog.eisplatform.modules.auth.service;

import com.vyoog.eisplatform.modules.registration.service.EmailService;

/** Captures the last password-reset link/notice instead of sending real
 * email — see FakeKeycloakAdminClient's own javadoc for why this suite never
 * makes live outbound calls of any kind. */
public class FakeEmailService implements EmailService {

    public String lastResetEmail;
    public String lastResetLink;
    public String lastChangedNoticeEmail;
    public String lastNotificationEmail;
    public String lastNotificationSubject;
    public String lastNotificationMessage;

    @Override
    public void sendVerificationEmail(String toEmail, String recipientName, String verificationLink) {
    }

    @Override
    public void sendDuplicateRegistrationNotice(String toEmail, String recipientName) {
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        this.lastResetEmail = toEmail;
        this.lastResetLink = resetLink;
    }

    @Override
    public void sendPasswordChangedNotice(String toEmail) {
        this.lastChangedNoticeEmail = toEmail;
    }

    @Override
    public void sendNotificationEmail(String toEmail, String subject, String message) {
        this.lastNotificationEmail = toEmail;
        this.lastNotificationSubject = subject;
        this.lastNotificationMessage = message;
    }

    /** Same reasoning as FakeKeycloakAdminClient#reset. */
    public void reset() {
        lastResetEmail = null;
        lastResetLink = null;
        lastChangedNoticeEmail = null;
        lastNotificationEmail = null;
        lastNotificationSubject = null;
        lastNotificationMessage = null;
    }
}
