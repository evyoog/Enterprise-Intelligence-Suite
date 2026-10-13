package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceReminder;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceReminderRepository;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayClient;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxTestSupport;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/** C97 BI-2: reminders 3 days before the due date, on the due date, and 7 days after; each once. */
@SpringBootTest
@ActiveProfiles("test")
@Import(TaxTestSupport.class)
class InvoiceReminderServiceTest {

    @Autowired private TaxTestSupport tax;
    @Autowired private InvoiceReminderService reminders;
    @Autowired private InvoiceReminderRepository reminderRepository;
    @Autowired private InvoiceService invoiceService;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @MockBean private RazorpayClient razorpayClient;

    @Test
    void theKindDependsOnTheDaysLeftToTheDueDate() {
        LocalDate due = LocalDate.of(2026, 11, 15);
        assertThat(InvoiceReminderService.kindFor(due.minusDays(10), due)).isNull();
        assertThat(InvoiceReminderService.kindFor(due.minusDays(4), due)).isNull();
        assertThat(InvoiceReminderService.kindFor(due.minusDays(3), due)).isEqualTo(InvoiceReminder.Kind.BEFORE_DUE);
        assertThat(InvoiceReminderService.kindFor(due.minusDays(1), due)).isEqualTo(InvoiceReminder.Kind.BEFORE_DUE);
        assertThat(InvoiceReminderService.kindFor(due, due)).isEqualTo(InvoiceReminder.Kind.ON_DUE);
        assertThat(InvoiceReminderService.kindFor(due.plusDays(6), due)).isEqualTo(InvoiceReminder.Kind.ON_DUE);
        assertThat(InvoiceReminderService.kindFor(due.plusDays(7), due)).isEqualTo(InvoiceReminder.Kind.AFTER_DUE);
        assertThat(InvoiceReminderService.kindFor(due.plusDays(30), due)).isEqualTo(InvoiceReminder.Kind.AFTER_DUE);
    }

    private Invoice openInvoice() {
        Customer c = new Customer();
        c.setEmail("remind-" + System.nanoTime() + "@example.com");
        c.setFirstName("Re");
        c.setLastName("Mind");
        c = customerRepository.save(c);
        tax.readyToInvoiceInIndia(c);
        Product p = new Product();
        p.setName("Reminder Product " + System.nanoTime());
        p.setPrice(BigDecimal.valueOf(300));
        p.setStatus(ProductStatus.ACTIVE);
        p = productRepository.save(p);
        ProductSubscription s = new ProductSubscription();
        s.setProductId(p.getId());
        s.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
        s.setOwnerCustomerId(c.getId());
        s.setStatus(SubscriptionStatus.ACTIVE);
        return invoiceService.generateForSubscription(subscriptionRepository.save(s).getId());
    }

    @Test
    void eachReminderIsSentOnceAndStopsWhenTheInvoiceIsPaid() {
        Invoice invoice = openInvoice();
        Instant due = Instant.parse("2026-12-20T09:00:00Z");
        invoice.setDueAt(due);
        invoiceRepository.save(invoice);
        Long id = invoice.getId();

        reminders.sendDue(due.minus(10, ChronoUnit.DAYS));
        assertThat(reminderRepository.findByInvoiceIdOrderBySentAtAsc(id)).isEmpty();

        reminders.sendDue(due.minus(3, ChronoUnit.DAYS));
        reminders.sendDue(due.minus(2, ChronoUnit.DAYS));   // the same kind is not sent again
        assertThat(reminderRepository.findByInvoiceIdOrderBySentAtAsc(id)).extracting(InvoiceReminder::getKind)
            .containsExactly(InvoiceReminder.Kind.BEFORE_DUE);

        reminders.sendDue(due);
        reminders.sendDue(due.plus(7, ChronoUnit.DAYS));
        assertThat(reminderRepository.findByInvoiceIdOrderBySentAtAsc(id)).extracting(InvoiceReminder::getKind)
            .containsExactly(InvoiceReminder.Kind.BEFORE_DUE, InvoiceReminder.Kind.ON_DUE, InvoiceReminder.Kind.AFTER_DUE);
    }

    @Test
    void aPaidInvoiceGetsNoReminder() {
        Invoice invoice = openInvoice();
        Instant due = Instant.parse("2026-12-20T09:00:00Z");
        invoice.setDueAt(due);
        invoiceRepository.save(invoice);
        invoiceService.markPaid(invoice);

        reminders.sendDue(due);

        assertThat(reminderRepository.findByInvoiceIdOrderBySentAtAsc(invoice.getId())).isEmpty();
    }
}
