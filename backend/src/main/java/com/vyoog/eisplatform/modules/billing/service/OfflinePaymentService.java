package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.dto.InvoiceDto;
import com.vyoog.eisplatform.modules.billing.dto.OfflineBankDetailsDto;
import com.vyoog.eisplatform.modules.billing.dto.RecordOfflinePaymentRequest;
import com.vyoog.eisplatform.modules.billing.dto.SaveOfflineBankDetailsRequest;
import com.vyoog.eisplatform.modules.billing.model.BillingSettings;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.model.OfflinePaymentMethod;
import com.vyoog.eisplatform.modules.billing.model.Payment;
import com.vyoog.eisplatform.modules.billing.model.PaymentRoute;
import com.vyoog.eisplatform.modules.billing.model.PaymentStatus;
import com.vyoog.eisplatform.modules.billing.repository.BillingSettingsRepository;
import com.vyoog.eisplatform.modules.billing.repository.PaymentRepository;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/** C55 (REQ-BIL-001.20, .21): payments a billing administrator records by
 * hand when a bank transfer, NEFT/RTGS or cheque arrives, and the offline
 * bank details printed on offline invoices. Nothing here calls Razorpay, so
 * it works in "Payment gateway not configured" mode. */
@Service
@RequiredArgsConstructor
public class OfflinePaymentService {

    public static final String OFFLINE = "OFFLINE";

    private final InvoiceService invoiceService;
    private final PaymentRepository paymentRepository;
    private final BillingSettingsRepository settingsRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final PaymentEvents paymentEvents;

    @Transactional
    public InvoiceDto recordOfflinePayment(Long adminCustomerId, Long invoiceId, RecordOfflinePaymentRequest request) {
        Invoice invoice = invoiceService.getById(invoiceId);
        if (invoice.getStatus() != InvoiceStatus.OPEN) {
            throw new BillingConflictException("Only an OPEN invoice can be marked as paid.");
        }
        if (invoice.getPaymentRoute() != PaymentRoute.OFFLINE) {
            // Recording an offline payment against an invoice the customer is
            // paying online is Not specified (C55) — refused rather than guessed.
            throw new BillingConflictException("This invoice is not set to be paid by invoice.");
        }
        if (request.amount() != invoice.getTotal()) {
            // Partial offline payments: Not specified (FRD Open question 16).
            throw new IllegalArgumentException("The amount received must equal the open amount of the invoice.");
        }
        if (!acceptedMethod(request.method())) {
            // C60: the admin has switched this offline method off.
            throw new IllegalArgumentException("This offline payment method is not accepted.");
        }
        if (request.receivedOn().isAfter(LocalDate.now(ZoneOffset.UTC))) {
            throw new IllegalArgumentException("The date received cannot be in the future.");
        }

        Payment payment = new Payment();
        payment.setInvoiceId(invoice.getId());
        payment.setProvider(OFFLINE);
        payment.setMethodType(OFFLINE);
        payment.setMethodNetwork(request.method().name());
        payment.setStatus(PaymentStatus.CAPTURED);
        payment.setCurrency(invoice.getCurrency());
        payment.setAmount(request.amount());
        payment.setCapturedAt(Instant.now());
        payment.setOfflineMethod(request.method());
        payment.setOfflineReference(request.reference().trim());
        payment.setReceivedOn(request.receivedOn());
        payment.setRecordedByCustomerId(adminCustomerId);
        payment.setNote(request.note() == null || request.note().isBlank() ? null : request.note().trim());
        payment = paymentRepository.save(payment);

        invoiceService.markPaid(invoice);
        paymentEvents.captured(payment);
        auditService.recordSuccess("OFFLINE_PAYMENT_RECORDED", null, adminCustomerId, null,
            "Payment", payment.getId().toString(), invoice.getOwnerOrganizationId(),
            "Offline payment " + request.method() + " ref " + payment.getOfflineReference()
                + " recorded for invoice " + invoice.getInvoiceNumber());
        if (invoice.getOwnerCustomerId() != null) {
            customerRepository.findById(invoice.getOwnerCustomerId()).ifPresent(c ->
                notificationService.notify(c.getId(), c.getEmail(), NotificationCategory.BILLING, NotificationSeverity.INFO,
                    "Payment received for " + invoice.getInvoiceNumber(),
                    "We've received your payment. Your receipt is available in Billing."));
        }
        return invoiceService.getAdminInvoiceDetail(invoice.getId());
    }

