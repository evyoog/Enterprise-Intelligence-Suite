package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.common.exception.SeatLimitExceededException;
import com.vyoog.eisplatform.modules.audit.repository.AuditLogRepository;
import com.vyoog.eisplatform.modules.integration.repository.OutboxEventRepository;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-SUB-003 (C63): seats of organization subscriptions. TC-SUB-013..017. */
@SpringBootTest
@ActiveProfiles("test")
class SubscriptionSeatServiceTest {

    @Autowired private SubscriptionSeatService seatService;
    @Autowired private SubscriptionService subscriptionService;
    @Autowired private OrganizationMemberService memberService;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private OutboxEventRepository outboxEventRepository;
    @Autowired private AuditLogRepository auditLogRepository;

    private Organization newOrganization(int licensedSeats) {
        Organization organization = new Organization();
        organization.setName("Seat Org");
        organization.setCode("SEAT-" + System.nanoTime());
        organization.setBusinessEmail("seats@test-org.example");
        organization.setCountry("India");
        organization.setLicensedSeats(licensedSeats);
        return organizationRepository.save(organization);
    }

    private Long newMember(Organization org, OrgRole role) {
        Customer customer = new Customer();
        customer.setEmail("seat-" + System.nanoTime() + "@test.example");
        customer.setFirstName("Seat");
        customer.setLastName("User");
        customer = customerRepository.save(customer);
        memberService.addMember(org.getId(), customer.getId(), role);
        return customer.getId();
    }

    private Product newProduct() {
        Product product = new Product();
        product.setName("Seat product " + System.nanoTime());
        product.setPrice(BigDecimal.ZERO);
        product.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(product);
    }

    @Test
    void aNewOrganizationSubscriptionStartsAtTheLicensedSeats() {
        Organization org = newOrganization(5);
        Long subscriptionId = subscriptionService.subscribeOrganization(org.getId(), newProduct().getId(), null).id();
        assertThat(subscriptionRepository.findById(subscriptionId).orElseThrow().getQuantity()).isEqualTo(5);
    }

    @Test
    void anAdminSeesSeatsAndCanIncreaseThemAtOnce() {
        Organization org = newOrganization(3);
        Long adminId = newMember(org, OrgRole.ORG_ADMIN);
        newMember(org, OrgRole.MEMBER);
        Long subscriptionId = subscriptionService.subscribeOrganization(org.getId(), newProduct().getId(), null).id();

        var summary = seatService.summary(adminId, subscriptionId);
        assertThat(summary.quantity()).isEqualTo(3);
        assertThat(summary.inUse()).isEqualTo(2);
        assertThat(summary.minimum()).isEqualTo(2);

        var changed = seatService.changeSeats(adminId, subscriptionId, 8);
        assertThat(changed.quantity()).isEqualTo(8);
        // BR-5: the organization's seat limit follows an increase.
        assertThat(organizationRepository.findById(org.getId()).orElseThrow().getLicensedSeats()).isEqualTo(8);

        var events = outboxEventRepository.findByAggregateTypeAndAggregateIdOrderByIdAsc("Subscription", subscriptionId.toString());
        assertThat(events).extracting(e -> e.getEventType()).contains("SeatsChanged");
        assertThat(events.get(events.size() - 1).getPayload()).contains("\"fromQuantity\":3", "\"toQuantity\":8");
        assertThat(auditLogRepository.findAll()).anyMatch(a -> "SUBSCRIPTION_SEATS_CHANGED".equals(a.getAction())
            && subscriptionId.toString().equals(a.getTargetId()));
    }

    @Test
    void seatsCannotGoBelowTheSeatsInUse() {
        Organization org = newOrganization(5);
        Long adminId = newMember(org, OrgRole.ORG_ADMIN);
        newMember(org, OrgRole.MEMBER);
        newMember(org, OrgRole.MEMBER);
        Long subscriptionId = subscriptionService.subscribeOrganization(org.getId(), newProduct().getId(), null).id();

        assertThatThrownBy(() -> seatService.changeSeats(adminId, subscriptionId, 2))
            .isInstanceOf(InvalidStateException.class)
            .hasMessageContaining("3 seats are in use");
        var reduced = seatService.changeSeats(adminId, subscriptionId, 3);
        assertThat(reduced.quantity()).isEqualTo(3);
        // a decrease never lowers the organization's seat limit
        assertThat(organizationRepository.findById(org.getId()).orElseThrow().getLicensedSeats()).isEqualTo(5);
    }

    @Test
    void addingAMemberBeyondASubscriptionsSeatsIsRefused() {
        Organization org = newOrganization(10);
        Long adminId = newMember(org, OrgRole.ORG_ADMIN);
        newMember(org, OrgRole.MEMBER);
        Long subscriptionId = subscriptionService.subscribeOrganization(org.getId(), newProduct().getId(), null).id();
        seatService.changeSeats(adminId, subscriptionId, 2);

        assertThatThrownBy(() -> newMember(org, OrgRole.MEMBER))
            .isInstanceOf(SeatLimitExceededException.class)
            .hasMessageContaining("seats of a subscription are in use");

        seatService.changeSeats(adminId, subscriptionId, 3);
        newMember(org, OrgRole.MEMBER);
    }

    @Test
    void onlyOrganizationManagersOfTheSameOrganizationCanChangeSeats() {
        Organization org = newOrganization(5);
        Long adminId = newMember(org, OrgRole.ORG_ADMIN);
        Long memberId = newMember(org, OrgRole.MEMBER);
        Long subscriptionId = subscriptionService.subscribeOrganization(org.getId(), newProduct().getId(), null).id();

        assertThatThrownBy(() -> seatService.changeSeats(memberId, subscriptionId, 6)).isNotInstanceOf(InvalidStateException.class);

        Organization other = newOrganization(5);
        Long otherAdmin = newMember(other, OrgRole.ORG_ADMIN);
        assertThatThrownBy(() -> seatService.summary(otherAdmin, subscriptionId)).isInstanceOf(ResourceNotFoundException.class);

        ProductSubscription cancelled = subscriptionRepository.findById(subscriptionId).orElseThrow();
        cancelled.setStatus(SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(cancelled);
        assertThatThrownBy(() -> seatService.changeSeats(adminId, subscriptionId, 6)).isInstanceOf(InvalidStateException.class);
    }

    @Test
    void anIndividualSubscriptionIsNotAnOrganizationSeatSubscription() {
        Organization org = newOrganization(5);
        Long adminId = newMember(org, OrgRole.ORG_ADMIN);
        ProductSubscription individual = new ProductSubscription();
        individual.setProductId(newProduct().getId());
        individual.setOwnerType(RegistrationOwnerType.INDIVIDUAL);
        individual.setOwnerCustomerId(adminId);
        individual.setStatus(SubscriptionStatus.ACTIVE);
        individual.setStartedAt(Instant.now());
        individual = subscriptionRepository.save(individual);
        assertThat(individual.getQuantity()).isEqualTo(1);
        Long id = individual.getId();
        assertThatThrownBy(() -> seatService.changeSeats(adminId, id, 4)).isInstanceOf(ResourceNotFoundException.class);
    }
}
