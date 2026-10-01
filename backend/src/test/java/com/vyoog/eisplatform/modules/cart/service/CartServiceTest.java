package com.vyoog.eisplatform.modules.cart.service;

import com.vyoog.eisplatform.common.exception.CartConflictException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.billing.service.CheckoutService;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayClient;
import com.vyoog.eisplatform.modules.cart.dto.AddCartItemRequest;
import com.vyoog.eisplatform.modules.cart.dto.CartCheckoutResultDto;
import com.vyoog.eisplatform.modules.cart.dto.CartDto;
import com.vyoog.eisplatform.modules.cart.dto.CartIssueDto;
import com.vyoog.eisplatform.modules.cart.dto.UpdateCartItemRequest;
import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrderStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrderRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** C59, REQ-MKT-003: cart, purchase validation (BR-6) and checkout (BR-9, BR-10). */
@SpringBootTest
@ActiveProfiles("test")
class CartServiceTest {

    @Autowired private CartService cartService;
    @Autowired private CheckoutService checkoutService;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductPlanRepository planRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberService organizationMemberService;

    @MockBean private RazorpayClient razorpayClient;

    private Customer newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("cart-" + System.nanoTime() + "@example.com");
        customer.setFirstName("Cart");
        customer.setLastName("User");
        return customerRepository.save(customer);
    }

    private Product newProduct(String name) {
        Product product = new Product();
        product.setName(name + " " + System.nanoTime());
        product.setPrice(BigDecimal.ZERO);
        product.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(product);
    }

    private ProductPlan newPlan(Product product, String name, BillingPeriod period, String price) {
        ProductPlan plan = new ProductPlan();
        plan.setProduct(product);
        plan.setName(name);
        plan.setPrice(new BigDecimal(price));
        plan.setBillingPeriod(period);
        plan.setCurrency(Currency.INR);
        return planRepository.save(plan);
    }

    @Test
    void buyingAddsTheProductOnceAndASecondPlanReplacesTheFirst() {
        Customer customer = newCustomer();
        Product product = newProduct("Valam");
        ProductPlan monthly = newPlan(product, "Monthly", BillingPeriod.MONTHLY, "1000.00");
        ProductPlan yearly = newPlan(product, "Yearly", BillingPeriod.YEARLY, "10000.00");

        cartService.addItem(customer.getId(), new AddCartItemRequest(product.getId(), monthly.getId()));
        CartDto cart = cartService.addItem(customer.getId(), new AddCartItemRequest(product.getId(), yearly.getId()));

        assertThat(cart.itemCount()).isEqualTo(1);
        assertThat(cart.items().get(0).planId()).isEqualTo(yearly.getId());
        assertThat(cart.total()).isEqualTo(1_000_000L);
        assertThat(cart.currency()).isEqualTo("INR");
        assertThat(cart.continueAs()).isEqualTo(CartService.INDIVIDUAL);
        assertThat(cart.taxCalculatedAtPayment()).isTrue();
        assertThat(cart.items().get(0).plans()).extracting("id").containsExactly(monthly.getId(), yearly.getId());
    }

    @Test
    void freePlansAndPlansOfAnotherProductAreRefused() {
        Customer customer = newCustomer();
        Product product = newProduct("Free");
        ProductPlan free = newPlan(product, "Free", BillingPeriod.MONTHLY, "0");
        Product other = newProduct("Other");
        ProductPlan otherPlan = newPlan(other, "Pro", BillingPeriod.MONTHLY, "10.00");

        assertThatThrownBy(() -> cartService.addItem(customer.getId(), new AddCartItemRequest(product.getId(), free.getId())))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> cartService.addItem(customer.getId(), new AddCartItemRequest(product.getId(), otherPlan.getId())))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anotherUsersItemIsNotFound() {
        Customer owner = newCustomer();
        Customer other = newCustomer();
        Product product = newProduct("Mine");
        ProductPlan plan = newPlan(product, "Pro", BillingPeriod.MONTHLY, "10.00");
        Long itemId = cartService.addItem(owner.getId(), new AddCartItemRequest(product.getId(), plan.getId())).items().get(0).id();
        cartService.addItem(other.getId(), new AddCartItemRequest(product.getId(), plan.getId()));

        assertThatThrownBy(() -> cartService.removeItem(other.getId(), itemId)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> cartService.updateItem(other.getId(), itemId, new UpdateCartItemRequest(null, true)))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThat(cartService.getCart(owner.getId()).itemCount()).isEqualTo(1);
    }

    @Test
    void validationReportsRetiredProductsChangedPricesActiveSubscriptionsAndMissingDependencies() {
        Customer customer = newCustomer();
        Product retired = newProduct("Retired");
        ProductPlan retiredPlan = newPlan(retired, "Pro", BillingPeriod.MONTHLY, "10.00");
        Product repriced = newProduct("Repriced");
        ProductPlan repricedPlan = newPlan(repriced, "Pro", BillingPeriod.MONTHLY, "1000.00");
        Product owned = newProduct("Owned");
        ProductPlan ownedPlan = newPlan(owned, "Pro", BillingPeriod.MONTHLY, "10.00");
        Product base = newProduct("Base");
        Product dependent = newProduct("Dependent");
        dependent.getDependsOn().add(base);
        dependent = productRepository.save(dependent);
        ProductPlan dependentPlan = newPlan(dependent, "Pro", BillingPeriod.MONTHLY, "10.00");

        Long retiredItem = cartService.addItem(customer.getId(), new AddCartItemRequest(retired.getId(), retiredPlan.getId())).items().get(0).id();
        cartService.addItem(customer.getId(), new AddCartItemRequest(repriced.getId(), repricedPlan.getId()));
        cartService.addItem(customer.getId(), new AddCartItemRequest(owned.getId(), ownedPlan.getId()));
        cartService.addItem(customer.getId(), new AddCartItemRequest(dependent.getId(), dependentPlan.getId()));

        retired.setStatus(ProductStatus.RETIRED);
        productRepository.save(retired);
        repricedPlan.setPrice(new BigDecimal("1200.00"));
        planRepository.save(repricedPlan);
        ProductSubscription subscription = new ProductSubscription();
        subscription.setProductId(owned.getId());
        subscription.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
        subscription.setOwnerCustomerId(customer.getId());
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(subscription);

        List<CartIssueDto> issues = cartService.validate(customer.getId()).issues();

        assertThat(issues).extracting(CartIssueDto::code)
            .containsExactlyInAnyOrder("NOT_AVAILABLE", "PRICE_CHANGED", "ALREADY_SUBSCRIBED", "MISSING_DEPENDENCY");
        CartIssueDto price = issues.stream().filter(i -> i.code().equals("PRICE_CHANGED")).findFirst().orElseThrow();
        assertThat(price.oldPrice()).isEqualTo(100_000L);
        assertThat(price.newPrice()).isEqualTo(120_000L);
        CartIssueDto dependency = issues.stream().filter(i -> i.code().equals("MISSING_DEPENDENCY")).findFirst().orElseThrow();
        assertThat(dependency.requiredProductId()).isEqualTo(base.getId());
        assertThat(issues.stream().filter(i -> i.itemId().equals(retiredItem)).findFirst().orElseThrow().reason()).isEqualTo("PRODUCT");

        assertThatThrownBy(() -> cartService.checkout(customer.getId()))
            .isInstanceOf(CartConflictException.class)
            .satisfies(e -> assertThat(((CartConflictException) e).getCode()).isEqualTo("CART_INVALID"));
        assertThat(cartService.getCart(customer.getId()).itemCount()).isEqualTo(4);
    }

    @Test
    void confirmingTheNewPriceClearsThePriceIssue() {
        Customer customer = newCustomer();
        Product product = newProduct("Price");
        ProductPlan plan = newPlan(product, "Pro", BillingPeriod.MONTHLY, "10.00");
        Long itemId = cartService.addItem(customer.getId(), new AddCartItemRequest(product.getId(), plan.getId())).items().get(0).id();
        plan.setPrice(new BigDecimal("12.00"));
        planRepository.save(plan);
        assertThat(cartService.validate(customer.getId()).valid()).isFalse();

        CartDto cart = cartService.updateItem(customer.getId(), itemId, new UpdateCartItemRequest(null, true));

        assertThat(cart.items().get(0).unitPriceAtAdd()).isEqualTo(1200L);
        assertThat(cartService.validate(customer.getId()).valid()).isTrue();
    }

    @Test
    void aRequiredProductInTheSameCartIsNotAMissingDependency() {
        Customer customer = newCustomer();
        Product base = newProduct("Base");
        ProductPlan basePlan = newPlan(base, "Pro", BillingPeriod.MONTHLY, "10.00");
        Product dependent = newProduct("Dependent");
        dependent.getDependsOn().add(base);
        dependent = productRepository.save(dependent);
        ProductPlan dependentPlan = newPlan(dependent, "Pro", BillingPeriod.MONTHLY, "10.00");
        cartService.addItem(customer.getId(), new AddCartItemRequest(dependent.getId(), dependentPlan.getId()));
        cartService.addItem(customer.getId(), new AddCartItemRequest(base.getId(), basePlan.getId()));

        assertThat(cartService.validate(customer.getId()).valid()).isTrue();
    }

    @Test
    void anIndividualsCheckoutBillsEveryItemOnOneInvoiceEmptiesTheCartAndIsIdempotent() {
        Customer customer = newCustomer();
        Product a = newProduct("A");
        ProductPlan aPlan = newPlan(a, "Pro", BillingPeriod.MONTHLY, "100.00");
        Product b = newProduct("B");
        ProductPlan bPlan = newPlan(b, "Team", BillingPeriod.YEARLY, "250.50");
        cartService.addItem(customer.getId(), new AddCartItemRequest(a.getId(), aPlan.getId()));
        cartService.addItem(customer.getId(), new AddCartItemRequest(b.getId(), bPlan.getId()));

        CartCheckoutResultDto result = cartService.checkout(customer.getId());
        CartCheckoutResultDto again = cartService.checkout(customer.getId());

        assertThat(result.kind()).isEqualTo("INVOICE");
        assertThat(again.invoiceId()).isEqualTo(result.invoiceId());
        Invoice invoice = invoiceRepository.findById(result.invoiceId()).orElseThrow();
        assertThat(invoice.getTotal()).isEqualTo(35_050L);
        assertThat(invoice.getOwnerCustomerId()).isEqualTo(customer.getId());
        assertThat(invoiceRepository.findAll().stream().filter(i -> customer.getId().equals(i.getOwnerCustomerId())).count()).isEqualTo(1);
        assertThat(subscriptionRepository.findByOwnerCustomerIdAndProductId(customer.getId(), b.getId()).orElseThrow().getPlanId())
            .isEqualTo(bPlan.getId());
        assertThat(cartService.getCart(customer.getId()).itemCount()).isZero();

        var summary = checkoutService.summaryForInvoice(customer.getId(), null, result.invoiceId());
        assertThat(summary.items()).extracting("productName").containsExactly(a.getName(), b.getName());
        assertThat(summary.items()).extracting("planName").containsExactly("Pro", "Team");
    }

    @Test
    void anEmptyCartCannotBeCheckedOut() {
        Customer customer = newCustomer();
        assertThatThrownBy(() -> cartService.checkout(customer.getId()))
            .isInstanceOf(CartConflictException.class)
            .satisfies(e -> assertThat(((CartConflictException) e).getCode()).isEqualTo("CART_EMPTY"));
    }

    @Test
    void anOrganizationMembersCheckoutSubmitsOneOrderPerItemAndNoInvoice() {
        Organization org = new Organization();
        org.setName("Cart Org");
        org.setCode("CART-" + System.nanoTime());
        org.setBusinessEmail("biz@cart-org.example");
        org.setCountry("India");
        org.setLicensedSeats(5);
        org = organizationRepository.save(org);
        Customer member = newCustomer();
        organizationMemberService.addMember(org.getId(), newCustomer().getId(), OrgRole.ORG_ADMIN);
        organizationMemberService.addMember(org.getId(), member.getId(), OrgRole.MEMBER);
        Product a = newProduct("OrgA");
        ProductPlan aPlan = newPlan(a, "Pro", BillingPeriod.MONTHLY, "100.00");
        Product b = newProduct("OrgB");
        ProductPlan bPlan = newPlan(b, "Pro", BillingPeriod.MONTHLY, "50.00");
        cartService.addItem(member.getId(), new AddCartItemRequest(a.getId(), aPlan.getId()));
        CartDto cart = cartService.addItem(member.getId(), new AddCartItemRequest(b.getId(), bPlan.getId()));
        assertThat(cart.continueAs()).isEqualTo(CartService.ORGANIZATION_MEMBER);

        CartCheckoutResultDto result = cartService.checkout(member.getId());

        assertThat(result.kind()).isEqualTo("ORDER");
        assertThat(result.orderIds()).hasSize(2);
        assertThat(result.orderIds()).allSatisfy(id ->
            assertThat(orderRepository.findById(id).orElseThrow().getStatus()).isEqualTo(OrderStatus.SUBMITTED));
        Long orgId = org.getId();
        assertThat(invoiceRepository.findAll().stream().filter(i -> orgId.equals(i.getOwnerOrganizationId()))).isEmpty();
        assertThat(cartService.getCart(member.getId()).itemCount()).isZero();
    }
}
