package com.vyoog.eisplatform.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** C103 / REQ-TEN-010: every access-request and self-service-removal endpoint needs a signed-in customer (the service checks organization membership and,
 * for the admin side, MANAGE_PRODUCT_ACCESS, itself — see AccessRequestServiceTest). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccessRequestAuthorizationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void everyAccessRequestEndpointNeedsASignedInCustomer() throws Exception {
        mockMvc.perform(post("/organization/me/access-requests").contentType(MediaType.APPLICATION_JSON).content("{\"productId\":1}"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/organization/me/access-requests")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/organization/me/access-requests/1/cancel")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/organization/me/products/1/access")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/organization/me/access-requests/all")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/organization/me/access-requests/1/approve")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/organization/me/access-requests/1/reject")).andExpect(status().isUnauthorized());
    }
}
