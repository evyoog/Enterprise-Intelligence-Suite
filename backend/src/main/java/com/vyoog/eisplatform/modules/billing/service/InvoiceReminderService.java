package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceReminder;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceReminderRepository;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * C97 BI-2: payment reminders for unpaid invoices, three days before the due date, on the due date, and seven days after. Each kind is
 * sent once per invoice (a row in {@code invoice_reminder}); if the job was down, only the most advanced kind that applies is sent, never
 * a burst. Reminders stop when the invoice is paid, refunded or is a Draft, and never go out for an invoice without a due date.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceReminderService {

    public static final int DAYS_BEFORE = 3;
    public static final int DAYS_AFTER = 7;

    private final InvoiceRepository invoiceRepository;
    private final InvoiceReminderRepository reminderRepository;
    private final OrganizationMemberRepository memberRepository;
    private final CustomerRepository customerRepository;
    private final BillingDetailsEmail billingDetailsEmail;
    private final NotificationService notificationService;

    /** Which reminder an invoice due on {@code due} needs on {@code today}, or null if none yet. */
    static InvoiceReminder.Kind kindFor(LocalDate today, LocalDate due) {
        long daysUntilDue = ChronoUnit.DAYS.between(today, due);
        if (daysUntilDue <= -DAYS_AFTER) {
            return InvoiceReminder.Kind.AFTER_DUE;
        }
        if (daysUntilDue <= 0) {
            return InvoiceReminder.Kind.ON_DUE;
        }
        if (daysUntilDue <= DAYS_BEFORE) {
            return InvoiceReminder.Kind.BEFORE_DUE;
        }
        return null;
    }

    @Transactional
    public int sendDue(Instant now) {
        LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();
        int sent = 0;
        for (Invoice invoice : invoiceRepository.findByStatusOrderByIdAsc(InvoiceStatus.OPEN)) {
            if (invoice.getDueAt() == null) {
                continue;
            }
            InvoiceReminder.Kind kind = kindFor(today, invoice.getDueAt().atZone(ZoneOffset.UTC).toLocalDate());
            if (kind == null || reminderRepository.existsByInvoiceIdAndKind(invoice.getId(), kind)) {
                continue;
            }
            notifyOwners(invoice, kind);
            InvoiceReminder row = new InvoiceReminder();
            row.setInvoiceId(invoice.getId());
            row.setKind(kind);
            row.setSentAt(now);
            reminderRepository.save(row);
            sent++;
        }
        return sent;
    }

    public int sendDue() {
        return sendDue(Instant.now());
    }

    private void notifyOwners(Invoice invoice, InvoiceReminder.Kind kind) {
        String title;
        String body;
        String amount = java.math.BigDecimal.valueOf(invoice.getTotal()).movePointLeft(2).toPlainString() + " " + invoice.getCurrency();
        String due = invoice.getDueAt().atZone(ZoneOffset.UTC).toLocalDate().toString();
        switch (kind) {
            case BEFORE_DUE -> {
                title = "Invoice " + invoice.getInvoiceNumber() + " is due on " + due;
                body = "Invoice " + invoice.getInvoiceNumber() + " for " + amount + " is due on " + due + ". Please pay it before then.";
            }
            case ON_DUE -> {
                title = "Invoice " + invoice.getInvoiceNumber() + " is due today";
                body = "Invoice " + invoice.getInvoiceNumber() + " for " + amount + " is due on " + due + ". Please pay it today.";
            }
            default -> {
                title = "Invoice " + invoice.getInvoiceNumber() + " is overdue";
                body = "Invoice " + invoice.getInvoiceNumber() + " for " + amount + " was due on " + due + " and has not been paid. Please pay it now.";
            }
        }
        NotificationSeverity severity = kind == InvoiceReminder.Kind.AFTER_DUE ? NotificationSeverity.WARNING : NotificationSeverity.INFO;
        List<Customer> recipients = new ArrayList<>();
        if (invoice.getOwnerCustomerId() != null) {
            customerRepository.findById(invoice.getOwnerCustomerId()).ifPresent(recipients::add);
        } else if (invoice.getOwnerOrganizationId() != null) {
            memberRepository.findByOrganizationIdAndOrgRoleAndStatus(invoice.getOwnerOrganizationId(), OrgRole.ORG_ADMIN, MembershipStatus.ACTIVE)
                .forEach(m -> customerRepository.findById(m.getCustomerId()).ifPresent(recipients::add));
        }
        String billingEmail = billingDetailsEmail.forInvoice(invoice);
        for (Customer c : recipients) {
            // The billing e-mail receives the message once, through the first recipient; every recipient still gets the in-app notice.
            String email = c == recipients.get(0) && billingEmail != null ? billingEmail : c.getEmail();
            notificationService.notify(c.getId(), email, NotificationCategory.BILLING, severity, title, body);
        }
        log.info("Invoice {} {} reminder sent to {} recipient(s)", invoice.getId(), kind, recipients.size());
    }
}
