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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * SecurityConfig's own comment on ProductController/PlatformController's
 * mutation endpoints says the MANAGE_CATALOG check there "is the real
 * security boundary" — but until now nothing actually asserted that at test
 * time; a typo in a requestMatchers pattern (the exact class of bug that
 * comment already warns about — a wildcard rule silently swallowing a more
 * specific one) would only ever have surfaced manually or in prod. This
 * drives real HTTP requests through the real filter chain (no mocking) with
 * spring-security-test's {@code jwt()} post-processor standing in for a
 * genuine Keycloak-issued token, since RbacSeeder maps client role ADMIN to
 * MANAGE_CATALOG regardless of how the JWT reached that authority.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String PRODUCT_BODY = """
        {"name":"Security test product","price":1.00}
        """;

    @Test
    void anonymousCanReadThePublicCatalog() throws Exception {
        mockMvc.perform(get("/products")).andExpect(status().isOk());
    }

    @Test
    void anonymousCanUseThePublicSearchEndpoint() throws Exception {
        mockMvc.perform(get("/products/search").param("q", "anything")).andExpect(status().isOk());
    }

    @Test
    void nonAdminCannotUseTheAdminSearchEndpoint() throws Exception {
        mockMvc.perform(get("/products/admin/search")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanUseTheAdminSearchEndpoint() throws Exception {
        mockMvc.perform(get("/products/admin/search")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
    }

    @Test
    void anonymousCannotUseSearchHistory() throws Exception {
        mockMvc.perform(get("/me/search-history")).andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousCannotCreateAProduct() throws Exception {
        mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(PRODUCT_BODY))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminCannotCreateAProduct() throws Exception {
        mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(PRODUCT_BODY)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateAProduct() throws Exception {
        mockMvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(PRODUCT_BODY)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isCreated());
    }

    @Test
    void nonAdminCannotSeeTheAdminProductListing() throws Exception {
        mockMvc.perform(get("/products/admin")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotDeleteAProduct() throws Exception {
        mockMvc.perform(delete("/products/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotListPlatforms() throws Exception {
        // Unlike products, platforms have no public listing at all.
        mockMvc.perform(get("/platforms")).andExpect(status().isUnauthorized());
    }

    @Test
    void nonAdminCannotCreateAPlatform() throws Exception {
        mockMvc.perform(post("/platforms").contentType(MediaType.APPLICATION_JSON).content("""
                {"name":"Security test platform"}
                """)
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotDeleteAPlatform() throws Exception {
        mockMvc.perform(delete("/platforms/1")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
    }
}
