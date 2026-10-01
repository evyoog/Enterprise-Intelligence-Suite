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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** C53: the platform admin dashboard needs VIEW_PLATFORM_DASHBOARD (seeded
 * for ADMIN only) — a signed-out call is unauthenticated, a signed-in
 * non-admin is forbidden, and a platform admin sees the real overview. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlatformDashboardAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void onlyAPlatformAdminCanSeeThePlatformDashboard() throws Exception {
        mockMvc.perform(get("/admin/platform-dashboard")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/platform-dashboard").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/platform-dashboard").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.organizations.total").exists())
            .andExpect(jsonPath("$.catalog.totalProducts").exists())
            .andExpect(jsonPath("$.serviceHealth.platformStatus").exists());
    }
}
