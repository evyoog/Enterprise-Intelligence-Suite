package com.vyoog.eisplatform.modules.toolsync;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The platform's MCP endpoint needs a validated bearer token (TC-INT-056); which tool may do what is decided per call, not here. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PlatformMcpSecurityTest {

    @Autowired private MockMvc mvc;

    @Test
    void theMcpEndpointRefusesACallWithoutAToken() throws Exception {
        mvc.perform(post("/mcp").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isUnauthorized());
    }

    @Test
    void aValidTokenPassesTheSecurityChainOnTheMcpPath() throws Exception {
        // MockMvc has no servlet for /mcp (it is a plain servlet registered with the container), so "not 401/403" is what is checked here
        int status = mvc.perform(post("/mcp").with(jwt().jwt(j -> j.claim("azp", "thittam-sync"))).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andReturn().getResponse().getStatus();
        org.assertj.core.api.Assertions.assertThat(status).isNotIn(401, 403);
    }
}
