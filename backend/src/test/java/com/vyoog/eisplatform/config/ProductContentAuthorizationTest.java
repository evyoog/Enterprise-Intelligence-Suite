package com.vyoog.eisplatform.config;

import com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** BR-PCON-001: only MANAGE_CATALOG configures product content; the product page's reads are public. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(KnowledgeTestConfig.class)
class ProductContentAuthorizationTest {

    private static final String BODY = "{\"kind\":\"VIDEO\",\"title\":\"Demo\",\"videoUrl\":\"https://youtu.be/dQw4w9WgXcQ\"}";

    @Autowired
    private MockMvc mockMvc;

    private static RequestPostProcessor admin() {
        return jwt().jwt(j -> j.subject("sub-admin")).authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static RequestPostProcessor customer() {
        return jwt().jwt(j -> j.subject("sub-customer")).authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"));
    }

    @Test
    void signedOutAndCustomersCannotConfigureContent() throws Exception {
        String base = "/admin/products/1/content";
        mockMvc.perform(get(base)).andExpect(status().isUnauthorized());
        for (RequestPostProcessor who : new RequestPostProcessor[] {customer()}) {
            mockMvc.perform(get(base).with(who)).andExpect(status().isForbidden());
            mockMvc.perform(post(base + "/upload-url").with(who).contentType(MediaType.APPLICATION_JSON)
                .content("{\"kind\":\"IMAGE\",\"fileName\":\"a.png\",\"contentType\":\"image/png\",\"size\":10}")).andExpect(status().isForbidden());
            mockMvc.perform(post(base).with(who).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden());
            mockMvc.perform(put(base + "/1").with(who).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isForbidden());
            mockMvc.perform(post(base + "/1/publish").with(who)).andExpect(status().isForbidden());
            mockMvc.perform(delete(base + "/1").with(who)).andExpect(status().isForbidden());
        }
        mockMvc.perform(post(base).contentType(MediaType.APPLICATION_JSON).content(BODY)).andExpect(status().isUnauthorized());
    }

    @Test
    void administratorsReachTheirEndpointsAndTheProductPageReadsArePublic() throws Exception {
        mockMvc.perform(get("/admin/products/999999/content").with(admin())).andExpect(status().isNotFound());
        mockMvc.perform(get("/products/999999/content")).andExpect(status().isNotFound());
        mockMvc.perform(get("/products/999999/content/1/download")).andExpect(status().isNotFound());
    }
}
