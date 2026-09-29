package com.vyoog.eisplatform.modules.servicestatus.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.repository.AuditLogRepository;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.dashboard.service.BusinessDashboardService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import com.vyoog.eisplatform.modules.servicestatus.dto.IncidentRequest;
import com.vyoog.eisplatform.modules.servicestatus.dto.ProductStatusDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.UpdateProductStatusRequest;
import com.vyoog.eisplatform.modules.servicestatus.model.ServiceStatusValue;
import com.vyoog.eisplatform.modules.servicestatus.repository.ProductServiceStatusRepository;
import com.vyoog.eisplatform.modules.servicestatus.repository.ServiceIncidentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** REQ-PRT-001 (C20/C26): posting status and incidents, and who sees what. */
@SpringBootTest
@ActiveProfiles("test")
class ServiceStatusServiceTest {

    @Autowired private ServiceStatusService service;
    @Autowired private BusinessDashboardService businessDashboardService;
    @Autowired private ProductServiceStatusRepository statusRepository;
    @Autowired private ServiceIncidentRepository incidentRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private OrganizationMemberRepository memberRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrganizationMemberService memberService;
    @Autowired private AuditService auditService;
    @Autowired private AuditLogRepository auditLogRepository;

    private Product product(String name) {
        Product p = new Product();
        p.setName(name + "-" + System.nanoTime());
        p.setPrice(BigDecimal.TEN);
        return productRepository.save(p);
    }

    private Customer customer(String name) {
        Customer c = new Customer();
        c.setEmail(name + "-" + System.nanoTime() + "@status.example");
        c.setFirstName("Status");
        c.setLastName("User");
        return customerRepository.save(c);
    }

    private Customer orgAdminSubscribedTo(Product product) {
        Organization org = new Organization();
        org.setName("Status Org");
        org.setCode("STS-" + System.nanoTime());
        org.setBusinessEmail("biz@status.example");
        org.setCountry("India");
        org.setLicensedSeats(5);
        org = organizationRepository.save(org);
        Customer admin = customer("orgadmin");
        memberService.addMember(org.getId(), admin.getId(), OrgRole.ORG_ADMIN);
        ProductSubscription s = new ProductSubscription();
        s.setProductId(product.getId());
        s.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        s.setOwnerOrganizationId(org.getId());
        s.setStatus(SubscriptionStatus.ACTIVE);
        s.setStartedAt(Instant.now());
        subscriptionRepository.save(s);
        return admin;
    }

    private IncidentRequest incident(Product p, Instant start, Instant end) {
        return new IncidentRequest(p.getId(), "Slow exports", "Exports take up to 10 minutes.", start, end);
    }

    private ProductStatusDto statusOf(java.util.List<ProductStatusDto> list, Product p) {
        return list.stream().filter(s -> s.productId().equals(p.getId())).findFirst().orElseThrow();
    }

    @Test
    void everySignedInCustomerSeesEveryProductsStatusButIncidentDetailsOnlyForPurchasedOnes() {
        Product bought = product("Thiran");
        Product other = product("Valam");
        Customer member = orgAdminSubscribedTo(bought);
        Customer stranger = customer("stranger");
        service.updateProductStatus(other.getId(), new UpdateProductStatusRequest(ServiceStatusValue.DEGRADED, "Slow"), "admin-sub");
        service.createIncident(incident(bought, Instant.now().minus(1, ChronoUnit.HOURS), null), "admin-sub");
        service.createIncident(incident(other, Instant.now().minus(1, ChronoUnit.HOURS), null), "admin-sub");

        var memberView = service.customerView(member.getId());
        assertThat(memberView.enabled()).isTrue();
        assertThat(statusOf(memberView.products(), bought).purchased()).isTrue();
        assertThat(statusOf(memberView.products(), other).status()).isEqualTo(ServiceStatusValue.DEGRADED);
        assertThat(statusOf(memberView.products(), other).purchased()).isFalse();
        assertThat(memberView.incidents()).extracting(i -> i.productId()).containsOnly(bought.getId());

        var strangerView = service.customerView(stranger.getId());
        assertThat(statusOf(strangerView.products(), other).status()).isEqualTo(ServiceStatusValue.DEGRADED);
        assertThat(strangerView.incidents()).isEmpty();
        assertThat(service.customerView(null).incidents()).isEmpty();
    }

