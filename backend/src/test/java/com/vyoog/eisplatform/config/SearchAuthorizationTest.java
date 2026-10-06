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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** C70: search is public; search administration needs MANAGE_SEARCH (seeded for ADMIN). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SearchAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    private static SimpleGrantedAuthority admin() {
        return new SimpleGrantedAuthority("ROLE_ADMIN");
    }

    @Test
    void searchAndSuggestionsArePublic() throws Exception {
        mockMvc.perform(get("/search").param("q", "anything").param("mode", "keyword"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.results").isArray())
            .andExpect(jsonPath("$.engine").value("BASIC"));
        mockMvc.perform(get("/search/suggest").param("q", "an")).andExpect(status().isOk());
    }

    @Test
    void searchAdministrationNeedsManageSearch() throws Exception {
        mockMvc.perform(get("/admin/search/index")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/search/index").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/search/index/rebuild").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/search/index").with(jwt().authorities(admin())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.engine").value("BASIC"))
            .andExpect(jsonPath("$.embedding.configured").value(false));
        mockMvc.perform(get("/admin/search/insights").param("days", "7").with(jwt().authorities(admin())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.days").value(7));
    }

    @Test
    void synonymGroupsAreValidated() throws Exception {
        mockMvc.perform(post("/admin/search/synonyms").with(jwt().authorities(admin()))
                .contentType(MediaType.APPLICATION_JSON).content("{\"terms\":[\"only-one\"]}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/admin/search/synonyms").with(jwt().authorities(admin()))
                .contentType(MediaType.APPLICATION_JSON).content("{\"terms\":[\"Same\",\"same\"]}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/admin/search/synonyms").with(jwt().authorities(admin()))
                .contentType(MediaType.APPLICATION_JSON).content("{\"terms\":[\"Bill\",\"Facturación\"]}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.terms[0]").value("bill"))
            .andExpect(jsonPath("$.terms[1]").value("facturacion"));
        mockMvc.perform(delete("/admin/search/synonyms/999999").with(jwt().authorities(admin())))
            .andExpect(status().isNotFound());
    }
}
