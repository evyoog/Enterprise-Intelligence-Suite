package com.vyoog.eisplatform.modules.serviceinstance.service;

import com.vyoog.eisplatform.common.exception.InvalidCredentialsException;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.*;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import com.vyoog.eisplatform.modules.serviceinstance.dto.ServiceInstanceDtos;
import com.vyoog.eisplatform.modules.serviceinstance.model.ServiceInstance;
import com.vyoog.eisplatform.modules.serviceinstance.repository.ServiceInstanceRepository;
import com.vyoog.eisplatform.modules.servicestatus.dto.ProductStatusDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.UpdateProductStatusRequest;
import com.vyoog.eisplatform.modules.servicestatus.model.ServiceStatusValue;
import com.vyoog.eisplatform.modules.servicestatus.service.ServiceStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** C101 / REQ-SRM-001 (SR-1 to SR-5): instances follow subscriptions, products report health, the status page is derived, admins can override. */
@SpringBootTest
@ActiveProfiles("test")
class ServiceInstanceServiceTest {

    @Autowired private ServiceInstanceService service;
    @Autowired private ServiceInstanceRepository instanceRepository;
    @Autowired private ServiceStatusService statusService;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductSubscriptionRepository subscriptionRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberService memberService;

    private Product product() {
        Product p = new Product();
        p.setName("Instance-" + System.nanoTime());
        p.setPrice(BigDecimal.TEN);
        return productRepository.save(p);
    }

    private Organization organization() {
        Organization org = new Organization();
        org.setName("Inst Org");
        org.setCode("INS-" + System.nanoTime());
        org.setBusinessEmail("biz@instance.example");
        org.setCountry("India");
        org.setLicensedSeats(5);
        return organizationRepository.save(org);
    }

    private Customer customer() {
        Customer c = new Customer();
        c.setEmail("inst-" + System.nanoTime() + "@instance.example");
        c.setFirstName("Inst");
        c.setLastName("User");
        return customerRepository.save(c);
    }

    private ProductSubscription subscribe(Product product, Organization org, SubscriptionStatus status) {
        ProductSubscription s = new ProductSubscription();
        s.setProductId(product.getId());
        s.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        s.setOwnerOrganizationId(org.getId());
        s.setStatus(status);
        s.setStartedAt(Instant.now());
        return subscriptionRepository.save(s);
    }

    private ServiceInstance instanceFor(Product product, Organization org) {
        ProductSubscription subscription = subscribe(product, org, SubscriptionStatus.ACTIVE);
        service.syncFromSubscription(subscription);
        return instanceRepository.findBySubscriptionId(subscription.getId()).orElseThrow();
    }

    private String key(Product product) {
        return service.createKey("admin-sub", new ServiceInstanceDtos.CreateReporterKey(product.getId(), "test")).secret();
    }

    private ServiceInstanceDtos.HealthReport report(ServiceInstance i, String health) {
        return new ServiceInstanceDtos.HealthReport(i.getId(), null, health, null, null, null, null);
    }

    @Test
    void anActiveSubscriptionGetsAnInstanceAndAPendingOneDoesNot() {
        Product p = product();
        Organization org = organization();
        ProductSubscription pending = subscribe(p, org, SubscriptionStatus.PENDING_SUBSCRIPTION);
        service.syncFromSubscription(pending);
        assertThat(instanceRepository.findBySubscriptionId(pending.getId())).isEmpty();

        pending.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepository.save(pending);
        service.syncFromSubscription(pending);
        ServiceInstance i = instanceRepository.findBySubscriptionId(pending.getId()).orElseThrow();
        assertThat(i.getStatus().name()).isEqualTo("PROVISIONING");
        assertThat(i.getHealth().name()).isEqualTo("UNKNOWN");
    }

    @Test
    void theInstanceFollowsTheSubscriptionSuspendResumeAndCancel() {
        Product p = product();
        Organization org = organization();
        ProductSubscription s = subscribe(p, org, SubscriptionStatus.ACTIVE);
        service.syncFromSubscription(s);
        s.setStatus(SubscriptionStatus.SUSPENDED);
        service.syncFromSubscription(subscriptionRepository.save(s));
        assertThat(instanceRepository.findBySubscriptionId(s.getId()).orElseThrow().getStatus().name()).isEqualTo("SUSPENDED");
        s.setStatus(SubscriptionStatus.ACTIVE);
        service.syncFromSubscription(subscriptionRepository.save(s));
        assertThat(instanceRepository.findBySubscriptionId(s.getId()).orElseThrow().getStatus().name()).isEqualTo("PROVISIONING");
        s.setStatus(SubscriptionStatus.CANCELLED);
        service.syncFromSubscription(subscriptionRepository.save(s));
        assertThat(instanceRepository.findBySubscriptionId(s.getId()).orElseThrow().getStatus().name()).isEqualTo("DEPROVISIONED");
    }

