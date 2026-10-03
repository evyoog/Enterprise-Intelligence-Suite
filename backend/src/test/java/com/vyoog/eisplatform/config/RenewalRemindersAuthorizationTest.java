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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** REQ-SUB-004.4/.5 (C64): the platform reminder defaults need MANAGE_BILLING. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RenewalRemindersAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void onlyABillingAdminCanSetTheReminderDefaults() throws Exception {
        String body = "{\"daysBefore\":7,\"sendTime\":\"09:00\",\"timeZone\":\"Asia/Kolkata\"}";
        mockMvc.perform(get("/admin/billing/settings/renewal-reminders").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
        mockMvc.perform(put("/admin/billing/settings/renewal-reminders").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.daysBefore").value(7))
            .andExpect(jsonPath("$.sendTime").value("09:00"));
        mockMvc.perform(put("/admin/billing/settings/renewal-reminders").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).content("{\"daysBefore\":31,\"sendTime\":\"09:00\",\"timeZone\":\"Asia/Kolkata\"}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(put("/admin/billing/settings/renewal-reminders").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).content("{\"daysBefore\":7,\"sendTime\":\"25:00\",\"timeZone\":\"Asia/Kolkata\"}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(put("/admin/billing/settings/renewal-reminders").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).content("{\"daysBefore\":7,\"sendTime\":\"09:00\",\"timeZone\":\"Mars/Base\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void reminderSettingsNeedASignedInUser() throws Exception {
        mockMvc.perform(get("/me/notification-preferences/renewal-reminders")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/me/renewals")).andExpect(status().isUnauthorized());
    }
}
