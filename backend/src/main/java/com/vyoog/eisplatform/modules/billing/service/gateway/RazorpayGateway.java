package com.vyoog.eisplatform.modules.billing.service.gateway;

import com.vyoog.eisplatform.modules.billing.dto.GatewayStatusDto;
import com.vyoog.eisplatform.modules.billing.service.GatewayStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/** The Razorpay implementation of {@link PaymentGateway}. */
@Component
@RequiredArgsConstructor
public class RazorpayGateway implements PaymentGateway {

    public static final String CODE = "RAZORPAY";

    private final GatewayStatusService statusService;

    @Override public String code() { return CODE; }

    @Override public String displayName() { return "Razorpay"; }

    @Override public Set<String> supportedMethods() { return Set.of("card", "upi", "netbanking", "wallet"); }

    @Override public boolean isConfigured() { return statusService.getStatus().configured(); }

    @Override public GatewayStatusDto status() { return statusService.getStatus(); }

    @Override public void testConnection() { statusService.testConnection(); }
}
