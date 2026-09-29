package com.vyoog.eisplatform.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** REQ-IAM-006: sign-in navigations are public and fail back to the web app;
 * provider management needs a signed-in organization admin. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OidcEndpointSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void signInNavigationIsPublicAndRedirectsBackWithAReason() throws Exception {
        mockMvc.perform(get("/oidc/999999/login-init"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", containsString("/?ssoError=")));
        mockMvc.perform(get("/oidc/999999/callback").param("state", "nope").param("code", "x"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", containsString("/?ssoError=")));
    }

    @Test
    void providerManagementNeedsASignedInUser() throws Exception {
        mockMvc.perform(get("/organization/me/oidc-providers")).andExpect(status().isUnauthorized());
    }
}