    @Test
    void aProductReportsHealthWithItsKeyAndTheFirstReportActivatesTheInstance() {
        Product p = product();
        ServiceInstance i = instanceFor(p, organization());
        String secret = key(p);
        var result = service.report(secret, List.of(new ServiceInstanceDtos.HealthReport(i.getId(), null, "degraded", "Slow queue", null, "tenant-1", "https://x.example")));
        assertThat(result.accepted()).isEqualTo(1);
        ServiceInstance after = instanceRepository.findById(i.getId()).orElseThrow();
        assertThat(after.getStatus().name()).isEqualTo("ACTIVE");
        assertThat(after.getHealth().name()).isEqualTo("DEGRADED");
        assertThat(after.getTenantReference()).isEqualTo("tenant-1");
        assertThat(after.getLastChangedBy()).isEqualTo("PRODUCT");
    }

    @Test
    void aMissingRevokedOrOtherProductsKeyIsRejected() {
        Product p = product();
        Product other = product();
        ServiceInstance i = instanceFor(p, organization());
        assertThatThrownBy(() -> service.report(null, List.of(report(i, "HEALTHY")))).isInstanceOf(InvalidCredentialsException.class);
        assertThatThrownBy(() -> service.report("eisrk_nope", List.of(report(i, "HEALTHY")))).isInstanceOf(InvalidCredentialsException.class);

        var created = service.createKey("admin-sub", new ServiceInstanceDtos.CreateReporterKey(other.getId(), null));
        var wrongProduct = service.report(created.secret(), List.of(report(i, "HEALTHY")));
        assertThat(wrongProduct.accepted()).isZero();
        assertThat(wrongProduct.errors()).hasSize(1);

        var mine = service.createKey("admin-sub", new ServiceInstanceDtos.CreateReporterKey(p.getId(), null));
        service.revokeKey("admin-sub", mine.key().id());
        assertThatThrownBy(() -> service.report(mine.secret(), List.of(report(i, "HEALTHY")))).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void theKeyIsOnlyShownOnceAndStoredHashed() {
        Product p = product();
        var created = service.createKey("admin-sub", new ServiceInstanceDtos.CreateReporterKey(p.getId(), "ci"));
        assertThat(created.secret()).startsWith("eisrk_");
        assertThat(created.key().keyPrefix()).isEqualTo(created.secret().substring(0, 12));
        assertThat(service.listKeys()).anyMatch(k -> k.id().equals(created.key().id()) && k.keyPrefix().length() == 12);
    }

    @Test
    void anInvalidHealthValueIsRejectedPerReport() {
        Product p = product();
        ServiceInstance i = instanceFor(p, organization());
        var result = service.report(key(p), List.of(report(i, "HEALTHY"), report(i, "BROKEN")));
        assertThat(result.accepted()).isEqualTo(1);
        assertThat(result.rejected()).isEqualTo(1);
        assertThat(result.errors().get(0)).contains("Report 2");
    }

    @Test
    void moreThanHalfDownIsAMajorOutageSomeDownIsPartialDegradedIsDegradedAndAllHealthyIsOperational() {
        Product p = product();
        String secret = key(p);
        ServiceInstance a = instanceFor(p, organization());
        ServiceInstance b = instanceFor(p, organization());
        ServiceInstance c = instanceFor(p, organization());
        ServiceInstance d = instanceFor(p, organization());

        service.report(secret, List.of(report(a, "HEALTHY"), report(b, "HEALTHY"), report(c, "HEALTHY"), report(d, "HEALTHY")));
        assertThat(service.derivedStatus(p.getId()).orElseThrow().status()).isEqualTo(ServiceStatusValue.OPERATIONAL);

        service.report(secret, List.of(report(a, "DEGRADED")));
        assertThat(service.derivedStatus(p.getId()).orElseThrow().status()).isEqualTo(ServiceStatusValue.DEGRADED);

        service.report(secret, List.of(report(b, "DOWN")));
        assertThat(service.derivedStatus(p.getId()).orElseThrow().status()).isEqualTo(ServiceStatusValue.PARTIAL_OUTAGE);

        service.report(secret, List.of(report(c, "DOWN")));   // exactly half: not more than half
        assertThat(service.derivedStatus(p.getId()).orElseThrow().status()).isEqualTo(ServiceStatusValue.PARTIAL_OUTAGE);

        service.report(secret, List.of(report(d, "DOWN")));
        var derived = service.derivedStatus(p.getId()).orElseThrow();
        assertThat(derived.status()).isEqualTo(ServiceStatusValue.MAJOR_OUTAGE);
        assertThat(derived.down()).isEqualTo(3);
        assertThat(derived.reporting()).isEqualTo(4);
    }

    @Test
    void aStaleReportIsIgnoredAndTheManualStatusIsKept() {
        Product p = product();
        ServiceInstance i = instanceFor(p, organization());
        service.report(key(p), List.of(report(i, "DOWN")));
        ServiceInstance stored = instanceRepository.findById(i.getId()).orElseThrow();
        stored.setLastReportedAt(Instant.now().minus(10, ChronoUnit.MINUTES));
        instanceRepository.save(stored);

        assertThat(service.derivedStatus(p.getId())).isEmpty();
        statusService.updateProductStatus(p.getId(), new UpdateProductStatusRequest(ServiceStatusValue.DEGRADED, "Manual"), "admin-sub");
        ProductStatusDto dto = statusService.adminView().products().stream().filter(s -> s.productId().equals(p.getId())).findFirst().orElseThrow();
        assertThat(dto.status()).isEqualTo(ServiceStatusValue.DEGRADED);
        assertThat(dto.source()).isEqualTo("MANUAL");
    }

    @Test
    void theStatusPageUsesTheInstancesButMaintenanceWins() {
        Product p = product();
        ServiceInstance i = instanceFor(p, organization());
        service.report(key(p), List.of(report(i, "DOWN")));
        ProductStatusDto dto = statusOf(p);
        assertThat(dto.status()).isEqualTo(ServiceStatusValue.MAJOR_OUTAGE);
        assertThat(dto.source()).isEqualTo("INSTANCES");
        assertThat(dto.downInstances()).isEqualTo(1);

        statusService.updateProductStatus(p.getId(), new UpdateProductStatusRequest(ServiceStatusValue.MAINTENANCE, "Planned"), "admin-sub");
        assertThat(statusOf(p).status()).isEqualTo(ServiceStatusValue.MAINTENANCE);
        assertThat(statusOf(p).source()).isEqualTo("MANUAL");
    }

    private ProductStatusDto statusOf(Product p) {
        return statusService.adminView().products().stream().filter(s -> s.productId().equals(p.getId())).findFirst().orElseThrow();
    }

    @Test
    void anAdminChangesAnInstanceByHandWithANoteAndItCountsAsCurrent() {
        Product p = product();
        ServiceInstance i = instanceFor(p, organization());
        assertThatThrownBy(() -> service.changeByHand("admin-sub", i.getId(), new ServiceInstanceDtos.ManualChange("FAILED", null, " ")))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.changeByHand("admin-sub", i.getId(), new ServiceInstanceDtos.ManualChange(null, null, "why")))
            .isInstanceOf(IllegalArgumentException.class);

        var changed = service.changeByHand("admin-sub", i.getId(), new ServiceInstanceDtos.ManualChange("ACTIVE", "DOWN", "Product cannot report"));
        assertThat(changed.status()).isEqualTo("ACTIVE");
        assertThat(changed.health()).isEqualTo("DOWN");
        assertThat(changed.changedBy()).isEqualTo("ADMIN");
        assertThat(changed.stale()).isFalse();
        assertThat(service.derivedStatus(p.getId()).orElseThrow().status()).isEqualTo(ServiceStatusValue.MAJOR_OUTAGE);
    }

