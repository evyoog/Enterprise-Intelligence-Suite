package com.vyoog.eisplatform.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** REQ-MKT-004 and REQ-CAT-003.13 (C94): the wishlist needs sign-in; the catalog ratings are public like the product list. TC-MKT-036. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WishlistAuthorizationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void theWishlistNeedsSignInAndTheCatalogRatingsDoNot() throws Exception {
        mockMvc.perform(get("/me/wishlist")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/me/wishlist/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/me/wishlist/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/products/ratings")).andExpect(status().isOk());
    }
}
