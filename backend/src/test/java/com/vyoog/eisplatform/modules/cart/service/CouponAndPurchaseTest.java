package com.vyoog.eisplatform.modules.cart.service;

import com.vyoog.eisplatform.common.exception.BillingConflictException;
import com.vyoog.eisplatform.modules.access.service.AccessManagementService;
import com.vyoog.eisplatform.modules.billing.dto.CouponDtos;
import com.vyoog.eisplatform.modules.billing.model.Invoice;
import com.vyoog.eisplatform.modules.billing.model.InvoiceLine;
import com.vyoog.eisplatform.modules.billing.model.InvoiceStatus;
import com.vyoog.eisplatform.modules.billing.repository.CouponRepository;
import com.vyoog.eisplatform.modules.billing.repository.InvoiceRepository;
import com.vyoog.eisplatform.modules.billing.service.CheckoutService;
import com.vyoog.eisplatform.modules.billing.service.CouponService;
import com.vyoog.eisplatform.modules.billing.service.InvoiceService;
import com.vyoog.eisplatform.modules.billing.service.razorpay.RazorpayClient;
import com.vyoog.eisplatform.modules.billing.tax.service.TaxTestSupport;
import com.vyoog.eisplatform.modules.cart.dto.AddCartItemRequest;
import com.vyoog.eisplatform.modules.cart.dto.CartCheckoutResultDto;
import com.vyoog.eisplatform.modules.cart.dto.CartDto;
import com.vyoog.eisplatform.modules.notification.repository.NotificationRepository;
import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.DecideOrderRequest;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.Order;
import com.vyoog.eisplatform.modules.registration.model.OrderStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrderRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.service.OrderService;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * C100: coupons (PB-1 note) on the cart and the invoice, and who buys what: CA-1 B (several products, one invoice or one multi-item order), CA-2 (a
 * member's purchase request with a reason; an organization admin or a person with Manage billing buys directly, and the admins are told).
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(TaxTestSupport.class)
class CouponAndPurchaseTest {

    @Autowired private CartService cartService;
    @Autowired private CheckoutService checkoutService;
    @Autowired private CouponService couponService;
    @Autowired private CouponRepository couponRepository;
    @Autowired private InvoiceService invoiceService;
    @Autowired private InvoiceRepository invoiceRepository;
    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductPlanRepository planRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberRepository memberRepository;
    @Autowired private OrganizationMemberService organizationMemberService;
    @Autowired private AccessManagementService accessService;
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private TaxTestSupport tax;
    @Autowired private PlatformTransactionManager transactionManager;
    @MockBean private RazorpayClient razorpayClient;

    private Customer newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("c100cart-" + System.nanoTime() + "@example.com");
        customer.setFirstName("Cart");
        customer.setLastName("Buyer");
        return customerRepository.save(customer);
    }

    private ProductPlan plan(String name, String price) {
        Product product = new Product();
        product.setName(name + " " + System.nanoTime());
        product.setPrice(BigDecimal.ZERO);
        product.setStatus(ProductStatus.ACTIVE);
        product = productRepository.save(product);
        ProductPlan plan = new ProductPlan();
        plan.setProduct(product);
        plan.setName("Pro");
        plan.setPrice(new BigDecimal(price));
        plan.setBillingPeriod(BillingPeriod.MONTHLY);
        plan.setCurrency(Currency.INR);
        return planRepository.save(plan);
    }

    private String code(String prefix) {
        return prefix + System.nanoTime();
    }

    private CouponDtos.Coupon percent(String code, int percent) {
        return couponService.create("admin-sub", null, new CouponDtos.Save(code, "Test", "PERCENT", new BigDecimal(percent), null, null, Instant.now().plus(30, ChronoUnit.DAYS)));
    }

    private List<InvoiceLine> linesOf(Long invoiceId) {
        return new TransactionTemplate(transactionManager).execute(s -> List.copyOf(invoiceRepository.findById(invoiceId).orElseThrow().getLines()));
    }

    private Organization newOrganization() {
        Organization org = new Organization();
        org.setName("C100 Cart Org");
        org.setCode("C100CART-" + System.nanoTime());
        org.setBusinessEmail("biz@c100cart.example");
        org.setCountry("India");
        org.setLicensedSeats(5);
        return organizationRepository.save(org);
    }

    // ------------------------------------------------------------------ coupons

    @Test
    void theAdministratorCreatesDisablesAndDeletesCouponsAndTheRulesAreEnforced() {
        String code = code("save");
        var created = percent(code.toLowerCase(), 20);
        assertThat(created.code()).as("stored in capitals").isEqualTo(code.toUpperCase());
        assertThat(created.status()).isEqualTo("ACTIVE");

        assertThatThrownBy(() -> percent(code, 10)).isInstanceOf(BillingConflictException.class);
        assertThatThrownBy(() -> couponService.create("a", null, new CouponDtos.Save(code("bad"), null, "PERCENT", new BigDecimal(100), null, null, Instant.now().plus(1, ChronoUnit.DAYS))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("between 1% and 99%");
        assertThatThrownBy(() -> couponService.create("a", null, new CouponDtos.Save(code("past"), null, "PERCENT", new BigDecimal(10), null, null, Instant.now().minus(1, ChronoUnit.DAYS))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("future");
        assertThatThrownBy(() -> couponService.create("a", null, new CouponDtos.Save("x", null, "PERCENT", new BigDecimal(10), null, null, Instant.now().plus(1, ChronoUnit.DAYS))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("3 to 40");
        assertThatThrownBy(() -> couponService.create("a", null, new CouponDtos.Save(code("fix"), null, "FIXED", null, 500L, null, Instant.now().plus(1, ChronoUnit.DAYS))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("currency");

        assertThat(couponService.quote(code, "INR", 10_000).amount()).isEqualTo(2_000);
        assertThat(couponService.setEnabled("a", null, created.id(), false).status()).isEqualTo("DISABLED");
        assertThatThrownBy(() -> couponService.quote(code, "INR", 10_000)).hasMessageContaining("not active");
        couponService.setEnabled("a", null, created.id(), true);
        assertThatThrownBy(() -> couponService.quote("nope-" + code, "INR", 10_000)).hasMessageContaining("not valid");

        var expired = couponService.update("a", null, created.id(), new CouponDtos.Save(code, "Test", "PERCENT", new BigDecimal(20), null, null, Instant.now().minus(1, ChronoUnit.MINUTES)));
        assertThat(expired.status()).isEqualTo("EXPIRED");
        assertThatThrownBy(() -> couponService.quote(code, "INR", 10_000)).hasMessageContaining("expired");

        var fixed = couponService.create("a", null, new CouponDtos.Save(code("fixed"), null, "FIXED", null, 5_000L, "usd", Instant.now().plus(1, ChronoUnit.DAYS)));
        assertThatThrownBy(() -> couponService.quote(fixed.code(), "INR", 100_000)).hasMessageContaining("USD purchases only");
        assertThat(couponService.quote(fixed.code(), "USD", 100_000).amount()).isEqualTo(5_000);
        assertThatThrownBy(() -> couponService.quote(fixed.code(), "USD", 5_000)).hasMessageContaining("worth more than your cart");

        couponService.delete("a", null, fixed.id());
        assertThat(couponRepository.findByCode(fixed.code())).isEmpty();
    }

    @Test
    void aCouponTakesTheDiscountOffBeforeTaxAndIsCountedOnceTheInvoiceExists() {
        Customer customer = newCustomer();
        tax.readyToInvoiceInIndia(customer);
        var coupon = percent(code("TEN"), 10);
        ProductPlan a = plan("CA", "100.00");
        ProductPlan b = plan("CB", "250.50");
        cartService.addItem(customer.getId(), new AddCartItemRequest(a.getProduct().getId(), a.getId()));
        cartService.addItem(customer.getId(), new AddCartItemRequest(b.getProduct().getId(), b.getId()));

        CartDto applied = cartService.applyCoupon(customer.getId(), coupon.code().toLowerCase());
        assertThat(applied.couponCode()).isEqualTo(coupon.code());
        assertThat(applied.subtotal()).isEqualTo(35_050);
        assertThat(applied.discount()).isEqualTo(3_505);
        assertThat(applied.total()).isEqualTo(31_545);

        CartCheckoutResultDto result = cartService.checkout(customer.getId());

        Invoice invoice = invoiceRepository.findById(result.invoiceId()).orElseThrow();
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OPEN);
        assertThat(invoice.getDiscountAmount()).isEqualTo(3_505);
        assertThat(invoice.getCouponCode()).isEqualTo(coupon.code());
        assertThat(invoice.getSubtotal()).as("net of the discount").isEqualTo(31_545);
        assertThat(invoice.getTotal()).isEqualTo(invoice.getSubtotal() + invoice.getTaxAmount());
        assertThat(linesOf(invoice.getId()).stream().mapToLong(InvoiceLine::getDiscountAmount).sum()).as("the lines share the discount").isEqualTo(3_505);
        assertThat(invoiceService.taxLinesOf(invoice.getId()).stream().mapToLong(t -> t.taxableAmount()).sum())
            .as("tax is worked out on the discounted amounts").isEqualTo(31_545);
        assertThat(couponRepository.findByCode(coupon.code()).orElseThrow().getRedeemedCount()).isEqualTo(1);
        assertThat(cartService.getCart(customer.getId()).couponCode()).as("the cart is empty and the coupon is gone").isNull();

        var summary = checkoutService.summaryForInvoice(customer.getId(), null, result.invoiceId());
        assertThat(summary.subtotal()).as("before the discount").isEqualTo(35_050);
        assertThat(summary.discount()).isEqualTo(3_505);
        assertThat(summary.couponCode()).isEqualTo(coupon.code());
        assertThat(summary.total()).isEqualTo(invoice.getTotal());
        assertThatThrownBy(() -> couponService.delete("a", null, coupon.id())).isInstanceOf(BillingConflictException.class);
    }

    @Test
    void aCouponThatStopsWorkingBeforeCheckoutIsShownInTheCartAndBlocksTheCheckoutWithNothingCreated() {
        Customer customer = newCustomer();
        tax.readyToInvoiceInIndia(customer);
        var coupon = percent(code("GONE"), 25);
        ProductPlan a = plan("CG", "200.00");
        cartService.addItem(customer.getId(), new AddCartItemRequest(a.getProduct().getId(), a.getId()));
        cartService.applyCoupon(customer.getId(), coupon.code());
        couponService.setEnabled("a", null, coupon.id(), false);

        CartDto cart = cartService.getCart(customer.getId());
        assertThat(cart.couponCode()).isEqualTo(coupon.code());
        assertThat(cart.discount()).isZero();
        assertThat(cart.couponMessage()).contains("not active");
        assertThat(cart.total()).isEqualTo(20_000);
        assertThatThrownBy(() -> cartService.checkout(customer.getId())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not active");
        assertThat(cartService.getCart(customer.getId()).itemCount()).as("the cart is untouched").isEqualTo(1);
        assertThat(couponRepository.findByCode(coupon.code()).orElseThrow().getRedeemedCount()).isZero();

        assertThat(cartService.removeCoupon(customer.getId()).couponCode()).isNull();
        assertThat(cartService.checkout(customer.getId()).kind()).isEqualTo("INVOICE");
    }

    // ------------------------------------------------------------------ CA-1 B, CA-2

    @Test
    void aMembersCartBecomesOneMultiItemRequestWithAReasonAndAdminsApproveItIntoOneInvoice() {
        Organization org = newOrganization();
        Customer admin = newCustomer();
        Customer member = newCustomer();
        organizationMemberService.addMember(org.getId(), admin.getId(), OrgRole.ORG_ADMIN);
        organizationMemberService.addMember(org.getId(), member.getId(), OrgRole.MEMBER);
        ProductPlan a = plan("RqA", "100.00");
        ProductPlan b = plan("RqB", "50.00");
        cartService.addItem(member.getId(), new AddCartItemRequest(a.getProduct().getId(), a.getId()));
        CartDto cart = cartService.addItem(member.getId(), new AddCartItemRequest(b.getProduct().getId(), b.getId()));
        assertThat(cart.continueAs()).isEqualTo(CartService.ORGANIZATION_MEMBER);
        var coupon = percent(code("MEM"), 10);
        assertThatThrownBy(() -> cartService.applyCoupon(member.getId(), coupon.code())).hasMessageContaining("purchase request");

        assertThatThrownBy(() -> cartService.checkout(member.getId(), "  ")).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("why you need this");
        assertThat(cartService.getCart(member.getId()).itemCount()).isEqualTo(2);

        CartCheckoutResultDto result = cartService.checkout(member.getId(), "The design team needs both for the Q4 launch.");

        assertThat(result.kind()).isEqualTo("ORDER");
        assertThat(result.orderIds()).hasSize(1);
        Long orderId = result.orderIds().get(0);
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SUBMITTED);
        assertThat(order.getRequestReason()).isEqualTo("The design team needs both for the Q4 launch.");
        var dto = orderService.listMyOrders(member.getId()).stream().filter(o -> o.id().equals(orderId)).findFirst().orElseThrow();
        assertThat(dto.items()).extracting(i -> i.productId()).containsExactly(a.getProduct().getId(), b.getProduct().getId());
        assertThat(dto.requestReason()).contains("Q4 launch");
        assertThat(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(admin.getId()))
            .anyMatch(n -> n.getTitle().contains("purchase request") && n.getMessage().contains("Q4 launch"));
        assertThat(invoiceRepository.findAll().stream().filter(i -> org.getId().equals(i.getOwnerOrganizationId()))).as("nothing is billed before approval").isEmpty();

        orderService.approveOrder(admin.getId(), orderId, new DecideOrderRequest(null));

        List<Invoice> invoices = invoiceRepository.findAll().stream().filter(i -> org.getId().equals(i.getOwnerOrganizationId())).toList();
        assertThat(invoices).as("one invoice for the whole order").hasSize(1);
        assertThat(linesOf(invoices.get(0).getId())).hasSize(2);
        assertThat(orderRepository.findById(orderId).orElseThrow().getStatus()).isEqualTo(OrderStatus.APPROVED);
    }

    @Test
    void anAdminOrAManageBillingHolderBuysDirectlyAndTheAdminsAreToldWhenItIsNotAnAdmin() {
        Organization org = newOrganization();
        Customer admin = newCustomer();
        Customer otherAdmin = newCustomer();
        Customer billing = newCustomer();
        organizationMemberService.addMember(org.getId(), admin.getId(), OrgRole.ORG_ADMIN);
        organizationMemberService.addMember(org.getId(), otherAdmin.getId(), OrgRole.ORG_ADMIN);
        organizationMemberService.addMember(org.getId(), billing.getId(), OrgRole.MEMBER);
        OrganizationMember row = memberRepository.findByOrganizationIdAndCustomerId(org.getId(), billing.getId()).orElseThrow();
        accessService.setOverrides(admin.getId(), row.getId(), new com.vyoog.eisplatform.modules.access.dto.AccessDtos.SetOverridesRequest(Map.of("MANAGE_ORGANIZATION_BILLING", true), null, null));

        // The admin buys: no request, one invoice, nobody else is notified of a purchase.
        ProductPlan a = plan("DirA", "100.00");
        CartDto adminCart = cartService.addItem(admin.getId(), new AddCartItemRequest(a.getProduct().getId(), a.getId()));
        assertThat(adminCart.continueAs()).isEqualTo(CartService.ORGANIZATION_BUYER);
        CartCheckoutResultDto adminResult = cartService.checkout(admin.getId());
        assertThat(adminResult.kind()).isEqualTo("INVOICE");
        assertThat(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(otherAdmin.getId())).noneMatch(n -> n.getTitle().contains("A purchase was made"));

        // A person with Manage billing buys: the same, and the admins are told.
        ProductPlan b = plan("DirB", "40.00");
        ProductPlan c = plan("DirC", "60.00");
        cartService.addItem(billing.getId(), new AddCartItemRequest(b.getProduct().getId(), b.getId()));
        CartDto billingCart = cartService.addItem(billing.getId(), new AddCartItemRequest(c.getProduct().getId(), c.getId()));
        assertThat(billingCart.continueAs()).isEqualTo(CartService.ORGANIZATION_BUYER);
        var coupon = percent(code("ORG"), 50);
        CartDto couponCart = cartService.applyCoupon(billing.getId(), coupon.code());
        assertThat(couponCart.items()).as("the plan price is per seat: the organization starts with 5 seats").allSatisfy(i -> assertThat(i.seats()).isEqualTo(5));
        assertThat(couponCart.subtotal()).isEqualTo((4_000 + 6_000) * 5);
        assertThat(couponCart.total()).isEqualTo((4_000 + 6_000) * 5 / 2);

        CartCheckoutResultDto result = cartService.checkout(billing.getId());

        assertThat(result.kind()).isEqualTo("INVOICE");
        Invoice invoice = invoiceRepository.findById(result.invoiceId()).orElseThrow();
        assertThat(invoice.getOwnerOrganizationId()).isEqualTo(org.getId());
        assertThat(invoice.getSubtotal()).as("what the cart showed").isEqualTo(couponCart.total());
        assertThat(invoice.getCouponCode()).isEqualTo(coupon.code());
        assertThat(linesOf(invoice.getId())).hasSize(2);
        assertThat(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(admin.getId()))
            .anyMatch(n -> n.getTitle().contains("A purchase was made") && n.getMessage().contains(b.getProduct().getName()));
        assertThat(notificationRepository.findByCustomerIdOrderByCreatedAtDesc(otherAdmin.getId())).anyMatch(n -> n.getTitle().contains("A purchase was made"));
        assertThat(orderRepository.findByRequestedByCustomerIdOrderByCreatedAtDesc(billing.getId())).singleElement()
            .satisfies(o -> assertThat(o.getStatus()).isEqualTo(OrderStatus.APPROVED));
    }
}