    @Test
    void anOrganizationAdminSeesOnlyTheirOrganizationsInstancesAndAMemberCannot() {
        Product p = product();
        Organization mine = organization();
        Organization theirs = organization();
        ServiceInstance own = instanceFor(p, mine);
        ServiceInstance foreign = instanceFor(p, theirs);
        Customer admin = customer();
        memberService.addMember(mine.getId(), admin.getId(), OrgRole.ORG_ADMIN);
        Customer member = customer();
        memberService.addMember(mine.getId(), member.getId(), OrgRole.MEMBER);

        List<ServiceInstanceDtos.Instance> seen = service.listForOrganization(admin.getId());
        assertThat(seen).extracting(ServiceInstanceDtos.Instance::id).contains(own.getId()).doesNotContain(foreign.getId());
        assertThatThrownBy(() -> service.listForOrganization(member.getId())).isInstanceOf(RuntimeException.class);
    }

    @Test
    void reconcileCreatesMissingInstancesForActiveSubscriptions() {
        Product p = product();
        ProductSubscription s = subscribe(p, organization(), SubscriptionStatus.ACTIVE);
        assertThat(instanceRepository.findBySubscriptionId(s.getId())).isEmpty();
        service.reconcile();
        assertThat(instanceRepository.findBySubscriptionId(s.getId())).isPresent();
    }
}