    public OfflineBankDetailsDto bankDetails() {
        return settingsRepository.findFirstByOrderByIdAsc().map(this::toDto).orElse(OfflineBankDetailsDto.empty());
    }

    private boolean acceptedMethod(OfflinePaymentMethod method) {
        OfflineBankDetailsDto d = bankDetails();
        return switch (method) {
            case BANK_TRANSFER -> d.bankTransferEnabled();
            case NEFT_RTGS -> d.neftRtgsEnabled();
            case CHEQUE -> d.chequeEnabled();
        };
    }

    @Transactional
    public OfflineBankDetailsDto saveBankDetails(Long adminCustomerId, SaveOfflineBankDetailsRequest r) {
        boolean bankTransfer = r.bankTransferEnabled() == null || r.bankTransferEnabled();
        boolean neftRtgs = r.neftRtgsEnabled() == null || r.neftRtgsEnabled();
        boolean cheque = r.chequeEnabled() == null || r.chequeEnabled();
        if (!bankTransfer && !neftRtgs && !cheque) {
            throw new IllegalArgumentException("Accept at least one offline payment method.");
        }
        BillingSettings settings = settingsRepository.findFirstByOrderByIdAsc().orElseGet(BillingSettings::new);
        settings.setOfflineAccountName(r.accountName().trim());
        settings.setOfflineBankName(r.bankName().trim());
        settings.setOfflineBranchName(text(r.branchName()));
        settings.setOfflineAccountNumber(r.accountNumber().trim());
        settings.setOfflineAccountType(text(r.accountType()));
        settings.setOfflineIfsc(code(r.ifsc()));
        settings.setOfflineSwiftBic(code(r.swiftBic()));
        settings.setOfflineIban(code(r.iban()));
        settings.setOfflineMicr(text(r.micr()));
        settings.setOfflineUpiId(text(r.upiId()));
        settings.setOfflineChequePayableTo(text(r.chequePayableTo()));
        settings.setOfflineChequeAddress(text(r.chequeAddress()));
        settings.setOfflineInstructions(text(r.instructions()));
        settings.setOfflineBankTransferEnabled(bankTransfer);
        settings.setOfflineNeftRtgsEnabled(neftRtgs);
        settings.setOfflineChequeEnabled(cheque);
        settings.setUpdatedByCustomerId(adminCustomerId);
        settings = settingsRepository.save(settings);
        auditService.recordSuccess("OFFLINE_BANK_DETAILS_SAVED", null, adminCustomerId, null,
            "BillingSettings", settings.getId().toString(), null, "Offline bank details saved");
        return toDto(settings);
    }

    private static String text(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String code(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase();
    }

    private OfflineBankDetailsDto toDto(BillingSettings s) {
        return new OfflineBankDetailsDto(s.getOfflineAccountName(), s.getOfflineBankName(), s.getOfflineBranchName(),
            s.getOfflineAccountNumber(), s.getOfflineAccountType(), s.getOfflineIfsc(), s.getOfflineSwiftBic(), s.getOfflineIban(),
            s.getOfflineMicr(), s.getOfflineUpiId(), s.getOfflineChequePayableTo(), s.getOfflineChequeAddress(),
            s.getOfflineInstructions(), s.isOfflineBankTransferEnabled(), s.isOfflineNeftRtgsEnabled(), s.isOfflineChequeEnabled(),
            s.getUpdatedAt());
    }
}
