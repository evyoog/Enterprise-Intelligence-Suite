package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.dto.PaymentGatewayDto;
import com.vyoog.eisplatform.modules.billing.model.BillingSettings;
import com.vyoog.eisplatform.modules.billing.repository.BillingSettingsRepository;
import com.vyoog.eisplatform.modules.billing.service.gateway.PaymentGateway;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** C97 BI-8: the list of payment gateways (every {@link PaymentGateway} bean) and which one is active at checkout. Switching the active
 * gateway affects new payments only; a payment already started stays with the gateway that created it. */
@Service
public class PaymentGatewayService {

    private final List<PaymentGateway> gateways;
    private final BillingSettingsService settingsService;
    private final BillingSettingsRepository settingsRepository;
    private final AuditService auditService;

    public PaymentGatewayService(List<PaymentGateway> gateways, BillingSettingsService settingsService,
                                 BillingSettingsRepository settingsRepository, AuditService auditService) {
        this.gateways = gateways.stream().sorted(Comparator.comparing(PaymentGateway::displayName)).toList();
        this.settingsService = settingsService;
        this.settingsRepository = settingsRepository;
        this.auditService = auditService;
    }

    public List<PaymentGatewayDto> list() {
        String active = activeCode();
        return gateways.stream().map(g -> new PaymentGatewayDto(g.code(), g.displayName(), g.isConfigured(), g.code().equals(active),
            g.supportedMethods().stream().sorted().toList(), g.status())).toList();
    }

    /** The active gateway's code; falls back to the first registered gateway if the saved code is no longer registered. */
    public String activeCode() {
        String saved = settingsService.current().getActiveGateway();
        return gateways.stream().map(PaymentGateway::code).filter(c -> c.equals(saved)).findFirst()
            .orElse(gateways.isEmpty() ? null : gateways.get(0).code());
    }

    public String activeName() {
        String code = activeCode();
        return gateways.stream().filter(g -> g.code().equals(code)).map(PaymentGateway::displayName).findFirst().orElse(null);
    }

    @Transactional
    public List<PaymentGatewayDto> setActive(Long adminCustomerId, String code) {
        PaymentGateway gateway = find(code);
        if (!gateway.isConfigured()) {
            throw new IllegalArgumentException(gateway.displayName() + " is not configured yet. Add its credentials to the backend environment first.");
        }
        BillingSettings s = settingsService.current();
        String before = s.getActiveGateway();
        s.setActiveGateway(gateway.code());
        s.setUpdatedByCustomerId(adminCustomerId);
        s = settingsRepository.save(s);
        auditService.recordSuccess("BILLING_ACTIVE_GATEWAY_CHANGED", null, adminCustomerId, null, "BillingSettings", s.getId().toString(), null,
            "Active payment gateway changed from " + before + " to " + gateway.code());
        return list();
    }

    public void test(String code) {
        find(code).testConnection();
    }

    private PaymentGateway find(String code) {
        String wanted = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        return gateways.stream().filter(g -> g.code().equals(wanted)).findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Payment gateway not found"));
    }
}
