package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.dto.BusinessProfileDto;
import com.vyoog.eisplatform.modules.billing.dto.PaymentMethodSettingsDto;
import com.vyoog.eisplatform.modules.billing.dto.SaveBusinessProfileRequest;
import com.vyoog.eisplatform.modules.billing.dto.SavePaymentMethodSettingsRequest;
import com.vyoog.eisplatform.modules.billing.model.BillingSettings;
import com.vyoog.eisplatform.modules.billing.repository.BillingSettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** C60: the business profile (invoice issuer), invoicing rules and the
 * checkout's payment methods, kept in the one {@code billing_settings} row
 * (offline bank details: {@link OfflinePaymentService}). Before an admin
 * saves anything, every method is on, the prefix is "INV" and invoices are
 * due on issue — the behaviour before C60. Nothing here is a secret
 * (BR-SEC-001). */
@Service
@RequiredArgsConstructor
public class BillingSettingsService {

    public static final String CARD = "card";
    public static final String UPI = "upi";
    public static final String NETBANKING = "netbanking";
    public static final String WALLET = "wallet";

    private final BillingSettingsRepository settingsRepository;
    private final AuditService auditService;

    /** The saved row, or an unsaved one holding the defaults. */
    public BillingSettings current() {
        return settingsRepository.findFirstByOrderByIdAsc().orElseGet(BillingSettings::new);
    }

    public BusinessProfileDto businessProfile() {
        return toBusinessDto(current());
    }

    @Transactional
    public BusinessProfileDto saveBusinessProfile(Long adminCustomerId, SaveBusinessProfileRequest r) {
        BillingSettings s = current();
        s.setBusinessLegalName(r.legalName().trim());
        s.setBusinessTradeName(blankToNull(r.tradeName()));
        s.setBusinessGstin(upper(r.gstin()));
        s.setBusinessPan(upper(r.pan()));
        s.setBusinessCin(upper(r.cin()));
        s.setBusinessAddressLine1(r.addressLine1().trim());
        s.setBusinessAddressLine2(blankToNull(r.addressLine2()));
        s.setBusinessCity(r.city().trim());
        s.setBusinessState(r.state().trim());
        s.setBusinessPostalCode(r.postalCode().trim());
        s.setBusinessCountry(r.country().trim());
        s.setBusinessEmail(r.email().trim());
        s.setBusinessPhone(blankToNull(r.phone()));
        s.setBusinessWebsite(blankToNull(r.website()));
        s.setInvoicePrefix(r.invoicePrefix().trim().toUpperCase(Locale.ROOT));
        s.setPaymentTermsDays(r.paymentTermsDays());
        s.setInvoiceFooterNote(blankToNull(r.invoiceFooterNote()));
        s.setUpdatedByCustomerId(adminCustomerId);
        s = settingsRepository.save(s);
        auditService.recordSuccess("BILLING_BUSINESS_PROFILE_SAVED", null, adminCustomerId, null,
            "BillingSettings", s.getId().toString(), null, "Business profile and invoicing settings saved");
        return toBusinessDto(s);
    }

    public PaymentMethodSettingsDto paymentMethods() {
        return toMethodsDto(current());
    }

    @Transactional
    public PaymentMethodSettingsDto savePaymentMethods(Long adminCustomerId, SavePaymentMethodSettingsRequest r) {
        if (!r.cardEnabled() && !r.upiEnabled() && !r.netbankingEnabled() && !r.walletEnabled() && !r.payByInvoiceEnabled()) {
            throw new IllegalArgumentException("Keep at least one payment method on, or customers cannot pay.");
        }
        BillingSettings s = current();
        s.setMethodCardEnabled(r.cardEnabled());
        s.setMethodUpiEnabled(r.upiEnabled());
        s.setMethodNetbankingEnabled(r.netbankingEnabled());
        s.setMethodWalletEnabled(r.walletEnabled());
        s.setMethodPayByInvoiceEnabled(r.payByInvoiceEnabled());
        s.setCheckoutDisplayName(blankToNull(r.checkoutDisplayName()));
        s.setCheckoutDescription(blankToNull(r.checkoutDescription()));
        s.setCheckoutThemeColor(r.checkoutThemeColor() == null || r.checkoutThemeColor().isBlank()
            ? null : r.checkoutThemeColor().trim().toUpperCase(Locale.ROOT));
        s.setUpdatedByCustomerId(adminCustomerId);
        s = settingsRepository.save(s);
        auditService.recordSuccess("BILLING_PAYMENT_METHODS_SAVED", null, adminCustomerId, null,
            "BillingSettings", s.getId().toString(), null, "Checkout payment methods saved");
        return toMethodsDto(s);
    }

    /** Online methods offered at checkout, in tile order. */
    public List<String> enabledOnlineMethods() {
        BillingSettings s = current();
        List<String> methods = new ArrayList<>();
        if (s.isMethodCardEnabled()) methods.add(CARD);
        if (s.isMethodUpiEnabled()) methods.add(UPI);
        if (s.isMethodNetbankingEnabled()) methods.add(NETBANKING);
        if (s.isMethodWalletEnabled()) methods.add(WALLET);
        return methods;
    }

    public boolean payByInvoiceEnabled() {
        return current().isMethodPayByInvoiceEnabled();
    }

    /** A start-payment request naming a method the admin switched off is refused. */
    public void requireOnlineMethodEnabled(String method) {
        if (method != null && !enabledOnlineMethods().contains(method)) {
            throw new IllegalArgumentException("This payment method is not offered.");
        }
    }

    private BusinessProfileDto toBusinessDto(BillingSettings s) {
        return new BusinessProfileDto(s.getBusinessLegalName(), s.getBusinessTradeName(), s.getBusinessGstin(), s.getBusinessPan(),
            s.getBusinessCin(), s.getBusinessAddressLine1(), s.getBusinessAddressLine2(), s.getBusinessCity(), s.getBusinessState(),
            s.getBusinessPostalCode(), s.getBusinessCountry(), s.getBusinessEmail(), s.getBusinessPhone(), s.getBusinessWebsite(),
            s.getInvoicePrefix(), s.getPaymentTermsDays(), s.getInvoiceFooterNote(), s.getUpdatedAt());
    }

    private PaymentMethodSettingsDto toMethodsDto(BillingSettings s) {
        return new PaymentMethodSettingsDto(s.isMethodCardEnabled(), s.isMethodUpiEnabled(), s.isMethodNetbankingEnabled(),
            s.isMethodWalletEnabled(), s.isMethodPayByInvoiceEnabled(), s.getCheckoutDisplayName(), s.getCheckoutDescription(),
            s.getCheckoutThemeColor(), s.getUpdatedAt());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String upper(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
