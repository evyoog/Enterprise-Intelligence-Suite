package com.vyoog.eisplatform.modules.registration.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.dto.AccessRequestDtos.AccessRequestDto;
import com.vyoog.eisplatform.modules.registration.dto.AccessRequestDtos.CreateAccessRequest;
import com.vyoog.eisplatform.modules.registration.dto.AccessRequestDtos.DecideAccessRequest;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
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

/** C103 / REQ-TEN-010: a member asking for their own access to a product the organization already subscribes to, and removing it again. */
@SpringBootTest
@ActiveProfiles("test")
class AccessRequestServiceTest {

    @Autowired private AccessRequestService accessRequestService;
    @Autowired private OrganizationMemberService memberService;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private OrganizationProductAccessRepository accessRepository;
    @Autowired private OrganizationMemberRepository memberRepository;

    private Organization newOrganization() {
        Organization org = new Organization();
        org.setName("AR Org");
        org.setCode("AR-" + System.nanoTime());
        org.setBusinessEmail("biz@ar.example");
        org.setCountry("India");
        org.setLicensedSeats(5);
        return organizationRepository.save(org);
    }

    private Customer newCustomer() {
        Customer c = new Customer();
        c.setEmail("ar-" + System.nanoTime() + "@ar.example");
        c.setFirstName("Ar");
        c.setLastName("User");
        return customerRepository.save(c);
    }

    private Product subscribedProduct(Organization org) {
        Product p = new Product();
        p.setName("AR Product " + System.nanoTime());
        p.setPrice(BigDecimal.TEN);
        p.setStatus(ProductStatus.ACTIVE);
        p = productRepository.save(p);
        ProductSubscription s = new ProductSubscription();
        s.setProductId(p.getId());
        s.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        s.setOwnerOrganizationId(org.getId());
        s.setStatus(SubscriptionStatus.ACTIVE);
        s.setStartedAt(Instant.now());
        subscriptionRepository.save(s);
        return p;
    }

    private record Setup(Organization org, Long adminId, Long memberId) {
    }

    private Setup orgWithAdminAndMember() {
        Organization org = newOrganization();
        Long adminId = newCustomer().getId();
        Long memberId = newCustomer().getId();
        memberService.addMember(org.getId(), adminId, OrgRole.ORG_ADMIN);
        memberService.addMember(org.getId(), memberId, OrgRole.MEMBER);
        return new Setup(org, adminId, memberId);
    }

    @Test
    void aMemberRequestsAccessAndAnAdminApprovesIt() {
        Setup setup = orgWithAdminAndMember();
        Product product = subscribedProduct(setup.org());

        AccessRequestDto created = accessRequestService.request(setup.memberId(), new CreateAccessRequest(product.getId(), "Need it for my work"));
        assertThat(created.status()).isEqualTo("PENDING");
        assertThat(accessRequestService.myRequests(setup.memberId())).extracting(AccessRequestDto::id).contains(created.id());
        assertThat(accessRequestService.listForOrganization(setup.adminId())).extracting(AccessRequestDto::id).contains(created.id());

        AccessRequestDto approved = accessRequestService.approve(setup.adminId(), created.id());
        assertThat(approved.status()).isEqualTo("APPROVED");

        var member = memberRepository.findByOrganizationIdAndCustomerId(setup.org().getId(), setup.memberId()).orElseThrow();
        assertThat(accessRepository.findByOrganizationMemberIdAndProductId(member.getId(), product.getId()).orElseThrow().getStatus().name()).isEqualTo("ACTIVE");
    }

    @Test
    void anAdminRejectsARequestWithAReason() {
        Setup setup = orgWithAdminAndMember();
        Product product = subscribedProduct(setup.org());
        AccessRequestDto created = accessRequestService.request(setup.memberId(), new CreateAccessRequest(product.getId(), null));

        assertThatThrownBy(() -> accessRequestService.reject(setup.adminId(), created.id(), new DecideAccessRequest(" ")))
            .isInstanceOf(IllegalArgumentException.class);

        AccessRequestDto rejected = accessRequestService.reject(setup.adminId(), created.id(), new DecideAccessRequest("Not needed for your role"));
        assertThat(rejected.status()).isEqualTo("REJECTED");
        assertThat(rejected.decisionNote()).isEqualTo("Not needed for your role");
    }

