package com.vyoog.eisplatform.modules.template.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The message types EIS sends through templates (REQ-GOV-002, C92, TM-1 A): code, who it is for, the placeholders an
 * administrator may use (some required, so a password-reset email can never lose its link) and the built-in text in each
 * language. The English text is exactly what the code sent before templates existed. An administrator's edit is stored
 * in {@code message_template}; without one, this text is used.
 */
public final class MessageCatalog {

    public static final List<String> LANGUAGES = List.of("en", "es");
    public static final String DEFAULT_LANGUAGE = "en";

    public enum Channel { EMAIL, NOTIFICATION }

    public record Placeholder(String name, String description, String example, boolean required) {
    }

    public record Text(String subject, String body) {
    }

    public record Type(String code, String name, String description, Channel channel, List<Placeholder> placeholders,
                       Map<String, Text> defaults) {
        public Optional<Placeholder> placeholder(String name) {
            return placeholders.stream().filter(p -> p.name().equals(name)).findFirst();
        }
    }

    private static Placeholder p(String name, String description, String example, boolean required) {
        return new Placeholder(name, description, example, required);
    }

    private static Map<String, Text> texts(String enSubject, String enBody, String esSubject, String esBody) {
        return Map.of("en", new Text(enSubject, enBody), "es", new Text(esSubject, esBody));
    }

