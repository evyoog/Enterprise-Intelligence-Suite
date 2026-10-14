package com.vyoog.eisplatform.modules.billing.service.gateway;

import org.springframework.stereotype.Component;

import java.util.List;

import static com.vyoog.eisplatform.modules.billing.service.gateway.GatewayDescriptor.Implementation.PLANNED;

/** C98: providers EIS can be extended to, listed with what they will need but with NO adapter. They are shown as "integration planned" and can
 * not be configured, tested or activated, so none is ever presented as operational. When an adapter is written, delete its entry here. */
@Component
public class PlannedProviders {

    private static final List<String> TX = List.of("INVOICE_PAYMENT");

    public List<GatewayDescriptor> all() {
        return List.of(
            new GatewayDescriptor("STRIPE", "Stripe", "Cards and local payment methods worldwide.", "stripe",
                List.of("US", "GB", "IN", "EU"), List.of("USD", "EUR", "GBP", "INR"), List.of("card", "wallet", "bank_transfer"), TX,
                List.of("TEST", "LIVE"), true, true, true, PLANNED,
                List.of(ConfigField.of("publishableKey", "Publishable key", false, true, "Starts with pk_test_ or pk_live_."),
                    ConfigField.of("secretKey", "Secret key", true, true, "Starts with sk_test_ or sk_live_."),
                    ConfigField.webhookSecret("webhookSecret", "Webhook signing secret", true, "Starts with whsec_.")),
                List.of(), List.of()),
            new GatewayDescriptor("PAYPAL", "PayPal", "PayPal balance, cards and local methods.", "paypal",
                List.of("US", "GB", "IN", "EU"), List.of("USD", "EUR", "GBP"), List.of("paypal", "card"), TX,
                List.of("TEST", "LIVE"), true, true, true, PLANNED,
                List.of(ConfigField.of("clientId", "Client ID", false, true, null),
                    ConfigField.of("clientSecret", "Client secret", true, true, null),
                    ConfigField.webhookSecret("webhookId", "Webhook ID", true, "The ID PayPal shows for the registered webhook.")),
                List.of(), List.of()),
            new GatewayDescriptor("PAYU", "PayU", "Cards, UPI and netbanking in India.", "payu",
                List.of("IN"), List.of("INR"), List.of("card", "upi", "netbanking", "wallet"), TX,
                List.of("TEST", "LIVE"), true, true, false, PLANNED,
                List.of(ConfigField.of("merchantKey", "Merchant key", false, true, null),
                    ConfigField.of("merchantSalt", "Merchant salt", true, true, null)),
                List.of(), List.of()),
            new GatewayDescriptor("CASHFREE", "Cashfree Payments", "Cards, UPI and netbanking in India.", "cashfree",
                List.of("IN"), List.of("INR"), List.of("card", "upi", "netbanking", "wallet"), TX,
                List.of("TEST", "LIVE"), true, true, true, PLANNED,
                List.of(ConfigField.of("appId", "App ID", false, true, null),
                    ConfigField.of("secretKey", "Secret key", true, true, null)),
                List.of(), List.of())
        );
    }
}
