package com.vyoog.eisplatform.modules.dashboard.service;

import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.repository.BillingDetailsRepository;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.billing.service.BillingDetailsService;
import com.vyoog.eisplatform.modules.dashboard.repository.ProductUsageRepository;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import com.vyoog.eisplatform.modules.reviews.repository.ProductReviewRepository;
import com.vyoog.eisplatform.modules.support.repository.SupportTicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * C54: DemoDataSeeder is {@code @Profile("!test")} so it never runs under
 * this suite's own {@code @ActiveProfiles("test")} — this test instantiates
 * it directly (bypassing Spring's profile-gated bean registration entirely,
 * which only governs whether Spring itself creates the bean, not whether a
 * test can `new` it) to verify its enrichment logic and idempotency against
 * the real H2 test database.
 */
@SpringBootTest
@ActiveProfiles("test")
// A manually-`new`'d DemoDataSeeder has no Spring AOP proxy around it, so
// its own @Transactional on run() never applies — its @Modifying backdate
// query needs SOME active transaction to run under, which this annotation
// provides (via the test's own transaction manager); rolling back after
// each test is a welcome side effect, not just a workaround.
@Transactional
class DemoDataSeederTest {

    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberRepository organizationMemberRepository;
    @Autowired private OrganizationProductAccessRepository organizationProductAccessRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private ProductUsageRepository productUsageRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductPlanRepository productPlanRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private BillingDetailsRepository billingDetailsRepository;
    @Autowired private BillingDetailsService billingDetailsService;
    @Autowired private SupportTicketRepository supportTicketRepository;
    @Autowired private ProductReviewRepository productReviewRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AuditService auditService;
    @Autowired private OrganizationMemberService organizationMemberService;

    private DemoDataSeeder newSeeder() {
        return new DemoDataSeeder(
            organizationRepository, organizationMemberRepository, organizationProductAccessRepository,
            subscriptionRepository, productUsageRepository, productRepository, productPlanRepository,
            invoiceRepository, billingDetailsRepository, billingDetailsService,
            supportTicketRepository, productReviewRepository, customerRepository, auditService
        );
    }

    private Organization newOrganization() {
        Organization organization = new Organization();
        organization.setName("Demo Seeder Test Org");
        organization.setCode("DEMOSEED-" + System.nanoTime());
        organization.setBusinessEmail("demoseed@example.com");
        organization.setCountry("India");
        organization.setLicensedSeats(5);
        return organizationRepository.save(organization);
    }

    private Product newProduct() {
        Product product = new Product();
        product.setName("DemoSeed-" + System.nanoTime());
        product.setPrice(BigDecimal.valueOf(19.99));
        product.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(product);
    }

    @Test
    void seedsUsageForActiveProductAccessAndNeverOverwritesRealUsage() {
        Organization org = newOrganization();
        Product product = newProduct();
        var admin = organizationMemberService.addMember(org.getId(), newCustomer(), OrgRole.ORG_ADMIN);
        organizationProductAccessRepository.save(access(admin.getId(), product.getId()));

        assertThat(productUsageRepository.findByCustomerIdAndProductId(admin.getCustomerId(), product.getId())).isEmpty();

        newSeeder().run(null);

        var seeded = productUsageRepository.findByCustomerIdAndProductId(admin.getCustomerId(), product.getId());
        assertThat(seeded).isPresent();
        assertThat(seeded.get().getLaunchCount()).isGreaterThan(0);

        // Idempotent: a second run must not change an already-seeded row.
        long launchCountAfterFirstRun = seeded.get().getLaunchCount();
        newSeeder().run(null);
        assertThat(productUsageRepository.findByCustomerIdAndProductId(admin.getCustomerId(), product.getId()).get().getLaunchCount())
            .isEqualTo(launchCountAfterFirstRun);
    }

    @Test
    void seedsTwoBackdatedPaidInvoicesForAnActiveOrgSubscriptionWithNoHistoryYet() {
        Organization org = newOrganization();
        Product product = newProduct();
        ProductSubscription newSubscription = new ProductSubscription();
        newSubscription.setProductId(product.getId());
        newSubscription.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        newSubscription.setOwnerOrganizationId(org.getId());
        newSubscription.setStatus(SubscriptionStatus.ACTIVE);
        newSubscription.setStartedAt(Instant.now());
        final ProductSubscription subscription = subscriptionRepository.save(newSubscription);

        newSeeder().run(null);

        var invoices = invoiceRepository.findByOwnerOrganizationIdAndStatus(org.getId(), InvoiceStatus.PAID).stream()
            .filter(inv -> inv.getSubscriptionId().equals(subscription.getId())).toList();
        assertThat(invoices).hasSize(2);
        assertThat(invoices).allMatch(inv -> inv.getTotal() == 1999);
        assertThat(invoices).allMatch(inv -> inv.getInvoiceNumber() != null && inv.getInvoiceNumber().startsWith("INV-"));
        assertThat(invoices).allMatch(inv -> inv.getIssuedAt().isBefore(Instant.now().minus(java.time.Duration.ofDays(25))));

        // Idempotent: a subscription that already has invoices is never seeded again.
        newSeeder().run(null);
        var invoicesAfterSecondRun = invoiceRepository.findByOwnerOrganizationIdAndStatus(org.getId(), InvoiceStatus.PAID).stream()
            .filter(inv -> inv.getSubscriptionId().equals(subscription.getId())).toList();
        assertThat(invoicesAfterSecondRun).hasSize(2);
    }

    @Test
    void topsUpSupportTicketsAndReviewsOnlyUpToTheirFloorEvenAcrossRepeatedRuns() {
        long ticketsBefore = supportTicketRepository.count();
        long reviewsBefore = productReviewRepository.count();

        newSeeder().run(null);
        long ticketsAfterFirst = supportTicketRepository.count();
        long reviewsAfterFirst = productReviewRepository.count();

        newSeeder().run(null);
        assertThat(supportTicketRepository.count()).isEqualTo(ticketsAfterFirst);
        assertThat(productReviewRepository.count()).isEqualTo(reviewsAfterFirst);

        assertThat(ticketsAfterFirst).isGreaterThanOrEqualTo(ticketsBefore);
        assertThat(reviewsAfterFirst).isGreaterThanOrEqualTo(reviewsBefore);
    }

    private Long newCustomer() {
        var customer = new com.vyoog.eisplatform.modules.registration.model.Customer();
        customer.setEmail("demoseed-" + System.nanoTime() + "@example.com");
        customer.setFirstName("Seed");
        customer.setLastName("Test");
        return customerRepository.save(customer).getId();
    }

    private OrganizationProductAccess access(Long organizationMemberId, Long productId) {
        OrganizationProductAccess access = new OrganizationProductAccess();
        access.setOrganizationMemberId(organizationMemberId);
        access.setProductId(productId);
        access.setProductRole("ADMIN");
        access.setStatus(MembershipStatus.ACTIVE);
        return access;
    }
}
