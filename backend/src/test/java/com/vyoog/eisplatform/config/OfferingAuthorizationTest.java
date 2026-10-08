package com.vyoog.eisplatform.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** REQ-CAT-005 (BR-OFR-001): browsing is public; managing offerings and product rules needs MANAGE_CATALOG. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OfferingAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    private static RequestPostProcessor admin() {
        return jwt().jwt(j -> j.subject("sub-admin").claim("email", "admin@test")).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static RequestPostProcessor customer() {
        return jwt().jwt(j -> j.subject("sub-customer")).authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"));
    }

    @Test
    void browsingIsPublic() throws Exception {
        mockMvc.perform(get("/offerings")).andExpect(status().isOk());
        mockMvc.perform(get("/offerings/999999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/products/999999/works-with")).andExpect(status().isNotFound());
    }

    @Test
    void managingNeedsTheCatalogPermission() throws Exception {
        String body = "{\"name\":\"X\",\"productIds\":[1]}";
        mockMvc.perform(get("/admin/offerings")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/offerings").with(customer())).andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/offerings").with(customer()).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/offerings/product-rules").with(customer())).andExpect(status().isForbidden());
        mockMvc.perform(put("/admin/offerings/product-rules/1").with(customer()).contentType(MediaType.APPLICATION_JSON)
            .content("{\"audience\":\"BOTH\"}")).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/offerings").with(admin())).andExpect(status().isOk());
        mockMvc.perform(get("/admin/offerings/product-rules").with(admin())).andExpect(status().isOk()).andExpect(jsonPath("$").isArray());
    }
}