    @Test
    void onlyOnePendingRequestPerMemberAndProductAndCannotRequestWhatYouAlreadyHave() {
        Setup setup = orgWithAdminAndMember();
        Product product = subscribedProduct(setup.org());
        accessRequestService.request(setup.memberId(), new CreateAccessRequest(product.getId(), null));

        assertThatThrownBy(() -> accessRequestService.request(setup.memberId(), new CreateAccessRequest(product.getId(), null)))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("pending");

        Product unsavedProduct = new Product();
        unsavedProduct.setName("No sub " + System.nanoTime());
        unsavedProduct.setPrice(BigDecimal.ONE);
        unsavedProduct.setStatus(ProductStatus.ACTIVE);
        final Product noSubscription = productRepository.save(unsavedProduct);
        assertThatThrownBy(() -> accessRequestService.request(setup.memberId(), new CreateAccessRequest(noSubscription.getId(), null)))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("does not have an active subscription");
    }

    @Test
    void aMemberCanRemoveTheirOwnAccessImmediatelyAndMustRequestAgainToGetItBack() {
        Setup setup = orgWithAdminAndMember();
        Product product = subscribedProduct(setup.org());
        AccessRequestDto created = accessRequestService.request(setup.memberId(), new CreateAccessRequest(product.getId(), null));
        accessRequestService.approve(setup.adminId(), created.id());

        assertThatThrownBy(() -> accessRequestService.removeMyAccess(setup.adminId() /* someone without access */, product.getId()))
            .isInstanceOf(IllegalArgumentException.class);

        accessRequestService.removeMyAccess(setup.memberId(), product.getId());
        var member = memberRepository.findByOrganizationIdAndCustomerId(setup.org().getId(), setup.memberId()).orElseThrow();
        assertThat(accessRepository.findByOrganizationMemberIdAndProductId(member.getId(), product.getId()).orElseThrow().getStatus().name()).isEqualTo("INACTIVE");

        // Getting it back needs a new request — it is not still "approved" from before.
        AccessRequestDto again = accessRequestService.request(setup.memberId(), new CreateAccessRequest(product.getId(), null));
        assertThat(again.status()).isEqualTo("PENDING");
    }

    @Test
    void onlyManageProductAccessOrOrgAdminCanSeeOrDecideRequestsOfAnotherOrganization() {
        Setup setup = orgWithAdminAndMember();
        Product product = subscribedProduct(setup.org());
        AccessRequestDto created = accessRequestService.request(setup.memberId(), new CreateAccessRequest(product.getId(), null));

        Setup other = orgWithAdminAndMember();
        assertThatThrownBy(() -> accessRequestService.approve(other.adminId(), created.id())).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> accessRequestService.approve(setup.memberId(), created.id())).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> accessRequestService.approve(setup.adminId(), 999999L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void aDecidedRequestCannotBeDecidedAgain() {
        Setup setup = orgWithAdminAndMember();
        Product product = subscribedProduct(setup.org());
        AccessRequestDto created = accessRequestService.request(setup.memberId(), new CreateAccessRequest(product.getId(), null));
        accessRequestService.approve(setup.adminId(), created.id());

        assertThatThrownBy(() -> accessRequestService.approve(setup.adminId(), created.id())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> accessRequestService.reject(setup.adminId(), created.id(), new DecideAccessRequest("x"))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aMemberCanCancelTheirOwnPendingRequest() {
        Setup setup = orgWithAdminAndMember();
        Product product = subscribedProduct(setup.org());
        AccessRequestDto created = accessRequestService.request(setup.memberId(), new CreateAccessRequest(product.getId(), null));
        AccessRequestDto cancelled = accessRequestService.cancel(setup.memberId(), created.id());
        assertThat(cancelled.status()).isEqualTo("CANCELLED");
        assertThatThrownBy(() -> accessRequestService.cancel(setup.memberId(), created.id())).isInstanceOf(IllegalArgumentException.class);
    }
}
