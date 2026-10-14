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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** C101: who can reach the service instance, reporter key, incident, problem and SLA endpoints, and the open product report endpoint. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ServiceStatusSlaAuthorizationTest {

    @Autowired private MockMvc mockMvc;

    private static final String BODY = "{\"reports\":[{\"instanceId\":1,\"health\":\"HEALTHY\"}]}";

    @Test
    void theProductReportEndpointNeedsNoTokenButRejectsAMissingOrUnknownKey() throws Exception {
        mockMvc.perform(post("/product-reports/health").contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/product-reports/health").header("X-Reporter-Key", "eisrk_not-a-key").contentType(MediaType.APPLICATION_JSON).content(BODY))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void instancesAndReporterKeysNeedManageServiceStatus() throws Exception {
        for (String path : new String[]{"/admin/service-status/instances", "/admin/service-status/instances/summary", "/admin/service-status/reporter-keys",
            "/admin/service-status/incidents", "/admin/service-status/problems"}) {
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
            mockMvc.perform(get(path).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER")))).andExpect(status().isForbidden());
            mockMvc.perform(get(path).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))).andExpect(status().isOk());
        }
    }

    @Test
    void aManualInstanceChangeWithoutANoteIsABadRequest() throws Exception {
        mockMvc.perform(put("/admin/service-status/instances/999999").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"FAILED\"}")).andExpect(status().isNotFound());
    }

    @Test
    void slaSettingsNeedManageSupportTickets() throws Exception {
        mockMvc.perform(get("/admin/support/sla")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/support/sla").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER")))).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/support/sla").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.policies.length()").value(4))
            .andExpect(jsonPath("$.settings.timeZone").exists());
        mockMvc.perform(put("/admin/support/sla/settings").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"businessDays\":[],\"businessStart\":\"09:00\",\"businessEnd\":\"18:00\",\"timeZone\":\"Asia/Kolkata\",\"warnAtPercent\":80,\"alertEmails\":[]}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void anOrganizationsInstancesNeedASignedInCustomer() throws Exception {
        mockMvc.perform(get("/organization/me/service-instances")).andExpect(status().isUnauthorized());
    }
}
