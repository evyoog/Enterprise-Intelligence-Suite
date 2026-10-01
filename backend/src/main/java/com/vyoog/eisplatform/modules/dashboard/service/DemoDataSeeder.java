package com.vyoog.eisplatform.modules.dashboard.service;

import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceLine;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.repository.BillingDetailsRepository;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.billing.service.BillingDetailsService;
import com.vyoog.eisplatform.modules.dashboard.model.ProductUsage;
import com.vyoog.eisplatform.modules.dashboard.repository.ProductUsageRepository;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.reviews.model.ProductReview;
import com.vyoog.eisplatform.modules.reviews.model.ReviewStatus;
import com.vyoog.eisplatform.modules.reviews.repository.ProductReviewRepository;
import com.vyoog.eisplatform.modules.support.model.SupportTicket;
import com.vyoog.eisplatform.modules.support.model.TicketPriority;
import com.vyoog.eisplatform.modules.support.model.TicketStatus;
import com.vyoog.eisplatform.modules.support.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Decision C54 (2026-10-01): the business dashboard and the platform admin
 * dashboard (C53) both read real data — this seeder makes some exist, for
 * dev/demo use, per {@code database/seed/README.md}. It never fabricates an
 * organization, a customer's registration, or a subscription itself; it only
 * ENRICHES what already genuinely exists (a real org, a real org-owned
 * subscription, a real member with real product access) with the usage and
 * billing history that a platform running for months would naturally have
 * accumulated. Every insert is idempotent (checked before written) and runs
 * on every startup, same pattern as {@link com.vyoog.eisplatform.modules.product.service.CatalogSeeder}
 * and {@code RbacSeeder}. See {@code docs/07-database/demo-data.md} for the
 * exact scope and the "never point this at a database with real customer
 * data" warning.
 *
 * <p>Runs after {@code CatalogSeeder} (implicit ordering via {@code @Order}
 * is not declared — this seeder only reads the catalog, product
 * subscriptions and org membership that other flows (registration, catalog
 * seeding, self-serve subscribe) create first; on a fresh database with none
 * of that yet, every step below is a safe no-op).
 */
