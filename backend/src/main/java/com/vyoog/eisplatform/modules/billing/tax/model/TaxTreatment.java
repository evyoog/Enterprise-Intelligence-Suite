package com.vyoog.eisplatform.modules.billing.tax.model;

/** What a rule does: charge its components, or an explicit non-taxable treatment (BR-16). */
public enum TaxTreatment {
    TAXABLE, EXEMPT, ZERO_RATED, NIL_RATED, REVERSE_CHARGE, OUT_OF_SCOPE
}
