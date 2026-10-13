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

/** REQ-GOV-002 (C92): message templates and platform defaults need MANAGE_PLATFORM_SETTINGS. TC-GOV-016. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MessageTemplateAuthorizationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void onlyPlatformAdministratorsReachTemplatesAndDefaults() throws Exception {
        for (String path : new String[] {"/admin/platform-settings/message-templates", "/admin/platform-settings/defaults"}) {
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
            mockMvc.perform(get(path).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER")))).andExpect(status().isForbidden());
        }
        mockMvc.perform(get("/admin/platform-settings/message-templates").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").exists())
            .andExpect(jsonPath("$[0].variants.length()").value(2));
        mockMvc.perform(put("/admin/platform-settings/message-templates/EMAIL_PASSWORD_RESET/en")
                .with(jwt().jwt(j -> j.subject("kc-admin")).authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).content("{\"subject\":\"Reset\",\"body\":\"No link\"}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/admin/platform-settings/defaults").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reminderDays").exists());
    }
}