    @Test
    void aProductWithNoPostedStatusIsOperationalAndInactiveProductsAreNotListed() {
        Product fresh = product("Fresh");
        Product retired = product("Retired");
        retired.setStatus(ProductStatus.INACTIVE);
        productRepository.save(retired);

        var view = service.customerView(null);
        assertThat(statusOf(view.products(), fresh).status()).isEqualTo(ServiceStatusValue.OPERATIONAL);
        assertThat(view.products()).noneMatch(s -> s.productId().equals(retired.getId()));
        assertThatThrownBy(() -> service.updateProductStatus(retired.getId(),
            new UpdateProductStatusRequest(ServiceStatusValue.MAINTENANCE, null), "admin-sub"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void incidentsAreOpenUntilEndedAndTheEndMustNotBeBeforeTheStart() {
        Product p = product("Yukth");
        Instant start = Instant.now().minus(2, ChronoUnit.HOURS);
        var created = service.createIncident(incident(p, start, null), "admin-sub");
        assertThat(created.open()).isTrue();

        var resolved = service.updateIncident(created.id(), incident(p, start, Instant.now()), "admin-sub");
        assertThat(resolved.open()).isFalse();

        assertThatThrownBy(() -> service.updateIncident(created.id(), incident(p, start, start.minus(1, ChronoUnit.MINUTES)), "admin-sub"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.updateIncident(-1L, incident(p, start, null), "admin-sub"))
            .isInstanceOf(ResourceNotFoundException.class);
        assertThat(auditLogRepository.findAll()).extracting(l -> l.getAction()).contains("INCIDENT_POSTED", "INCIDENT_UPDATED");
    }

    @Test
    void statusChangesAreAuditedAndShowOnThePurchasersBusinessDashboard() {
        Product p = product("Tharav");
        Customer admin = orgAdminSubscribedTo(p);
        service.updateProductStatus(p.getId(), new UpdateProductStatusRequest(ServiceStatusValue.MAJOR_OUTAGE, "Down"), "admin-sub");

        assertThat(auditLogRepository.findAll()).anySatisfy(l -> {
            assertThat(l.getAction()).isEqualTo("SERVICE_STATUS_CHANGED");
            assertThat(l.getTargetId()).isEqualTo(p.getId().toString());
        });
        var alerts = businessDashboardService.getBusinessDashboard(admin.getId()).alerts();
        assertThat(alerts).anySatisfy(a -> {
            assertThat(a.type()).isEqualTo("SERVICE_STATUS");
            assertThat(a.severity()).isEqualTo("error");
            assertThat(a.message()).contains(p.getName()).contains("major outage");
        });

        service.updateProductStatus(p.getId(), new UpdateProductStatusRequest(ServiceStatusValue.OPERATIONAL, null), "admin-sub");
        assertThat(businessDashboardService.getBusinessDashboard(admin.getId()).alerts())
            .noneMatch(a -> a.type().equals("SERVICE_STATUS") && a.message().contains(p.getName()));
    }

    @Test
    void theSettingTurnsTheCustomerViewOffButNotTheAdminView() {
        Product p = product("Hidden");
        ServiceStatusService off = new ServiceStatusService(statusRepository, incidentRepository, productRepository,
            subscriptionRepository, memberRepository, auditService, false);

        var view = off.customerView(null);
        assertThat(view.enabled()).isFalse();
        assertThat(view.products()).isEmpty();
        assertThat(off.adminView().enabled()).isFalse();
        assertThat(off.adminView().products()).anyMatch(s -> s.productId().equals(p.getId()));
        assertThat(service.isEnabled()).isTrue();
    }
}
