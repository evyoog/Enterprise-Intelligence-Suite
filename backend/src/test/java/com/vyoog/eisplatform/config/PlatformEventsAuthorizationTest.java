package com.vyoog.eisplatform.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** REQ-INT-002.6 / C62: the events view needs MANAGE_INTEGRATIONS (seeded for ADMIN). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlatformEventsAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void onlyAPlatformAdminCanSeeEvents() throws Exception {
        mockMvc.perform(get("/admin/events")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/events").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/events").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(20));
        mockMvc.perform(get("/admin/events/types").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
    }

    @Test
    void retryingAnUnknownEventIsNotFound() throws Exception {
        mockMvc.perform(post("/admin/events/999999/retry").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/admin/events?status=LOST").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isBadRequest());
    }
}
