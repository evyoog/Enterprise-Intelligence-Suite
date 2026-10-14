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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** C98 (REQ-BIL-003): the gateway API is MANAGE_BILLING only, never carries a secret, refuses planned providers; webhooks are public but signed. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentGatewayAuthorizationTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private RazorpayClient razorpayClient;

    private static final SimpleGrantedAuthority USER = new SimpleGrantedAuthority("ROLE_ACTIVITY_USER");
    private static final SimpleGrantedAuthority ADMIN = new SimpleGrantedAuthority("ROLE_ADMIN");

    @Test
    void everyGatewayEndpointNeedsSignInAndTheBillingPermission() throws Exception {
        for (String path : new String[] {"/admin/billing/gateways", "/admin/billing/gateways/summary", "/admin/billing/gateways/RAZORPAY"}) {
            mockMvc.perform(get(path)).andExpect(status().isUnauthorized());
            mockMvc.perform(get(path).with(jwt().authorities(USER))).andExpect(status().isForbidden());
            mockMvc.perform(get(path).with(jwt().authorities(ADMIN))).andExpect(status().isOk());
        }
        for (String path : new String[] {"/admin/billing/gateways/RAZORPAY/test", "/admin/billing/gateways/RAZORPAY/activate", "/admin/billing/gateways/RAZORPAY/deactivate"}) {
            mockMvc.perform(post(path)).andExpect(status().isUnauthorized());
            mockMvc.perform(post(path).with(jwt().authorities(USER))).andExpect(status().isForbidden());
        }
        mockMvc.perform(put("/admin/billing/gateways/RAZORPAY/configuration").with(jwt().authorities(USER)).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void theDetailShowsWhetherEachCredentialIsSetAndNeverItsValue() throws Exception {
        mockMvc.perform(get("/admin/billing/gateways/razorpay").with(jwt().authorities(ADMIN)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.gateway.code").value("RAZORPAY"))
            .andExpect(jsonPath("$.fields[?(@.key=='keySecret')].configured").value(true))
            .andExpect(jsonPath("$.fields[?(@.key=='keySecret')].source").value("ENVIRONMENT"))
            .andExpect(jsonPath("$.fields[?(@.key=='keySecret')].maskedValue").isEmpty())
            .andExpect(content().string(not(containsString("test-key-secret"))))
            .andExpect(content().string(not(containsString("test-webhook-secret"))))
            .andExpect(content().string(not(containsString("rzp_test_1234567890"))));
    }

    @Test
    void aPlannedProviderCannotBeConfiguredTestedOrActivated() throws Exception {
        for (String action : new String[] {"test", "activate"}) {
            mockMvc.perform(post("/admin/billing/gateways/STRIPE/" + action).with(jwt().authorities(ADMIN))).andExpect(status().isConflict());
        }
        mockMvc.perform(put("/admin/billing/gateways/STRIPE/configuration").with(jwt().authorities(ADMIN)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"environment\":\"TEST\",\"fields\":{\"secretKey\":\"sk_test_x\"}}"))
            .andExpect(status().isConflict());
        mockMvc.perform(get("/admin/billing/gateways/NOPE").with(jwt().authorities(ADMIN))).andExpect(status().isNotFound());
    }

    @Test
    void webhooksArePublicButNeedAValidSignatureAndAKnownProvider() throws Exception {
        when(razorpayClient.verifyWebhookSignature(anyString(), anyString())).thenReturn(false);
        mockMvc.perform(post("/webhooks/razorpay").header("X-Razorpay-Signature", "forged").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/webhooks/razorpay").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mockMvc.perform(post("/webhooks/stripe").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isNotFound());

        when(razorpayClient.verifyWebhookSignature(anyString(), anyString())).thenReturn(true);
        mockMvc.perform(post("/webhooks/razorpay").header("X-Razorpay-Signature", "ok").contentType(MediaType.APPLICATION_JSON)
                .content("{\"id\":\"evt_http_1\",\"event\":\"order.paid\"}"))
            .andExpect(status().isOk());
    }
}
