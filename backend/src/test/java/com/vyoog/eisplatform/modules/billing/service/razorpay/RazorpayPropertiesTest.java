package com.vyoog.eisplatform.modules.billing.service.razorpay;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RazorpayPropertiesTest {

    @Test
    void emptyCredentialsMeanNotConfigured() {
        RazorpayProperties properties = new RazorpayProperties("", "", "");
        assertThat(properties.isConfigured()).isFalse();
    }

    @Test
    void anyMissingCredentialMeansNotConfigured() {
        assertThat(new RazorpayProperties("rzp_test_abc", "", "whsec").isConfigured()).isFalse();
        assertThat(new RazorpayProperties("rzp_test_abc", "secret", "").isConfigured()).isFalse();
    }

    @Test
    void everyCredentialPresentMeansConfigured() {
        assertThat(new RazorpayProperties("rzp_test_abc", "secret", "whsec").isConfigured()).isTrue();
    }

    @Test
    void keyPrefixDeterminesLiveMode() {
        assertThat(new RazorpayProperties("rzp_live_abc", "s", "w").isLiveMode()).isTrue();
        assertThat(new RazorpayProperties("rzp_test_abc", "s", "w").isLiveMode()).isFalse();
    }

    @Test
    void maskedKeyIdKeepsThePrefixAndLastFourDigitsOnly() {
        String masked = new RazorpayProperties("rzp_live_1234567890", "s", "w").maskedKeyId();
        assertThat(masked).isEqualTo("rzp_live_••••••7890");
    }
}
