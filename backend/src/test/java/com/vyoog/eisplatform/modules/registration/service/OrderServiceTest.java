package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.product.model.BillingPeriod;
import com.vyoog.eisplatform.modules.product.model.Currency;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.DecideOrderRequest;
import com.vyoog.eisplatform.modules.registration.dto.OrderDto;
import com.vyoog.eisplatform.modules.registration.dto.SubmitOrderRequest;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrderStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 09 Order & Provisioning Management (sprint 2027.1.1), organization purchasing only. */
@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OrganizationMemberService organizationMemberService;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductPlanRepository productPlanRepository;
    @Autowired
    private ProductSubscriptionRepository subscriptionRepository;

    private Organization newOrganization() {
        Organization organization = new Organization();
        organization.setName("Test Org");
        organization.setCode("ORD-" + System.nanoTime());
        organization.setBusinessEmail("biz@test-order.example");
        organization.setCountry("India");
        organization.setLicensedSeats(5);
        return organizationRepository.save(organization);
    }

    private Customer newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("order-test-" + System.nanoTime() + "@test.example");
        customer.setFirstName("Test");
        customer.setLastName("User");
        return customerRepository.save(customer);
    }

    private Product newProduct() {
        Product product = new Product();
        product.setName("Order Test Product " + System.nanoTime());
        product.setPrice(BigDecimal.TEN);
        product.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(product);
    }

    private ProductPlan newPlan(Product product) {
        ProductPlan plan = new ProductPlan();
        plan.setProduct(product);
        plan.setName("Pro");
        plan.setPrice(BigDecimal.ONE);
        plan.setBillingPeriod(BillingPeriod.MONTHLY);
        plan.setCurrency(Currency.USD);
        return productPlanRepository.save(plan);
    }

    private record Setup(Organization org, Long adminId, Long memberId) {
    }

    private Setup newOrgWithAdminAndMember() {
        Organization org = newOrganization();
        Long adminId = newCustomer().getId();
        Long memberId = newCustomer().getId();
        organizationMemberService.addMember(org.getId(), adminId, OrgRole.ORG_ADMIN);
        organizationMemberService.addMember(org.getId(), memberId, OrgRole.MEMBER);
        return new Setup(org, adminId, memberId);
    }

    @Test
    void memberSubmitsOrderThenAdminApprovesAndProvisions() {
        Setup setup = newOrgWithAdminAndMember();
        Product product = newProduct();
        ProductPlan plan = newPlan(product);

        OrderDto submitted = orderService.submitOrder(setup.memberId(), new SubmitOrderRequest(product.getId(), plan.getId()));
        assertThat(submitted.status()).isEqualTo(OrderStatus.SUBMITTED);

        OrderDto approved = orderService.approveOrder(setup.adminId(), submitted.id(), new DecideOrderRequest("looks good"));
        assertThat(approved.status()).isEqualTo(OrderStatus.APPROVED);

        var subscription = subscriptionRepository.findByOwnerOrganizationIdAndProductId(setup.org().getId(), product.getId()).orElseThrow();
        assertThat(subscription.getPlanId()).isEqualTo(plan.getId());
        assertThat(subscription.getStatus().name()).isEqualTo("ACTIVE");
    }

    @Test
    void onlyAnAdminMayApprove() {
        Setup setup = newOrgWithAdminAndMember();
        Product product = newProduct();
        OrderDto submitted = orderService.submitOrder(setup.memberId(), new SubmitOrderRequest(product.getId(), null));

        assertThatThrownBy(() -> orderService.approveOrder(setup.memberId(), submitted.id(), new DecideOrderRequest(null)))
            .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void rejectingAnOrderRecordsTheDecisionNote() {
        Setup setup = newOrgWithAdminAndMember();
        Product product = newProduct();
        OrderDto submitted = orderService.submitOrder(setup.memberId(), new SubmitOrderRequest(product.getId(), null));

        OrderDto rejected = orderService.rejectOrder(setup.adminId(), submitted.id(), new DecideOrderRequest("not this quarter"));
        assertThat(rejected.status()).isEqualTo(OrderStatus.REJECTED);
        assertThat(rejected.decisionNote()).isEqualTo("not this quarter");
    }

    @Test
    void requesterCancelsOwnSubmittedOrderButNotAfterDecision() {
        Setup setup = newOrgWithAdminAndMember();
        Product product = newProduct();
        OrderDto submitted = orderService.submitOrder(setup.memberId(), new SubmitOrderRequest(product.getId(), null));

        OrderDto rejected = orderService.rejectOrder(setup.adminId(), submitted.id(), new DecideOrderRequest(null));
        assertThatThrownBy(() -> orderService.cancelOrder(setup.memberId(), rejected.id()))
            .isInstanceOf(IllegalArgumentException.class);

        Product secondProduct = newProduct();
        OrderDto stillPending = orderService.submitOrder(setup.memberId(), new SubmitOrderRequest(secondProduct.getId(), null));
        OrderDto cancelled = orderService.cancelOrder(setup.memberId(), stillPending.id());
        assertThat(cancelled.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void planMustBelongToTheOrderedProduct() {
        Setup setup = newOrgWithAdminAndMember();
        Product product = newProduct();
        Product otherProduct = newProduct();
        ProductPlan foreignPlan = newPlan(otherProduct);

        assertThatThrownBy(() -> orderService.submitOrder(setup.memberId(), new SubmitOrderRequest(product.getId(), foreignPlan.getId())))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anotherOrganizationsOrderIsInvisible() {
        Setup orgA = newOrgWithAdminAndMember();
        Setup orgB = newOrgWithAdminAndMember();
        Product product = newProduct();
        OrderDto order = orderService.submitOrder(orgA.memberId(), new SubmitOrderRequest(product.getId(), null));

        assertThatThrownBy(() -> orderService.approveOrder(orgB.adminId(), order.id(), new DecideOrderRequest(null)))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
