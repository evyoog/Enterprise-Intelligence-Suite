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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Phase 3 (2026.3.3): same convention as CatalogAuthorizationTest — proves
 * the MANAGE_ROLES/MANAGE_PERMISSIONS URL-level gates in SecurityConfig
 * actually work via a real request through the real filter chain, not just
 * that the annotation exists.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RbacAdminAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousCannotListRoles() throws Exception {
        mockMvc.perform(get("/admin/roles")).andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminCannotListRoles() throws Exception {
        mockMvc.perform(get("/admin/roles").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListRoles() throws Exception {
        mockMvc.perform(get("/admin/roles").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
    }

    @Test
    void nonAdminCannotListPermissions() throws Exception {
        mockMvc.perform(get("/admin/permissions").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListPermissions() throws Exception {
        mockMvc.perform(get("/admin/permissions").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
    }
}