    public static final List<Type> TYPES = List.of(
        new Type("EMAIL_VERIFY", "Verify your email", "Sent after registering, with the link that confirms the email address.",
            Channel.EMAIL, List.of(p("name", "The person's first name", "Asha", false), p("link", "The verification link", "https://example.com/verify?token=…", true)),
            texts("Verify your Vyoog account",
                "Hi {name},\n\nThanks for registering with Vyoog. Verify your email address to continue:\n\n{link}\n\nThis link expires soon and can only be used once. If you didn't request this, you can ignore this email.\n\n— Vyoog",
                "Verifique su cuenta de Vyoog",
                "Hola {name}:\n\nGracias por registrarse en Vyoog. Verifique su correo electrónico para continuar:\n\n{link}\n\nEste enlace caduca pronto y solo puede usarse una vez. Si no lo solicitó, puede ignorar este correo.\n\n— Vyoog")),
        new Type("EMAIL_DUPLICATE_REGISTRATION", "Registration with an existing email", "Sent when someone tries to register with an email address that already has an account.",
            Channel.EMAIL, List.of(p("name", "The name given in the registration", "Acme Ltd", false)),
            texts("Someone tried to register with your Vyoog email",
                "Hi {name},\n\nSomeone just tried to register a new Vyoog account using this email address, which already has an account. If this was you, please sign in instead.\n\nIf you didn't try to register, no action is needed — your account is unaffected.\n\n— Vyoog",
                "Alguien intentó registrarse con su correo de Vyoog",
                "Hola {name}:\n\nAlguien acaba de intentar registrar una cuenta nueva de Vyoog con este correo, que ya tiene una cuenta. Si fue usted, inicie sesión.\n\nSi no intentó registrarse, no hace falta hacer nada: su cuenta no se ve afectada.\n\n— Vyoog")),
        new Type("EMAIL_PASSWORD_RESET", "Reset your password", "Sent when someone asks to reset a password, with the one-time link.",
            Channel.EMAIL, List.of(p("link", "The reset link", "https://example.com/reset-password?token=…", true)),
            texts("Reset your Vyoog password",
                "Hi,\n\nWe received a request to reset the password for this Vyoog account. If this was you, set a new password here:\n\n{link}\n\nThis link expires soon and can only be used once. If you didn't request this, you can safely ignore this email — your password will not be changed.\n\n— Vyoog",
                "Restablezca su contraseña de Vyoog",
                "Hola:\n\nRecibimos una solicitud para restablecer la contraseña de esta cuenta de Vyoog. Si fue usted, defina una contraseña nueva aquí:\n\n{link}\n\nEste enlace caduca pronto y solo puede usarse una vez. Si no lo solicitó, ignore este correo: su contraseña no cambiará.\n\n— Vyoog")),
        new Type("EMAIL_PASSWORD_CHANGED", "Password changed", "Sent after a password was changed.",
            Channel.EMAIL, List.of(),
            texts("Your Vyoog password was changed",
                "Hi,\n\nThis is a confirmation that the password for this Vyoog account was just changed, and you've been signed out everywhere as a precaution.\n\nIf you didn't make this change, contact your administrator immediately.\n\n— Vyoog",
                "Su contraseña de Vyoog cambió",
                "Hola:\n\nLe confirmamos que la contraseña de esta cuenta de Vyoog acaba de cambiar y se cerró su sesión en todas partes como precaución.\n\nSi no hizo este cambio, contacte de inmediato con su administrador.\n\n— Vyoog")),
        new Type("EMAIL_INVITATION", "Invitation to join an organization", "Sent when an administrator invites someone to an organization.",
            Channel.EMAIL, List.of(p("organization", "The organization's name", "Acme Ltd", false), p("inviter", "Who sent the invitation", "Ravi Kumar", false),
                p("details", "Role and placement lines", "Role: Member", false), p("expires", "When the link expires", "20 Oct 2026", false),
                p("link", "The invitation link", "https://example.com/invitations/…", true)),
            texts("You have been invited to join {organization}",
                "You have been invited to join {organization} on Vyoog.\n\nInvited by: {inviter}\n{details}\nThis invitation expires on {expires} (UTC).\n\nAccept the invitation:\n{link}\n\nIf you were not expecting this, you can ignore this email.\n\n— Vyoog",
                "Le han invitado a unirse a {organization}",
                "Le han invitado a unirse a {organization} en Vyoog.\n\nInvitado por: {inviter}\n{details}\nEsta invitación caduca el {expires} (UTC).\n\nAcepte la invitación:\n{link}\n\nSi no esperaba este mensaje, puede ignorar este correo.\n\n— Vyoog")),
        new Type("NOTIF_ACCESS_CHANGED", "Your access changed", "Tells a member that an administrator changed what they can open or do.",
            Channel.NOTIFICATION, List.of(p("changes", "What changed, one sentence per change", "You can now open Valam.ai.", true)),
            texts("Your access changed", "{changes}", "Su acceso cambió", "{changes}")),
        new Type("NOTIF_ORG_ROLE_CHANGED", "Organization role changed", "Tells a member their role in the organization changed.",
            Channel.NOTIFICATION, List.of(p("role", "The new role", "MEMBER", true)),
            texts("Your organization role changed", "Your role in your organization is now {role}.", "Su rol en la organización cambió", "Su rol en la organización ahora es {role}.")),
        new Type("NOTIF_MEMBERSHIP_CHANGED", "Membership status changed", "Tells a member they were suspended, reactivated or removed.",
            Channel.NOTIFICATION, List.of(p("status", "The new status", "SUSPENDED", true)),
            texts("Your organization membership changed", "Your membership status is now {status}.", "Su pertenencia a la organización cambió", "El estado de su pertenencia ahora es {status}.")),
        new Type("NOTIF_PRODUCT_ACCESS_GRANTED", "Product access given", "Tells a member an administrator gave them access to a product.",
            Channel.NOTIFICATION, List.of(p("product", "The product's name", "Valam.ai", true), p("role", "The product role", "USER", false)),
            texts("Access granted: {product}", "You've been given {role} access to {product}.", "Acceso concedido: {product}", "Se le ha dado acceso {role} a {product}.")),
        new Type("NOTIF_PROJECT_ACCESS_GRANTED", "Product access through a project", "Tells a member they gained a product through a project.",
            Channel.NOTIFICATION, List.of(p("product", "The product's name", "Valam.ai", true), p("role", "The product role", "USER", false), p("project", "The project's name", "Mumbai rollout", false)),
            texts("Access granted: {product}", "You have {role} access to {product} through the project {project}.",
                "Acceso concedido: {product}", "Tiene acceso {role} a {product} a través del proyecto {project}.")),
        new Type("NOTIF_INVITATION_ACCEPTED", "Invitation accepted", "Tells the inviter that the person accepted.",
            Channel.NOTIFICATION, List.of(p("email", "The invited person's email", "asha@example.com", false), p("organization", "The organization's name", "Acme Ltd", false), p("role", "The role they joined with", "a member", false)),
            texts("Invitation accepted", "{email} accepted your invitation and joined {organization} as {role}.",
                "Invitación aceptada", "{email} aceptó su invitación y se unió a {organization} como {role}.")),
        new Type("NOTIF_INVITATION_DECLINED", "Invitation declined", "Tells the inviter that the person declined.",
            Channel.NOTIFICATION, List.of(p("email", "The invited person's email", "asha@example.com", false), p("organization", "The organization's name", "Acme Ltd", false)),
            texts("Invitation declined", "{email} declined your invitation to join {organization}.",
                "Invitación rechazada", "{email} rechazó su invitación para unirse a {organization}.")),
        new Type("NOTIF_EVENT_FAILED", "Platform event failed", "Tells the people on the event settings screen that an event failed for good.",
            Channel.NOTIFICATION, List.of(p("eventType", "The event type", "SubscriptionCreated", true), p("aggregate", "What it is about", "Subscription 42", false),
                p("attempts", "How many attempts were made", "10", false), p("error", "The last error", "provisioning: timeout", false)),
            texts("Event failed: {eventType}", "The event {eventType} ({aggregate}) could not be delivered after {attempts} attempts and is now FAILED. Last error: {error} Open Platform events to retry it.",
                "Evento fallido: {eventType}", "No se pudo entregar el evento {eventType} ({aggregate}) tras {attempts} intentos y ahora está FALLIDO. Último error: {error} Abra Eventos de la plataforma para reintentarlo.")),
        new Type("NOTIF_RENEWAL_REMINDER", "Subscription renewal reminder", "Reminds a person that a subscription renews soon.",
            Channel.NOTIFICATION, List.of(p("product", "The product's name", "Valam.ai", true), p("plan", "The plan, in brackets", " (Standard)", false), p("date", "The renewal date", "2026-11-01", true),
                p("amount", "The amount, with a leading 'for'", " for 4999 INR", false), p("how", "How the renewal is paid", "Your saved payment method will be charged.", false),
                p("manageUrl", "Link to the subscriptions page", "https://example.com/my/subscriptions", false), p("billingUrl", "Link to billing", "https://example.com/billing", false),
                p("preferencesUrl", "Link to the reminder preferences", "https://example.com/account/preferences", false)),
            texts("Your {product} subscription renews on {date}",
                "Your {product}{plan} subscription renews on {date}{amount}. {how}\n\nManage your subscription: {manageUrl}\nPay or update your payment method: {billingUrl}\nChange or turn off these reminders: {preferencesUrl}",
                "Su suscripción a {product} se renueva el {date}",
                "Su suscripción a {product}{plan} se renueva el {date}{amount}. {how}\n\nGestione su suscripción: {manageUrl}\nPague o actualice su método de pago: {billingUrl}\nCambie o desactive estos recordatorios: {preferencesUrl}")));

    private MessageCatalog() {
    }

    public static Optional<Type> find(String code) {
        return TYPES.stream().filter(t -> t.code().equals(code)).findFirst();
    }
}