// Never runs under the "test" profile (@ActiveProfiles("test") everywhere
// in this suite) — demo tickets/reviews would otherwise land in the shared
// H2 test database and could skew exact-count assertions in modules this
// seeder has no reason to touch during automated tests. CatalogSeeder and
// RbacSeeder don't need this: they seed foundational reference data
// (the catalog, roles/permissions) many tests actually depend on existing.
@Profile("!test")
@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    private static final int DEMO_TICKET_FLOOR = 5;
    private static final int DEMO_REVIEW_FLOOR = 4;
    private static final String DEMO_CUSTOMER_EMAIL = "demo.user@eis-demo.vyoog.local";

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationProductAccessRepository organizationProductAccessRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final ProductUsageRepository productUsageRepository;
    private final ProductRepository productRepository;
    private final ProductPlanRepository productPlanRepository;
    private final InvoiceRepository invoiceRepository;
    private final BillingDetailsRepository billingDetailsRepository;
    private final BillingDetailsService billingDetailsService;
    private final SupportTicketRepository supportTicketRepository;
    private final ProductReviewRepository productReviewRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedProductUsage();
        seedDemoInvoices();
        Customer demoCustomer = demoCustomer();
        seedDemoTickets(demoCustomer);
        seedDemoReviews(demoCustomer);
    }

    /** Every ACTIVE member's ACTIVE product access gets a real, if synthetic,
     * launch history — without this, "Launches by application" (business
     * dashboard) and "Most-launched products" (platform admin dashboard)
     * stay empty for an organization that hasn't actually clicked Launch
     * yet. Skips any (customer, product) pair that already has a
     * {@link ProductUsage} row — real usage, once it exists, is never
     * overwritten. */
    private void seedProductUsage() {
        for (Organization organization : organizationRepository.findAll()) {
            List<OrganizationMember> activeMembers = organizationMemberRepository.findByOrganizationId(organization.getId()).stream()
                .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
                .toList();
            if (activeMembers.isEmpty()) {
                continue;
            }
            Map<Long, Long> memberIdToCustomerId = activeMembers.stream()
                .collect(Collectors.toMap(OrganizationMember::getId, OrganizationMember::getCustomerId));
            List<OrganizationProductAccess> accessRows = organizationProductAccessRepository
                .findByOrganizationMemberIdIn(memberIdToCustomerId.keySet()).stream()
                .filter(a -> a.getStatus() == MembershipStatus.ACTIVE)
                .toList();
            for (OrganizationProductAccess access : accessRows) {
                Long customerId = memberIdToCustomerId.get(access.getOrganizationMemberId());
                if (customerId == null || productUsageRepository.findByCustomerIdAndProductId(customerId, access.getProductId()).isPresent()) {
                    continue;
                }
                ProductUsage usage = new ProductUsage();
                usage.setCustomerId(customerId);
                usage.setProductId(access.getProductId());
                long seed = hash(customerId, access.getProductId());
                usage.setLaunchCount(3 + (seed % 55));
                usage.setLastLaunchedAt(Instant.now().minus(seed % 14, ChronoUnit.DAYS));
                productUsageRepository.save(usage);
            }
        }
    }

    /** Backfills two months of PAID invoices for every ACTIVE, paid,
     * organization-owned subscription that has none yet — real invoice
     * generation (REQ-BIL-001.2) only ever creates one invoice going
     * forward from today, so a freshly-subscribed organization has no
     * billing HISTORY to show a this-period-vs-last-period trend with.
     * Skips any subscription that already has at least one invoice (its
     * own real history, or a previous run of this seeder). Amounts, line
     * description and bill-to snapshot mirror {@code InvoiceService#generateForSubscription}
     * exactly; only the issue date (backdated — see
     * {@code InvoiceRepository#backdateIssuedAtForDemoData}) and the
     * already-PAID status differ, since these represent invoices a demo
     * organization would already have settled months ago. */
    private void seedDemoInvoices() {
        for (Organization organization : organizationRepository.findAll()) {
            List<ProductSubscription> subscriptions = subscriptionRepository.findByOwnerOrganizationId(organization.getId()).stream()
                .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
                .toList();
            for (ProductSubscription subscription : subscriptions) {
                boolean hasAnyInvoice = invoiceRepository.findByOwnerOrganizationIdAndStatusIn(organization.getId(), List.of(InvoiceStatus.values()))
                    .stream().anyMatch(inv -> inv.getSubscriptionId().equals(subscription.getId()));
                if (hasAnyInvoice) {
                    continue;
                }
                Product product = productRepository.findById(subscription.getProductId()).orElse(null);
                if (product == null) {
                    continue;
                }
                ProductPlan plan = subscription.getPlanId() == null ? null
                    : productPlanRepository.findById(subscription.getPlanId()).orElse(null);
                BigDecimal price = plan != null ? plan.getPrice() : product.getPrice();
                long amount = toMinorUnits(price);
                if (amount <= 0) {
                    continue;
                }
                Currency currency = plan != null ? plan.getCurrency() : Currency.USD;
                String description = product.getName() + (plan != null ? " — " + plan.getName() : "");
                var billingDetails = billingDetailsRepository.findByOwnerOrganizationId(organization.getId()).orElse(null);
                String billToSnapshot = billingDetailsService.snapshotFor(billingDetails);

                int seededInvoices = 0;
                for (int monthsAgo = 2; monthsAgo >= 1; monthsAgo--) {
                    Instant issuedAt = Instant.now().minus(30L * monthsAgo, ChronoUnit.DAYS);
                    Invoice invoice = new Invoice();
                    invoice.setSubscriptionId(subscription.getId());
                    invoice.setOwnerOrganizationId(organization.getId());
                    invoice.setStatus(InvoiceStatus.PAID);
                    invoice.setCurrency(currency);
                    invoice.setSubtotal(amount);
                    invoice.setTaxAmount(0);
                    invoice.setTotal(amount);
                    invoice.setPeriodStart(issuedAt);
                    invoice.setPeriodEnd(issuedAt.plus(30, ChronoUnit.DAYS));
                    invoice.setBillToSnapshot(billToSnapshot);
                    invoice = invoiceRepository.save(invoice);

                    InvoiceLine line = new InvoiceLine();
                    line.setInvoice(invoice);
                    line.setDescription(description);
                    line.setPeriodStart(invoice.getPeriodStart());
                    line.setPeriodEnd(invoice.getPeriodEnd());
                    line.setQuantity(1);
                    line.setUnitAmount(amount);
                    line.setAmount(amount);
                    invoice.getLines().add(line);
                    invoice = invoiceRepository.save(invoice);

                    int year = issuedAt.atZone(java.time.ZoneOffset.UTC).getYear();
                    invoice.setInvoiceNumber("INV-" + year + "-" + String.format("%06d", invoice.getId()));
                    invoiceRepository.save(invoice);
                    invoiceRepository.backdateIssuedAtForDemoData(invoice.getId(), issuedAt);
                    seededInvoices++;
                }
                if (seededInvoices > 0) {
                    auditService.recordSuccess("DEMO_DATA_SEEDED", null, null, null,
                        "Organization", organization.getId().toString(), organization.getId(),
                        seededInvoices + " demo paid invoice(s) backfilled for " + product.getName());
                }
            }
        }
    }

    private long toMinorUnits(BigDecimal price) {
        return price.multiply(BigDecimal.valueOf(100)).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private Customer demoCustomer() {
        return customerRepository.findByEmailIgnoreCase(DEMO_CUSTOMER_EMAIL).orElseGet(() -> {
            Customer customer = new Customer();
            customer.setEmail(DEMO_CUSTOMER_EMAIL);
            customer.setFirstName("Demo");
            customer.setLastName("User");
            customer.setStatus(RegistrationStatus.COMPLETED);
            return customerRepository.save(customer);
        });
    }

    /** Only tops the platform up to {@link #DEMO_TICKET_FLOOR} tickets total
     * — a platform that already has real tickets (from actually using
     * Support, sprint 2027.1.2) keeps them untouched; this never adds more
     * once that floor is reached, including on a later restart. */
    private void seedDemoTickets(Customer demoCustomer) {
        record DemoTicket(String subject, String description, TicketStatus status, TicketPriority priority) {
        }
        List<DemoTicket> demoTickets = List.of(
            new DemoTicket("Demo: Cannot access the product catalog", "Getting a blank page when opening /products.", TicketStatus.OPEN, TicketPriority.HIGH),
            new DemoTicket("Demo: Invoice download link is broken", "The PDF link on invoice INV-2026-000012 returns a 404.", TicketStatus.OPEN, TicketPriority.MEDIUM),
            new DemoTicket("Demo: Request to add 5 more licensed seats", "Our team grew — can we add seats to the current plan?", TicketStatus.IN_PROGRESS, TicketPriority.LOW),
            new DemoTicket("Demo: SSO login fails intermittently", "About 1 in 10 sign-ins redirect back to the login page.", TicketStatus.ESCALATED, TicketPriority.HIGH),
            new DemoTicket("Demo: Question about usage limits on the Pro plan", "Where can we see how close we are to our plan's usage limit?", TicketStatus.RESOLVED, TicketPriority.LOW)
        );
        for (DemoTicket demo : demoTickets) {
            if (supportTicketRepository.existsBySubject(demo.subject())) {
                continue;
            }
            SupportTicket ticket = new SupportTicket();
            ticket.setRequestedByCustomerId(demoCustomer.getId());
            ticket.setSubject(demo.subject());
            ticket.setDescription(demo.description());
            ticket.setStatus(demo.status());
            ticket.setPriority(demo.priority());
            supportTicketRepository.save(ticket);
        }
    }

    /** Only tops the platform up to {@link #DEMO_REVIEW_FLOOR} reviews total
     * — same floor-not-fixed-count reasoning as {@link #seedDemoTickets}.
     * One review per distinct ACTIVE product (the schema allows only one
     * review per (product, customer) pair), so this never collides with a
     * real customer's own review of the same product. */
    private void seedDemoReviews(Customer demoCustomer) {
        if (productReviewRepository.count() >= DEMO_REVIEW_FLOOR) {
            return;
        }
        record DemoReview(int rating, String comment, ReviewStatus status) {
        }
        List<DemoReview> demoReviews = List.of(
            new DemoReview(5, "Demo: Exactly what our team needed — easy to roll out.", ReviewStatus.APPROVED),
            new DemoReview(4, "Demo: Solid product, the admin screens could use more filters.", ReviewStatus.APPROVED),
            new DemoReview(3, "Demo: Works well but documentation is thin in places.", ReviewStatus.PENDING),
            new DemoReview(5, "Demo: Support was fast when we had an onboarding question.", ReviewStatus.PENDING)
        );
        List<Product> activeProducts = productRepository.findByStatus(ProductStatus.ACTIVE);
        for (int i = 0; i < demoReviews.size() && i < activeProducts.size(); i++) {
            Product product = activeProducts.get(i);
            if (productReviewRepository.findByProductIdAndCustomerId(product.getId(), demoCustomer.getId()).isPresent()) {
                continue;
            }
            DemoReview demo = demoReviews.get(i);
            ProductReview review = new ProductReview();
            review.setProductId(product.getId());
            review.setCustomerId(demoCustomer.getId());
            review.setRating(demo.rating());
            review.setComment(demo.comment());
            review.setStatus(demo.status());
            productReviewRepository.save(review);
        }
    }

    private long hash(Long a, Long b) {
        long h = (a == null ? 0 : a) * 31L + (b == null ? 0 : b);
        return Math.abs(h);
    }
}
