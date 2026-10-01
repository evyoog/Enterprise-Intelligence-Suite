package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.common.exception.InvalidPaymentSignatureException;
import com.vyoog.eisplatform.common.exception.PaymentGatewayNotConfiguredException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.dto.ConfirmPaymentMethodSetupRequest;
import com.vyoog.eisplatform.modules.billing.dto.PaymentMethodDto;
import com.vyoog.eisplatform.modules.billing.dto.PaymentMethodSetupResponse;
import com.vyoog.eisplatform.modules.billing.dto.SetupPaymentMethodRequest;
import com.vyoog.eisplatform.modules.billing.model.PaymentMethod;
import com.vyoog.eisplatform.modules.billing.model.PaymentMethodStatus;
import com.vyoog.eisplatform.modules.billing.model.PaymentMethodType;
import com.vyoog.eisplatform.modules.billing.repository.PaymentMethodRepository;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayClient;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayPaymentInfo;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/** REQ-BIL-001.10. A real recurring-billing integration would use
 * Razorpay's separate Customer + Token API to save a method independent of
 * any specific payment; that API is out of scope for this pass (Not
 * specified beyond "add/remove/set default" — FRD Open question 7). Instead
 * this MVP verifies a small ({@link #VERIFICATION_AMOUNT_MINOR_UNITS})
 * authorization transaction the same way a real payment is verified (BR-5),
 * then stores the resulting Razorpay payment id as
 * {@link PaymentMethod#getProviderTokenRef()} — good enough to look up and
 * display the method, but NOT sufficient to charge it again without the
 * customer re-entering details (no real recurring token exists here). This
 * limitation is recorded in decision C47 and must be revisited before this
 * feature is relied on for auto-renewal (D15, still undecided anyway). */
@Service
@RequiredArgsConstructor
public class PaymentMethodService {

    static final long VERIFICATION_AMOUNT_MINOR_UNITS = 100;

    private final PaymentMethodRepository paymentMethodRepository;
    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;
    private final AuditService auditService;

    public List<PaymentMethodDto> listForCustomer(Long customerId) {
        return paymentMethodRepository.findByOwnerCustomerIdAndStatus(customerId, PaymentMethodStatus.ACTIVE)
            .stream().map(this::toDto).toList();
    }

    public List<PaymentMethodDto> listForOrganization(Long organizationId) {
        return paymentMethodRepository.findByOwnerOrganizationIdAndStatus(organizationId, PaymentMethodStatus.ACTIVE)
            .stream().map(this::toDto).toList();
    }

    public PaymentMethodDto defaultForCustomer(Long customerId) {
        return paymentMethodRepository.findByOwnerCustomerIdAndIsDefaultTrueAndStatus(customerId, PaymentMethodStatus.ACTIVE)
            .map(this::toDto).orElse(null);
    }

    public PaymentMethodDto defaultForOrganization(Long organizationId) {
        return paymentMethodRepository.findByOwnerOrganizationIdAndIsDefaultTrueAndStatus(organizationId, PaymentMethodStatus.ACTIVE)
            .map(this::toDto).orElse(null);
    }

    public PaymentMethodSetupResponse startSetup(SetupPaymentMethodRequest request) {
        if (!razorpayProperties.isConfigured()) {
            throw new PaymentGatewayNotConfiguredException();
        }
        if (request.type() == PaymentMethodType.CARD && !request.consent()) {
            throw new IllegalArgumentException("Saving a card requires your consent.");
        }
        String orderId = razorpayClient.createOrder(VERIFICATION_AMOUNT_MINOR_UNITS, "INR", "payment-method-setup");
        return new PaymentMethodSetupResponse(orderId, VERIFICATION_AMOUNT_MINOR_UNITS, "INR", razorpayProperties.getKeyId());
    }

