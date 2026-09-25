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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * REQ-TEN-001: the new organization edit / suspend / activate / close
 * endpoints sit behind the existing /admin/registrations/** gate
 * (MANAGE_REGISTRATIONS), proved through the real filter chain.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrganizationLifecycleAuthorizationTest {

    private static final String VALID_BODY =
        "{\"name\":\"Acme\",\"businessEmail\":\"biz@acme.example\",\"country\":\"India\",\"billingSameAsAddress\":true}";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousCannotEditOrSuspend() throws Exception {
        mockMvc.perform(put("/admin/registrations/organizations/1").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/admin/registrations/organizations/1/suspend"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminCannotChangeTheLifecycle() throws Exception {
        var user = jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"));
        for (String action : new String[] {"suspend", "activate", "close"}) {
            mockMvc.perform(post("/admin/registrations/organizations/1/" + action).with(user))
                .andExpect(status().isForbidden());
        }
        mockMvc.perform(put("/admin/registrations/organizations/1").with(user)
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminGetsNotFoundForAnUnknownOrganization() throws Exception {
        var admin = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
        mockMvc.perform(post("/admin/registrations/organizations/999999/suspend").with(admin))
            .andExpect(status().isNotFound());
        mockMvc.perform(put("/admin/registrations/organizations/999999").with(admin)
                .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isNotFound());
    }

    @Test
    void anInvalidEditIsRejected() throws Exception {
        mockMvc.perform(put("/admin/registrations/organizations/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"businessEmail\":\"not-an-email\",\"country\":\"India\",\"billingSameAsAddress\":true}"))
            .andExpect(status().isBadRequest());
    }
}
