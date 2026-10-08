package com.vyoog.eisplatform.modules.registration.service;

/** No email-sending capability existed anywhere in this backend before this
 * feature — this is the first. Kept as a narrow interface (not just a
 * concrete class) so a future provider swap (e.g. a transactional email API
 * instead of raw SMTP) only touches SmtpEmailService, not its callers. */
public interface EmailService {

    void sendVerificationEmail(String toEmail, String recipientName, String verificationLink);

    /** Sent instead of a fresh verification link when someone tries to
     * register with an email that's already in use — never revealed via the
     * HTTP response itself (see RegistrationService), only through the
     * inbox only the real owner can read. */
    void sendDuplicateRegistrationNotice(String toEmail, String recipientName);

    /** Phase 8: the only outward sign a "forgot password" request ever
     * produces — see PasswordResetService's own anti-enumeration handling
     * for why an email with no matching Keycloak account gets no email at
     * all here (the HTTP response is identical either way; there's simply
     * no owner to notify when the account doesn't exist). */
    void sendPasswordResetEmail(String toEmail, String resetLink);

    /** Sent after a password reset actually succeeds — a real, completed
     * change, not the request itself (see sendPasswordResetEmail). */
    void sendPasswordChangedNotice(String toEmail);

    /** Phase 18: the email side of a Notification — unlike the other methods
     * here, subject/body are supplied by the caller (NotificationService)
     * rather than templated per call site, since notification content
     * varies per category in a way a dedicated method per type doesn't
     * scale to. */
    void sendNotificationEmail(String toEmail, String subject, String message);

    /** REQ-TEN-008: the invitation to join an organization. Returns false when the message could not be
     * handed to the mail server, so the caller can tell the inviter to resend. */
    boolean sendInvitationEmail(String toEmail, String organizationName, String inviterName, String roleLabel,
                                String structurePlacement, java.time.Instant expiresAt, String invitationLink);
}