    @Transactional
    public PaymentMethodDto confirmSetup(Long customerId, Long organizationId, ConfirmPaymentMethodSetupRequest request) {
        if (!razorpayProperties.isConfigured()) {
            throw new PaymentGatewayNotConfiguredException();
        }
        if (request.type() == PaymentMethodType.CARD && !request.consent()) {
            throw new IllegalArgumentException("Saving a card requires your consent.");
        }
        if (!razorpayClient.verifyPaymentSignature(request.providerOrderId(), request.providerPaymentId(), request.signature())) {
            throw new InvalidPaymentSignatureException();
        }
        RazorpayPaymentInfo info = razorpayClient.fetchPayment(request.providerPaymentId());

        PaymentMethod method = new PaymentMethod();
        method.setOwnerCustomerId(customerId);
        method.setOwnerOrganizationId(organizationId);
        method.setProviderTokenRef(request.providerPaymentId());
        method.setType(request.type());
        method.setNetwork(info.methodNetwork());
        method.setLast4(info.methodLast4());
        if (request.type() == PaymentMethodType.CARD) {
            method.setConsentAt(Instant.now());
        }
        method.setStatus(PaymentMethodStatus.ACTIVE);
        method = paymentMethodRepository.save(method);

        if (request.makeDefault()) {
            setDefaultInternal(customerId, organizationId, method);
        }
        auditService.recordSuccess("PAYMENT_METHOD_ADDED", null, customerId, null,
            "PaymentMethod", method.getId().toString(), organizationId, "Payment method added");
        return toDto(method);
    }

    @Transactional
    public void setDefault(Long customerId, Long organizationId, Long methodId) {
        PaymentMethod method = resolveOwn(customerId, organizationId, methodId);
        if (method.isExpiredCard()) {
            throw new BillingConflictException("An expired card cannot be set as default.");
        }
        setDefaultInternal(customerId, organizationId, method);
        auditService.recordSuccess("PAYMENT_METHOD_DEFAULT_SET", null, customerId, null,
            "PaymentMethod", method.getId().toString(), organizationId, "Default payment method changed");
    }

    private void setDefaultInternal(Long customerId, Long organizationId, PaymentMethod method) {
        List<PaymentMethod> existing = customerId != null
            ? paymentMethodRepository.findByOwnerCustomerIdAndStatus(customerId, PaymentMethodStatus.ACTIVE)
            : paymentMethodRepository.findByOwnerOrganizationIdAndStatus(organizationId, PaymentMethodStatus.ACTIVE);
        for (PaymentMethod other : existing) {
            if (other.isDefault() && !other.getId().equals(method.getId())) {
                other.setDefault(false);
                paymentMethodRepository.save(other);
            }
        }
        method.setDefault(true);
        paymentMethodRepository.save(method);
    }

    @Transactional
    public void remove(Long customerId, Long organizationId, Long methodId) {
        PaymentMethod method = resolveOwn(customerId, organizationId, methodId);
        razorpayClient.deleteToken(method.getProviderTokenRef());
        method.setStatus(PaymentMethodStatus.REMOVED);
        method.setDefault(false);
        paymentMethodRepository.save(method);
        auditService.recordSuccess("PAYMENT_METHOD_REMOVED", null, customerId, null,
            "PaymentMethod", method.getId().toString(), organizationId, "Payment method removed");
    }

    /** C59 (REQ-BIL-001.22): a saved method chosen at checkout must be the
     * caller's own, still saved, and not an expired card. */
    public void requireUsableForPayment(Long customerId, Long organizationId, Long methodId) {
        PaymentMethod method = resolveOwn(customerId, organizationId, methodId);
        if (method.getStatus() != PaymentMethodStatus.ACTIVE) {
            throw new ResourceNotFoundException("Payment method not found");
        }
        if (method.isExpiredCard()) {
            throw new BillingConflictException("This card has expired. Choose another card.");
        }
    }

    private PaymentMethod resolveOwn(Long customerId, Long organizationId, Long methodId) {
        PaymentMethod method = paymentMethodRepository.findById(methodId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment method not found"));
        boolean owns = (customerId != null && customerId.equals(method.getOwnerCustomerId()))
            || (organizationId != null && organizationId.equals(method.getOwnerOrganizationId()));
        if (!owns) {
            throw new ResourceNotFoundException("Payment method not found");
        }
        return method;
    }

    private PaymentMethodDto toDto(PaymentMethod m) {
        return new PaymentMethodDto(m.getId(), m.getType(), m.getNetwork(), m.getLast4(), m.getExpiryMonth(), m.getExpiryYear(),
            m.getCardType(), m.getIssuer(), m.getUpiMasked(), m.isDefault(), m.isExpiredCard());
    }
}
