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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** REQ-SUB-002 BR-4, BR-5 (C97 EN-2, EN-3): the internal check needs the shared secret; the support lookup needs VIEW_ENTITLEMENTS. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EntitlementsAuthorizationTest {

    @Autowired private MockMvc mockMvc;

    private static final String BODY = "{\"customerId\":1,\"productId\":1,\"featureKey\":\"x\"}";

    @Test
    void theInternalCheckNeedsTheSharedSecret() throws Exception {
        mockMvc.perform(post("/internal/entitlements/check").contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden());
        mockMvc.perform(post("/internal/entitlements/check").header("X-Internal-Sso-Secret", "wrong").contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isForbidden());
        mockMvc.perform(post("/internal/entitlements/check").header("X-Internal-Sso-Secret", "local-dev-only-shared-secret-change-me")
                .contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.allowed").value(false));
        mockMvc.perform(post("/internal/entitlements/check").header("X-Internal-Sso-Secret", "local-dev-only-shared-secret-change-me")
                .contentType(MediaType.APPLICATION_JSON).content("{\"customerId\":1}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void theSupportLookupIsForPlatformAdminsOnly() throws Exception {
        mockMvc.perform(get("/admin/entitlements/customers/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/entitlements/customers/1").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/entitlements/organizations/1").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
    }
}
