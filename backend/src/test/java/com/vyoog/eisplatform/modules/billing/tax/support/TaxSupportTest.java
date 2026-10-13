package com.vyoog.eisplatform.modules.billing.tax.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** REQ-BIL-002: country and state resolution and the tax ID format check (TX-4) are plain logic with no database. */
class TaxSupportTest {

    @Test
    void countriesAreResolvedFromNamesAndCodes() {
        assertThat(CountryResolver.resolve("India")).contains("IN");
        assertThat(CountryResolver.resolve("in")).contains("IN");
        assertThat(CountryResolver.resolve("IND")).contains("IN");
        assertThat(CountryResolver.resolve(" united states ")).contains("US");
        assertThat(CountryResolver.resolve("USA")).contains("US");
        assertThat(CountryResolver.resolve("U.K.")).contains("GB");
        assertThat(CountryResolver.resolve("Atlantis")).isEmpty();
        assertThat(CountryResolver.resolve("  ")).isEmpty();
        assertThat(CountryResolver.resolve(null)).isEmpty();
    }

    @Test
    void indianStatesAreResolvedFromNamesCodesAndGstCodes() {
        assertThat(StateCatalog.normalize("IN", "Tamil Nadu")).contains("TN");
        assertThat(StateCatalog.normalize("IN", "tamilnadu")).contains("TN");
        assertThat(StateCatalog.normalize("IN", "TN")).contains("TN");
        assertThat(StateCatalog.normalize("IN", "33")).contains("TN");
        assertThat(StateCatalog.normalize("IN", "Orissa")).contains("OD");
        assertThat(StateCatalog.normalize("IN", "Delhi NCT")).contains("DL");
        assertThat(StateCatalog.normalize("IN", "Jammu & Kashmir")).contains("JK");
        // A state of a catalog country that is not recognised is NOT guessed.
        assertThat(StateCatalog.normalize("IN", "Narnia")).isEmpty();
        assertThat(StateCatalog.statesOf("IN")).hasSize(36);
    }

    @Test
    void otherCountriesCompareTheCleanedTextOrTheCatalogCode() {
        assertThat(StateCatalog.normalize("US", "California")).contains("CA");
        assertThat(StateCatalog.normalize("US", "ca")).contains("CA");
        assertThat(StateCatalog.normalize("GB", "Greater London")).contains("GREATERLONDON");
        assertThat(StateCatalog.normalize("GB", "  ")).isEmpty();
    }

    @Test
    void anIndianTaxIdMustBeAValidGstin() {
        assertThat(TaxIdValidator.check("IN", "33ABCDE1234F1Z5")).isEmpty();
        assertThat(TaxIdValidator.check("IN", " 33abcde1234f1z5 ")).isEmpty();
        assertThat(TaxIdValidator.check("IN", "GSTIN123")).isPresent();
        assertThat(TaxIdValidator.check("IN", "33ABCDE1234F1Z")).isPresent();          // 14 characters
        assertThat(TaxIdValidator.check("IN", "99ABCDE1234F1Z5")).isPresent();         // 99 is not a state code
        assertThat(TaxIdValidator.check("IN", null)).isEmpty();
        assertThat(TaxIdValidator.check("IN", "  ")).isEmpty();
        assertThat(TaxIdValidator.gstinState("29ABCDE1234F1Z5")).get().extracting(StateCatalog.State::code).isEqualTo("KA");
        assertThat(TaxIdValidator.isValidGstin("33ABCDE1234F1Z5")).isTrue();
        assertThat(TaxIdValidator.isValidGstin("nope")).isFalse();
    }

    @Test
    void otherCountriesAcceptFreeTextTaxIds() {
        assertThat(TaxIdValidator.check("DE", "DE 123456789")).isEmpty();
        assertThat(TaxIdValidator.check("GB", "GB123 4567 89")).isEmpty();
        assertThat(TaxIdValidator.check("US", "12-3456789")).isEmpty();
        assertThat(TaxIdValidator.check("US", "bad<script>")).isPresent();
        assertThat(TaxIdValidator.check("US", "x".repeat(51))).isPresent();
    }
}
