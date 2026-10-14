package com.vyoog.eisplatform.modules.billing.tax.calc;

/** Why a calculation could not be completed safely (BR-18, BR-31, BR-33, BR-34). The invoice stays a Draft with this code. */
public enum BlockCode {
    NO_MATCHING_RULE("No approved, active, in-force tax rule matches this transaction."),
    AMBIGUOUS_RULES("More than one tax rule matches this transaction equally well."),
    MISSING_CUSTOMER_COUNTRY("The customer's billing country is missing or not recognised."),
    MISSING_SUPPLIER_PROFILE("The business profile in Billing settings has no recognised country."),
    MISSING_TAX_CLASS("A product on the invoice has no tax class."),
    PROVIDER_NOT_CONFIGURED("The external tax provider is not configured on the backend."),
    PROVIDER_NOT_VALIDATED("The external tax provider has not passed validation."),
    PROVIDER_UNSUPPORTED("The external tax provider does not support this country, currency or transaction type."),
    PROVIDER_UNAVAILABLE("The external tax provider could not be reached or kept failing."),
    PROVIDER_REJECTED("The external tax provider rejected the request."),
    PROVIDER_INVALID_RESPONSE("The external tax provider returned a calculation that failed validation."),
    ENGINE_ERROR("The tax calculation failed unexpectedly.");

    private final String defaultMessage;

    BlockCode(String defaultMessage) {
        this.defaultMessage = defaultMessage;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
