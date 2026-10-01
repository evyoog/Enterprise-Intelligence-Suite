package com.vyoog.eisplatform.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** C55: offline payments and offline bank details are MANAGE_BILLING only. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OfflineBillingAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void onlyABillingAdminCanRecordOfflinePaymentsOrSeeBankDetailsSettings() throws Exception {
        String body = "{\"amount\":100,\"receivedOn\":\"2026-10-01\",\"method\":\"CHEQUE\",\"reference\":\"X\"}";
        mockMvc.perform(post("/admin/billing/invoices/1/offline-payments").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/admin/billing/invoices/1/offline-payments").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER")))
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/billing/settings/offline").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/billing/settings/offline").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
    }
}
