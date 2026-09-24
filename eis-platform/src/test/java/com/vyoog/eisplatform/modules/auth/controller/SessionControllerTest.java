package com.vyoog.eisplatform.modules.auth.controller;

import com.vyoog.eisplatform.modules.auth.service.FakeKeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Phase 3 (2026.3.3): drives real HTTP requests through the real filter
 * chain (same convention as CatalogAuthorizationTest) to prove the one
 * thing that actually matters here — a caller can never revoke a session
 * that isn't in THEIR OWN {@code listSessions(sub)} result, regardless of
 * what session id they supply in the URL.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SessionControllerTest.TestConfig.class)
class SessionControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        KeycloakAdminClient fakeKeycloakAdminClient() {
            return new FakeKeycloakAdminClient();
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private KeycloakAdminClient keycloakAdminClient;

    private FakeKeycloakAdminClient fake() {
        return (FakeKeycloakAdminClient) keycloakAdminClient;
    }

    @BeforeEach
    void reset() {
        fake().reset();
    }

    @Test
    void listsOnlyTheCallersOwnSessions() throws Exception {
        String mySessionId = fake().addSession("my-sub", "1.2.3.4");
        fake().addSession("someone-elses-sub", "9.9.9.9");

        mockMvc.perform(get("/me/sessions").with(jwt().jwt(builder -> builder.subject("my-sub"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(mySessionId));
    }

    @Test
    void canRevokeOwnSession() throws Exception {
        String sessionId = fake().addSession("my-sub", "1.2.3.4");

        mockMvc.perform(delete("/me/sessions/" + sessionId).with(jwt().jwt(builder -> builder.subject("my-sub"))))
            .andExpect(status().isNoContent());

        org.assertj.core.api.Assertions.assertThat(fake().listSessions("my-sub")).isEmpty();
    }

    @Test
    void cannotRevokeAnotherUsersSessionByGuessingItsId() throws Exception {
        String otherUsersSessionId = fake().addSession("someone-elses-sub", "9.9.9.9");

        mockMvc.perform(delete("/me/sessions/" + otherUsersSessionId).with(jwt().jwt(builder -> builder.subject("my-sub"))))
            .andExpect(status().isForbidden());

        // The other user's session must still be intact — the forbidden
        // response must not have leaked into an actual revocation.
        org.assertj.core.api.Assertions.assertThat(fake().listSessions("someone-elses-sub")).hasSize(1);
    }

    @Test
    void revokingANonExistentSessionIdIsAlsoForbiddenNotFound() throws Exception {
        mockMvc.perform(delete("/me/sessions/does-not-exist").with(jwt().jwt(builder -> builder.subject("my-sub"))))
            .andExpect(status().isForbidden());
    }
}
