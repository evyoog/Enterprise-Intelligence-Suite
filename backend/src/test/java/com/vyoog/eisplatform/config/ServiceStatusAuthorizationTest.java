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

/** REQ-PRT-001 / C26: the status page needs a signed-in user; posting needs
 * MANAGE_SERVICE_STATUS (seeded for ADMIN). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServiceStatusAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void theCustomerViewNeedsASignedInUser() throws Exception {
        mockMvc.perform(get("/me/service-status")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/me/service-status").with(jwt()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    void onlyAPlatformAdminCanPostStatus() throws Exception {
        String body = "{\"status\":\"DEGRADED\"}";
        mockMvc.perform(put("/admin/service-status/products/1").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(put("/admin/service-status/products/1").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER")))
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/service-status").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
        mockMvc.perform(put("/admin/service-status/products/999999").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isNotFound());
    }
}
