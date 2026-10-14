package com.vyoog.eisplatform.modules.serviceinstance.service;

import com.vyoog.eisplatform.common.exception.InvalidCredentialsException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import com.vyoog.eisplatform.modules.serviceinstance.dto.ServiceInstanceDtos;
import com.vyoog.eisplatform.modules.serviceinstance.model.InstanceHealth;
import com.vyoog.eisplatform.modules.serviceinstance.model.InstanceStatus;
import com.vyoog.eisplatform.modules.serviceinstance.model.ServiceInstance;
import com.vyoog.eisplatform.modules.serviceinstance.model.ServiceReporterKey;
import com.vyoog.eisplatform.modules.serviceinstance.repository.ServiceInstanceRepository;
import com.vyoog.eisplatform.modules.serviceinstance.repository.ServiceReporterKeyRepository;
import com.vyoog.eisplatform.modules.servicestatus.model.ServiceStatusValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * C101 / REQ-SRM-001 (SR-1 to SR-5): service instances and their health. One instance per subscription, kept in step with the subscription; a product
 * reports the health of its own instances with a reporter key (every minute, SR-2 A); a platform administrator can set an instance's status and health by
 * hand (SR-5 B); an organization admin sees the organization's instances (SR-4 A). The status page's per-product status is derived from the instances
 * that reported recently (SR-3 B): {@link #derivedStatus}.
 */
@Service
@RequiredArgsConstructor
public class ServiceInstanceService {

    /** SR-2 A: a product reports every minute; a report older than five of those intervals is "no recent report". */
    public static final Duration STALE_AFTER = Duration.ofMinutes(5);
    private static final int MAX_REPORTS = 200;

    private final ServiceInstanceRepository instanceRepository;
    private final ServiceReporterKeyRepository keyRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationSelfService organizationSelfService;
    private final AuditService auditService;

    // ------------------------------------------------------------------ kept in step with subscriptions

    /** Creates or updates the instance of a subscription. A subscription that is still waiting for payment has no instance yet. */
    @Transactional
    public void syncFromSubscription(ProductSubscription subscription) {
        if (subscription == null || subscription.getId() == null) {
            return;
        }
        SubscriptionStatus status = subscription.getStatus();
        Optional<ServiceInstance> existing = instanceRepository.findBySubscriptionId(subscription.getId());
        if (existing.isEmpty() && status != SubscriptionStatus.ACTIVE) {
            return;
        }
        ServiceInstance instance = existing.orElseGet(() -> {
            ServiceInstance created = new ServiceInstance();
            created.setSubscriptionId(subscription.getId());
            created.setProductId(subscription.getProductId());
            created.setOwnerCustomerId(subscription.getOwnerCustomerId());
            created.setOwnerOrganizationId(subscription.getOwnerOrganizationId());
            return created;
        });
        InstanceStatus target = instance.getStatus();
        if (status == SubscriptionStatus.ACTIVE) {
            if (existing.isEmpty() || target == InstanceStatus.SUSPENDED || target == InstanceStatus.DEPROVISIONED) {
                target = instance.getLastReportedAt() == null ? InstanceStatus.PROVISIONING : InstanceStatus.ACTIVE;
            }
        } else if (status == SubscriptionStatus.SUSPENDED) {
            target = InstanceStatus.SUSPENDED;
        } else if (status == SubscriptionStatus.CANCELLED || status == SubscriptionStatus.EXPIRED) {
            target = InstanceStatus.DEPROVISIONED;
        }
        if (existing.isPresent() && target == instance.getStatus()) {
            return;
        }
        instance.setStatus(target);
        instance.setLastChangedBy("SYSTEM");
        instance.setLastChangeNote("Subscription " + status.name().toLowerCase(Locale.ROOT));
        instance.setUpdatedAt(Instant.now());
        instanceRepository.save(instance);
    }

    /** A safety net for the hook above: brings every instance in step with its subscription. */
    @Transactional
    public int reconcile() {
        int changed = 0;
        for (ProductSubscription subscription : subscriptionRepository.findAll()) {
            Optional<ServiceInstance> before = instanceRepository.findBySubscriptionId(subscription.getId());
            InstanceStatus was = before.map(ServiceInstance::getStatus).orElse(null);
            syncFromSubscription(subscription);
            InstanceStatus now = instanceRepository.findBySubscriptionId(subscription.getId()).map(ServiceInstance::getStatus).orElse(null);
            if (was != now) {
                changed++;
            }
        }
        return changed;
    }

    // ------------------------------------------------------------------ reports from a product

    /** Applies a product's health reports. The key names the product; a report for another product's instance is rejected. */
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public ServiceInstanceDtos.ReportResult report(String rawKey, List<ServiceInstanceDtos.HealthReport> reports) {
        ServiceReporterKey key = rawKey == null || rawKey.isBlank() ? null : keyRepository.findByKeyHash(hash(rawKey.trim())).orElse(null);
        if (key == null || key.getRevokedAt() != null) {
            throw new InvalidCredentialsException("Invalid or revoked reporter key.");
        }
        if (reports == null || reports.isEmpty() || reports.size() > MAX_REPORTS) {
            throw new IllegalArgumentException("Send between 1 and " + MAX_REPORTS + " reports.");
        }
        key.setLastUsedAt(Instant.now());
        keyRepository.save(key);
        int accepted = 0;
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < reports.size(); i++) {
            String problem = apply(key, reports.get(i));
            if (problem == null) {
                accepted++;
            } else {
                errors.add("Report " + (i + 1) + ": " + problem);
            }
        }
        return new ServiceInstanceDtos.ReportResult(accepted, reports.size() - accepted, errors);
    }

    private String apply(ServiceReporterKey key, ServiceInstanceDtos.HealthReport report) {
        ServiceInstance instance = report.instanceId() != null ? instanceRepository.findById(report.instanceId()).orElse(null)
            : report.subscriptionId() != null ? instanceRepository.findBySubscriptionId(report.subscriptionId()).orElse(null) : null;
        if (instance == null || !instance.getProductId().equals(key.getProductId())) {
            return "unknown instance for this product";
        }
        InstanceHealth health;
        try {
            health = InstanceHealth.valueOf(report.health() == null ? "" : report.health().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return "health must be HEALTHY, DEGRADED or DOWN";
        }
        if (health == InstanceHealth.UNKNOWN) {
            return "health must be HEALTHY, DEGRADED or DOWN";
        }
        if (instance.getStatus() == InstanceStatus.SUSPENDED || instance.getStatus() == InstanceStatus.DEPROVISIONED) {
            return "the instance is " + instance.getStatus().name().toLowerCase(Locale.ROOT);
        }
        InstanceStatus reported = null;
        if (report.status() != null && !report.status().isBlank()) {
            try {
                reported = InstanceStatus.valueOf(report.status().trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return "status must be PROVISIONING, ACTIVE or FAILED";
            }
            if (reported != InstanceStatus.PROVISIONING && reported != InstanceStatus.ACTIVE && reported != InstanceStatus.FAILED) {
                return "status must be PROVISIONING, ACTIVE or FAILED";
            }
        }
        Instant now = Instant.now();
        instance.setHealth(health);
        instance.setHealthMessage(cut(report.message(), 300));
        instance.setLastReportedAt(now);
        if (report.tenantReference() != null && !report.tenantReference().isBlank()) {
            instance.setTenantReference(cut(report.tenantReference(), 100));
        }
        if (report.accessUrl() != null && !report.accessUrl().isBlank()) {
            instance.setAccessUrl(cut(report.accessUrl(), 500));
        }
        if (reported != null) {
            instance.setStatus(reported);
        } else if (instance.getStatus() == InstanceStatus.PROVISIONING) {
            instance.setStatus(InstanceStatus.ACTIVE);   // the first report proves the tenant is there
        }
        instance.setLastChangedBy("PRODUCT");
        instance.setLastChangeNote(null);
        instance.setUpdatedAt(now);
        instanceRepository.save(instance);
        return null;
    }

    // ------------------------------------------------------------------ platform administrators

    @Transactional(readOnly = true)
    public List<ServiceInstanceDtos.Instance> listAll(Long productId, String status, String health) {
        Maps maps = maps();
        return instanceRepository.findAllByOrderByProductIdAscIdAsc().stream()
            .filter(i -> productId == null || productId.equals(i.getProductId()))
            .filter(i -> status == null || status.isBlank() || i.getStatus().name().equalsIgnoreCase(status))
            .filter(i -> health == null || health.isBlank() || effectiveHealth(i).name().equalsIgnoreCase(health))
            .map(i -> toDto(i, maps)).toList();
    }

    @Transactional(readOnly = true)
    public ServiceInstanceDtos.Summary summary(Long productId) {
        List<ServiceInstance> all = instanceRepository.findAll().stream()
            .filter(i -> productId == null || productId.equals(i.getProductId()))
            .filter(i -> i.getStatus() == InstanceStatus.ACTIVE || i.getStatus() == InstanceStatus.PROVISIONING).toList();
        int healthy = 0, degraded = 0, down = 0, unknown = 0, noReport = 0;
        for (ServiceInstance i : all) {
            if (isStale(i)) {
                noReport++;
                continue;
            }
            switch (i.getHealth()) {
                case HEALTHY -> healthy++;
                case DEGRADED -> degraded++;
                case DOWN -> down++;
                default -> unknown++;
            }
        }
        return new ServiceInstanceDtos.Summary(all.size(), healthy, degraded, down, unknown, noReport);
    }

    @Transactional(readOnly = true)
    public ServiceInstanceDtos.Instance get(Long id) {
        return toDto(instanceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service instance not found")), maps());
    }

    /** SR-5 B: sets an instance's status and/or health by hand, for example when a product cannot report. A note is required and the change is audited. */
    @Transactional
    public ServiceInstanceDtos.Instance changeByHand(String actorSub, Long id, ServiceInstanceDtos.ManualChange change) {
        ServiceInstance instance = instanceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service instance not found"));
        String note = change.note() == null ? "" : change.note().trim();
        if (note.isEmpty()) {
            throw new IllegalArgumentException("Write a note saying why you are changing this instance.");
        }
        if ((change.status() == null || change.status().isBlank()) && (change.health() == null || change.health().isBlank())) {
            throw new IllegalArgumentException("Choose a status, a health, or both.");
        }
        String before = instance.getStatus() + "/" + instance.getHealth();
        if (change.status() != null && !change.status().isBlank()) {
            try {
                instance.setStatus(InstanceStatus.valueOf(change.status().trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Status must be PROVISIONING, ACTIVE, SUSPENDED, FAILED or DEPROVISIONED.");
            }
        }
        if (change.health() != null && !change.health().isBlank()) {
            try {
                instance.setHealth(InstanceHealth.valueOf(change.health().trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Health must be HEALTHY, DEGRADED, DOWN or UNKNOWN.");
            }
            instance.setLastReportedAt(Instant.now());   // a hand-set health counts as current, so the status page uses it
        }
        instance.setLastChangedBy("ADMIN");
        instance.setLastChangeNote(cut(note, 300));
        instance.setUpdatedAt(Instant.now());
        instance = instanceRepository.save(instance);
        auditService.recordSuccess("SERVICE_INSTANCE_CHANGED", actorSub, null, null, "ServiceInstance", id.toString(), instance.getOwnerOrganizationId(),
            "Instance " + id + ": " + before + " -> " + instance.getStatus() + "/" + instance.getHealth() + " (" + note + ")");
        return toDto(instance, maps());
    }

    @Transactional(readOnly = true)
    public List<ServiceInstanceDtos.ReporterKey> listKeys() {
        Map<Long, String> names = productNames();
        return keyRepository.findAllByOrderByCreatedAtDesc().stream().map(k -> toDto(k, names)).toList();
    }

    @Transactional
    public ServiceInstanceDtos.CreatedReporterKey createKey(String actorSub, ServiceInstanceDtos.CreateReporterKey request) {
        Product product = productRepository.findById(request.productId()).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        byte[] random = new byte[32];
        new SecureRandom().nextBytes(random);
        String secret = "eisrk_" + Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        ServiceReporterKey key = new ServiceReporterKey();
        key.setProductId(product.getId());
        key.setLabel(request.label() == null || request.label().isBlank() ? null : cut(request.label().trim(), 100));
        key.setKeyPrefix(secret.substring(0, 12));
        key.setKeyHash(hash(secret));
        key.setCreatedByKeycloakSub(actorSub);
        key = keyRepository.save(key);
        auditService.recordSuccess("SERVICE_REPORTER_KEY_CREATED", actorSub, null, null, "ServiceReporterKey", key.getId().toString(), null,
            "Reporter key " + key.getKeyPrefix() + " created for " + product.getName());
        return new ServiceInstanceDtos.CreatedReporterKey(toDto(key, Map.of(product.getId(), product.getName())), secret);
    }

    @Transactional
    public ServiceInstanceDtos.ReporterKey revokeKey(String actorSub, Long id) {
        ServiceReporterKey key = keyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Reporter key not found"));
        if (key.getRevokedAt() == null) {
            key.setRevokedAt(Instant.now());
            keyRepository.save(key);
            auditService.recordSuccess("SERVICE_REPORTER_KEY_REVOKED", actorSub, null, null, "ServiceReporterKey", id.toString(), null,
                "Reporter key " + key.getKeyPrefix() + " revoked");
        }
        return toDto(key, productNames());
    }

    // ------------------------------------------------------------------ organization admins

    /** SR-4 A: an organization admin's own organization's instances. */
    @Transactional(readOnly = true)
    public List<ServiceInstanceDtos.Instance> listForOrganization(Long callerCustomerId) {
        Long organizationId = organizationSelfService.requireOrganizationManagement(callerCustomerId);
        Maps maps = maps();
        return instanceRepository.findByOwnerOrganizationIdOrderByProductIdAscIdAsc(organizationId).stream()
            .filter(i -> i.getStatus() != InstanceStatus.DEPROVISIONED)
            .map(i -> toDto(i, maps)).toList();
    }

    // ------------------------------------------------------------------ status page (SR-3 B)

    /** What a product's instances say about its status, or empty when none of them has reported recently. More than half Down = Major outage; at least one
     * Down = Partial outage; at least one Degraded = Degraded; otherwise Operational. */
    @Transactional(readOnly = true)
    public Optional<Derived> derivedStatus(Long productId) {
        List<ServiceInstance> reporting = instanceRepository.findByProductId(productId).stream()
            .filter(i -> i.getStatus() == InstanceStatus.ACTIVE || i.getStatus() == InstanceStatus.FAILED)
            .filter(i -> !isStale(i) && i.getHealth() != InstanceHealth.UNKNOWN).toList();
        if (reporting.isEmpty()) {
            return Optional.empty();
        }
        int down = (int) reporting.stream().filter(i -> i.getHealth() == InstanceHealth.DOWN || i.getStatus() == InstanceStatus.FAILED).count();
        int degraded = (int) reporting.stream().filter(i -> i.getHealth() == InstanceHealth.DEGRADED && i.getStatus() != InstanceStatus.FAILED).count();
        ServiceStatusValue value = down * 2 > reporting.size() ? ServiceStatusValue.MAJOR_OUTAGE
            : down > 0 ? ServiceStatusValue.PARTIAL_OUTAGE : degraded > 0 ? ServiceStatusValue.DEGRADED : ServiceStatusValue.OPERATIONAL;
        return Optional.of(new Derived(value, reporting.size(), down, degraded));
    }

    /** The product status the instances give, with the counts behind it. */
    public record Derived(ServiceStatusValue status, int reporting, int down, int degraded) {
    }

    // ------------------------------------------------------------------ helpers

    public boolean isStale(ServiceInstance i) {
        return i.getLastReportedAt() == null || i.getLastReportedAt().isBefore(Instant.now().minus(STALE_AFTER));
    }

    private InstanceHealth effectiveHealth(ServiceInstance i) {
        return isStale(i) ? InstanceHealth.UNKNOWN : i.getHealth();
    }

    private record Maps(Map<Long, String> products, Map<Long, String> organizations, Map<Long, String> customers) {
    }

    private Maps maps() {
        return new Maps(productNames(),
            organizationRepository.findAll().stream().collect(Collectors.toMap(o -> o.getId(), o -> o.getName(), (a, b) -> a)),
            customerRepository.findAll().stream().collect(Collectors.toMap(Customer::getId, c -> (c.getFirstName() + " " + c.getLastName()).trim(), (a, b) -> a)));
    }

    private Map<Long, String> productNames() {
        return productRepository.findAll().stream().collect(Collectors.toMap(Product::getId, Product::getName, (a, b) -> a));
    }

    private ServiceInstanceDtos.Instance toDto(ServiceInstance i, Maps maps) {
        boolean organization = i.getOwnerOrganizationId() != null;
        String owner = organization ? maps.organizations().get(i.getOwnerOrganizationId()) : maps.customers().get(i.getOwnerCustomerId());
        return new ServiceInstanceDtos.Instance(i.getId(), i.getSubscriptionId(), i.getProductId(), maps.products().get(i.getProductId()),
            organization ? "ORGANIZATION" : "INDIVIDUAL", i.getOwnerOrganizationId(), owner, i.getStatus().name(), i.getHealth().name(), i.getHealthMessage(),
            i.getLastReportedAt(), isStale(i), i.getTenantReference(), i.getAccessUrl(), i.getLastChangedBy(), i.getLastChangeNote(), i.getCreatedAt());
    }

    private ServiceInstanceDtos.ReporterKey toDto(ServiceReporterKey k, Map<Long, String> names) {
        return new ServiceInstanceDtos.ReporterKey(k.getId(), k.getProductId(), names.get(k.getProductId()), k.getLabel(), k.getKeyPrefix(),
            k.getCreatedAt(), k.getLastUsedAt(), k.getRevokedAt());
    }

    private static String cut(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }

    static String hash(String secret) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(secret.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
