package com.vyoog.eisplatform.config;

import com.vyoog.eisplatform.modules.knowledgebase.support.KnowledgeTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

/**
 * C79: after sign-out no customer data may be shown again, including from the
 * browser's own cache (Back button, shared computer). Every API answer tells
 * the browser not to store it.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(KnowledgeTestConfig.class)
class NoCacheHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void signedInAnswersAreNeverStoredByTheBrowser() throws Exception {
        mockMvc.perform(get("/me/notifications").with(jwt().jwt(j -> j.subject("cache-test-user"))))
            .andExpect(header().string("Cache-Control", containsString("no-store")))
            .andExpect(header().string("Pragma", "no-cache"));
    }

    @Test
    void refusedAnswersAreNeverStoredEither() throws Exception {
        mockMvc.perform(get("/me/notifications"))
            .andExpect(header().string("Cache-Control", containsString("no-store")));
    }
}
