package com.vyoog.eisplatform.config;

import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** C100 (PB-1 note): the coupon API is MANAGE_BILLING only; the cart coupon, auto-renew and reminder-recipient endpoints need sign-in. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CouponAuthorizationTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private RazorpayClient razorpayClient;

    private static final SimpleGrantedAuthority USER = new SimpleGrantedAuthority("ROLE_ACTIVITY_USER");
    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

    @Test
    void theCouponApiNeedsSignInAndTheBillingPermissionAndWorksEndToEnd() throws Exception {
        mockMvc.perform(get("/admin/billing/coupons")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/billing/coupons").with(jwt().authorities(USER))).andExpect(status().isForbidden());
        String code = "HTTP" + System.nanoTime();
        String body = "{\"code\":\"" + code.toLowerCase() + "\",\"description\":\"Launch\",\"discountType\":\"PERCENT\",\"percentOff\":15,\"expiresAt\":\"2099-01-01T00:00:00Z\"}";
        mockMvc.perform(post("/admin/billing/coupons").with(jwt().authorities(USER)).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isForbidden());

        String created = mockMvc.perform(post("/admin/billing/coupons").with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.code").value(code))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andReturn().getResponse().getContentAsString();
        String id = created.replaceAll(".*\"id\":(\\d+).*", "$1");
        mockMvc.perform(post("/admin/billing/coupons").with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict());
        mockMvc.perform(post("/admin/billing/coupons").with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"BADPCT" + System.nanoTime() + "\",\"discountType\":\"PERCENT\",\"percentOff\":100,\"expiresAt\":\"2099-01-01T00:00:00Z\"}"))
            .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/admin/billing/coupons/" + id + "/enabled").with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON).content("{\"enabled\":false}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DISABLED"));
        mockMvc.perform(put("/admin/billing/coupons/" + id).with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"" + code + "\",\"discountType\":\"FIXED\",\"amountOff\":2500,\"currency\":\"inr\",\"expiresAt\":\"2099-06-01T00:00:00Z\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.discountType").value("FIXED")).andExpect(jsonPath("$.currency").value("INR"));
        mockMvc.perform(get("/admin/billing/coupons").with(jwt().authorities(ADMIN))).andExpect(status().isOk()).andExpect(jsonPath("$[?(@.code=='" + code + "')].amountOff").value(2500));
        mockMvc.perform(delete("/admin/billing/coupons/" + id).with(jwt().authorities(USER))).andExpect(status().isForbidden());
        mockMvc.perform(delete("/admin/billing/coupons/" + id).with(jwt().authorities(ADMIN))).andExpect(status().isNoContent());
        mockMvc.perform(delete("/admin/billing/coupons/" + id).with(jwt().authorities(ADMIN))).andExpect(status().isNotFound());
    }

    @Test
    void theNewCustomerEndpointsNeedSignIn() throws Exception {
        mockMvc.perform(put("/me/cart/coupon").contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"X\"}")).andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/me/cart/coupon")).andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/me/subscriptions/1/auto-renew").contentType(MediaType.APPLICATION_JSON).content("{\"autoRenew\":false}")).andExpect(status().isUnauthorized());
        mockMvc.perform(patch("/organization/me/subscriptions/1/auto-renew").contentType(MediaType.APPLICATION_JSON).content("{\"autoRenew\":false}")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/organization/me/renewal-reminder-recipients")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/organization/me/renewal-reminder-recipients").contentType(MediaType.APPLICATION_JSON).content("{\"recipients\":[]}")).andExpect(status().isUnauthorized());
    }
}
