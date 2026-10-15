package com.vyoog.eisplatform.modules.billing.service;

import com.vyoog.eisplatform.modules.billing.dto.SaveSeatPricingDefaultsRequest;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.cart.dto.AddCartItemRequest;
import com.vyoog.eisplatform.modules.cart.dto.CartDto;
import com.vyoog.eisplatform.modules.cart.service.CartService;
import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionSeatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * C106: global and per-application seat pricing — inheritance/override resolution (SeatPricingService),
 * cart display (Total = base + per-seat price x seats, REQ-MKT seat pricing #4/#5), invoice generation
 * (base + seat-addon lines, #6) and that a product with seat pricing disabled is unaffected (#8).
 */
@SpringBootTest
@ActiveProfiles("test")
class SeatPricingTest {

    @Autowired private SeatPricingService seatPricingService;
    @Autowired private BillingSettingsService billingSettingsService;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductPlanRepository planRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberService organizationMemberService;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private InvoiceService invoiceService;
    @Autowired private CartService cartService;
    @Autowired private SubscriptionSeatService subscriptionSeatService;

    private Product newProduct(String name, String price, boolean seatPricingEnabled, String seatPrice) {
        Product product = new Product();
        product.setName(name + " " + System.nanoTime());
        product.setPrice(new BigDecimal(price));
        product.setStatus(ProductStatus.ACTIVE);
        product.setSeatPricingEnabled(seatPricingEnabled);
        if (seatPrice != null) {
            product.setSeatPrice(new BigDecimal(seatPrice));
            product.setSeatPriceCurrency(Currency.USD);
        }
        return productRepository.save(product);
    }

    private ProductPlan newPlan(Product product, String price) {
        ProductPlan plan = new ProductPlan();
        plan.setProduct(product);
        plan.setName("Pro");
        plan.setPrice(new BigDecimal(price));
        plan.setBillingPeriod(BillingPeriod.MONTHLY);
        plan.setCurrency(Currency.USD);
        return planRepository.save(plan);
    }

    private Customer newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("seat-" + System.nanoTime() + "@example.com");
        customer.setFirstName("Seat");
        customer.setLastName("User");
        return customerRepository.save(customer);
    }

    @Test
    void aProductsOwnSeatPriceOverridesTheGlobalDefaultWithoutChangingOtherProducts() {
        billingSettingsService.saveSeatPricingDefaults(null, new SaveSeatPricingDefaultsRequest(new BigDecimal("20.00"), Currency.USD));
        Product inheriting = newProduct("Inheriting", "200.00", true, null);
        Product overriding = newProduct("Overriding", "200.00", true, "35.00");

        assertThat(seatPricingService.effectiveSeatPrice(inheriting).orElseThrow().unitAmount()).isEqualTo(2000L);
        assertThat(seatPricingService.effectiveSeatPrice(overriding).orElseThrow().unitAmount()).isEqualTo(3500L);

        // Changing the global default updates the inheriting product but not the overriding one.
        billingSettingsService.saveSeatPricingDefaults(null, new SaveSeatPricingDefaultsRequest(new BigDecimal("25.00"), Currency.USD));
        assertThat(seatPricingService.effectiveSeatPrice(inheriting).orElseThrow().unitAmount()).isEqualTo(2500L);
        assertThat(seatPricingService.effectiveSeatPrice(overriding).orElseThrow().unitAmount()).isEqualTo(3500L);
    }

    @Test
    void aProductWithSeatPricingDisabledHasNoEffectiveSeatPrice() {
        billingSettingsService.saveSeatPricingDefaults(null, new SaveSeatPricingDefaultsRequest(new BigDecimal("20.00"), Currency.USD));
        Product product = newProduct("Legacy", "200.00", false, null);

        assertThat(seatPricingService.effectiveSeatPrice(product)).isEmpty();
    }

    @Test
    void theCartShowsBasePriceWithTheFirstSeatIncludedInTheTotal() {
        billingSettingsService.saveSeatPricingDefaults(null, new SaveSeatPricingDefaultsRequest(new BigDecimal("20.00"), Currency.USD));
        Customer customer = newCustomer();
        Product product = newProduct("Catalog", "200.00", true, null);
        ProductPlan plan = newPlan(product, "200.00");

        CartDto cart = cartService.addItem(customer.getId(), new AddCartItemRequest(product.getId(), plan.getId()));

        assertThat(cart.items().get(0).seatPricingEnabled()).isTrue();
        assertThat(cart.items().get(0).seatUnitPrice()).isEqualTo(2000L);
        assertThat(cart.items().get(0).baseAmount()).isEqualTo(20000L);
        assertThat(cart.items().get(0).seatAddonAmount()).isEqualTo(2000L);   // #3/#4: one seat included, not free
        assertThat(cart.items().get(0).amount()).isEqualTo(22000L);          // $200 + $20 = $220
    }

    @Test
    void aLegacyNonSeatPricingProductsCartAmountIsUnaffectedByTheFeature() {
        billingSettingsService.saveSeatPricingDefaults(null, new SaveSeatPricingDefaultsRequest(new BigDecimal("20.00"), Currency.USD));
        Customer customer = newCustomer();
        Product product = newProduct("Legacy", "0", false, null);
        ProductPlan plan = newPlan(product, "100.00");

        CartDto cart = cartService.addItem(customer.getId(), new AddCartItemRequest(product.getId(), plan.getId()));

        assertThat(cart.items().get(0).seatPricingEnabled()).isFalse();
        assertThat(cart.items().get(0).amount()).isEqualTo(10000L);   // exactly unitPrice x seats, as before C106
        assertThat(cart.items().get(0).seatAddonAmount()).isZero();
    }

    @Test
    void anOrganizationsInitialInvoiceHasABaseLineAndASeatAddonLineForFiveSeats() {
        billingSettingsService.saveSeatPricingDefaults(null, new SaveSeatPricingDefaultsRequest(new BigDecimal("20.00"), Currency.USD));
        Customer admin = newCustomer();
        Organization organization = new Organization();
        organization.setName("Acme " + System.nanoTime());
        organization.setCode("SEAT-" + System.nanoTime());
        organization.setCountry("India");
        organization.setBusinessEmail("acme-" + System.nanoTime() + "@example.com");
        organization.setLicensedSeats(5);
        organization = organizationRepository.save(organization);
        organizationMemberService.addMember(organization.getId(), admin.getId(), OrgRole.ORG_ADMIN);

        Product product = newProduct("Suite", "200.00", true, null);

        ProductSubscription subscription = new ProductSubscription();
        subscription.setProductId(product.getId());
        subscription.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        subscription.setOwnerOrganizationId(organization.getId());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setQuantity(5);
        subscription.setStartedAt(Instant.now());
        subscription.setExpiresAt(Instant.now().plusSeconds(30L * 86_400));
        subscription = subscriptionRepository.save(subscription);

        Invoice invoice = invoiceService.generateForSubscription(subscription.getId());

        assertThat(invoice.getLines()).hasSize(2);
        assertThat(invoice.getLines().get(0).getQuantity()).isEqualTo(1);
        assertThat(invoice.getLines().get(0).getAmount()).isEqualTo(20000L);     // base: $200
        assertThat(invoice.getLines().get(1).getQuantity()).isEqualTo(5);
        assertThat(invoice.getLines().get(1).getAmount()).isEqualTo(10000L);     // 5 seats x $20
        assertThat(invoice.getSubtotal()).isEqualTo(30000L);                     // #5: 200 + 5 x 20 = 300
    }

    @Test
    void aMidTermSeatIncreaseBillsOnlyTheProratedSeatAddonForASeatPricingEnabledProduct() {
        billingSettingsService.saveSeatPricingDefaults(null, new SaveSeatPricingDefaultsRequest(new BigDecimal("20.00"), Currency.USD));
        Customer admin = newCustomer();
        Organization organization = new Organization();
        organization.setName("Beta " + System.nanoTime());
        organization.setCode("SEAT2-" + System.nanoTime());
        organization.setCountry("India");
        organization.setBusinessEmail("beta-" + System.nanoTime() + "@example.com");
        organization.setLicensedSeats(10);
        organization = organizationRepository.save(organization);
        organizationMemberService.addMember(organization.getId(), admin.getId(), OrgRole.ORG_ADMIN);

        Product product = newProduct("Suite2", "200.00", true, null);
        ProductSubscription subscription = new ProductSubscription();
        subscription.setProductId(product.getId());
        subscription.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        subscription.setOwnerOrganizationId(organization.getId());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setQuantity(2);
        subscription.setStartedAt(Instant.now());
        subscription.setExpiresAt(Instant.now().plusSeconds(30L * 86_400));
        subscription = subscriptionRepository.save(subscription);

        Invoice invoice = invoiceService.generateSeatIncrease(subscription.getId(), 3, 30, 30);

        assertThat(invoice).isNotNull();
        assertThat(invoice.getLines()).hasSize(1);
        assertThat(invoice.getLines().get(0).getQuantity()).isEqualTo(3);
        assertThat(invoice.getLines().get(0).getAmount()).isEqualTo(6000L);   // 3 added seats x $20, full remaining term
    }
}
