package com.vyoog.eisplatform.config;

import com.vyoog.eisplatform.modules.billing.tax.service.TaxIntegrationSupport;
import com.vyoog.eisplatform.modules.billing.tax.service.StubTaxProvider;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** REQ-BIL-002 BR-6, AC-21, AC-22: every tax endpoint is MANAGE_BILLING only, and the API never shows a credential. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({TaxTestSupport.class, TaxIntegrationSupport.class, StubTaxProvider.class})
class TaxAuthorizationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TaxIntegrationSupport fixtures;

    @BeforeEach
    void reset() {
        fixtures.resetAll();
    }

    private static final SimpleGrantedAuthority USER = new SimpleGrantedAuthority("ROLE_ACTIVITY_USER");
    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

    @Test
    void everyTaxEndpointNeedsSignInAndTheBillingPermission() throws Exception {
        String[] reads = {"/admin/billing/tax/status", "/admin/billing/tax/rules", "/admin/billing/tax/classes", "/admin/billing/tax/products",
            "/admin/billing/tax/registrations", "/admin/billing/tax/exemptions", "/admin/billing/tax/reference", "/admin/billing/tax/blocked-invoices"};
        for (String path : reads) {
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
            mockMvc.perform(get(path).with(jwt().authorities(USER))).andExpect(status().isForbidden());
            mockMvc.perform(get(path).with(jwt().authorities(ADMIN))).andExpect(status().isOk());
        }
        String toggle = "{\"externalEngineEnabled\":true}";
        mockMvc.perform(put("/admin/billing/tax/mode").contentType(MediaType.APPLICATION_JSON).content(toggle)).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/admin/billing/tax/mode").with(jwt().authorities(USER)).contentType(MediaType.APPLICATION_JSON).content(toggle)).andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/billing/tax/provider/validate").with(jwt().authorities(USER))).andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/billing/tax/rules/1/approve").with(jwt().authorities(USER))).andExpect(status().isForbidden());
        mockMvc.perform(delete("/admin/billing/tax/rules/1").with(jwt().authorities(USER))).andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/billing/tax/blocked-invoices/1/retry").with(jwt().authorities(USER))).andExpect(status().isForbidden());
    }

    @Test
    void theStatusShowsTheModeAndNeverACredential() throws Exception {
        mockMvc.perform(get("/admin/billing/tax/status").with(jwt().authorities(ADMIN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.mode").value("INTERNAL"))
            .andExpect(jsonPath("$.externalEngineEnabled").value(false))
            .andExpect(jsonPath("$.status").value("BLOCKING"))
            .andExpect(content().string(not(containsString("apiKey"))))
            .andExpect(content().string(not(containsString("apiSecret"))))
            .andExpect(content().string(not(containsString("password"))));
    }

    @Test
    void switchingOnWithoutAProviderIsAConflictThatListsTheFailedChecks() throws Exception {
        mockMvc.perform(put("/admin/billing/tax/mode").with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"externalEngineEnabled\":true}"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.changed").value(false))
            .andExpect(jsonPath("$.report.passed").value(false))
            .andExpect(jsonPath("$.report.checks[?(@.code=='PROVIDER_SELECTED')].passed").value(false))
            .andExpect(jsonPath("$.status.mode").value("INTERNAL"));
        mockMvc.perform(put("/admin/billing/tax/mode").with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void theProviderSettingsApiTakesNoCredentialsAndRejectsBadValues() throws Exception {
        mockMvc.perform(put("/admin/billing/tax/provider").with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"providerCode\":\"STUB\",\"companyCode\":\"CO\",\"environment\":\"SANDBOX\",\"timeoutSeconds\":5,\"maxAttempts\":3,\"apiKey\":\"leak\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("leak"))))
            .andExpect(jsonPath("$.external.providerCode").value("STUB"))
            .andExpect(jsonPath("$.external.credentials.STUB_KEY").value(true));
        mockMvc.perform(put("/admin/billing/tax/provider").with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"providerCode\":\"STUB\",\"environment\":\"SANDBOX\",\"timeoutSeconds\":500,\"maxAttempts\":3}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void aRuleIsCreatedAndRatesAreValidatedByTheApi() throws Exception {
        String ok = "{\"name\":\"GST\",\"countryCode\":\"IN\",\"taxType\":\"GST\",\"treatment\":\"TAXABLE\",\"effectiveFrom\":\"2026-04-01\","
            + "\"components\":[{\"code\":\"IGST\",\"name\":\"IGST\",\"rate\":18,\"compound\":false}]}";
        mockMvc.perform(post("/admin/billing/tax/rules").with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON).content(ok))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.approvalStatus").value("DRAFT"))
            .andExpect(jsonPath("$.components[0].code").value("IGST"));
        String tooHigh = ok.replace("\"rate\":18", "\"rate\":180");
        mockMvc.perform(post("/admin/billing/tax/rules").with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON).content(tooHigh))
            .andExpect(status().isBadRequest());
        String tooManyDecimals = ok.replace("\"rate\":18", "\"rate\":18.12345");
        mockMvc.perform(post("/admin/billing/tax/rules").with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON).content(tooManyDecimals))
            .andExpect(status().isBadRequest());
        String negative = ok.replace("\"rate\":18", "\"rate\":-1");
        mockMvc.perform(post("/admin/billing/tax/rules").with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON).content(negative))
            .andExpect(status().isBadRequest());
    }
}
