package com.vyoog.eisplatform.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** C102 / REQ-MKT-005: the compare page is public; starting a trial and asking for a demo need a signed-in customer. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EvaluationAuthorizationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void theComparePageIsOpenToEveryoneAndValidatesItsItems() throws Exception {
        mockMvc.perform(get("/products/compare").param("items", "1")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/products/compare").param("items", "999998,999999")).andExpect(status().isNotFound());
        mockMvc.perform(get("/products/compare")).andExpect(status().isBadRequest());
    }

    @Test
    void trialsAndDemoRequestsNeedASignedInCustomer() throws Exception {
        for (String path : new String[]{"/me/trials", "/organization/me/trials", "/me/demo-requests"}) {
            mockMvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{\"productId\":1}")).andExpect(status().isUnauthorized());
        }
    }
}
