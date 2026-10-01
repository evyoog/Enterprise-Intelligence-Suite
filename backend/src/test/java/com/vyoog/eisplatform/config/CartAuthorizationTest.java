package com.vyoog.eisplatform.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** C59 (REQ-MKT-003 Open question 5, assumed no): there is no anonymous cart. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void theCartNeedsASignedInUser() throws Exception {
        mockMvc.perform(get("/me/cart")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/me/cart/checkout")).andExpect(status().isUnauthorized());
    }
}
