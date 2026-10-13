package com.vyoog.eisplatform.modules.access.service;

import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.authorization.service.MemberAccessService;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.event.OrganizationEntitlementsChangedEvent;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.ProductSeatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REQ-TEN-005 (C90): brings the stored product access of members in line with their effective access. A product is on
 * for a member when it is entitled (an ACTIVE organization subscription to an ACTIVE catalogue product), the member is
 * ACTIVE, the individual override or role default says ON (default ON, AM-1) and a named seat of that product is free
 * (AM-3 B). Direct rows only: access that comes from a project (REQ-TEN-009) is left to the project.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessSyncService {

    private static final Logger log = LoggerFactory.getLogger(AccessSyncService.class);

    /** The one default product role (AM-7). */
    public static final String DEFAULT_PRODUCT_ROLE = "USER";

    public enum Kind { GAINED, LOST, NO_SEAT }

    public record Change(Long memberId, Long productId, String productName, Kind kind) {
    }

    private final MemberAccessService memberAccessService;
    private final OrganizationMemberRepository memberRepository;
    private final OrganizationProductAccessRepository accessRepository;
    private final ProductSubscriptionRepository subscriptionRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final ProductSeatService productSeatService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    /** The products the organization is entitled to now: ACTIVE subscriptions of ACTIVE catalogue products (BR-7). */
    public Map<Long, Product> entitledProducts(Long organizationId) {
        Map<Long, Product> result = new LinkedHashMap<>();
        subscriptionRepository.findByOwnerOrganizationId(organizationId).stream()
            .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
            .map(s -> s.getProductId()).distinct().sorted()
            .forEach(id -> productRepository.findById(id)
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .ifPresent(p -> result.put(id, p)));
        return result;
    }

    /** Recalculates one member (BR-9: overrides are kept). Returns what changed; the caller decides about notices. */
    @Transactional
    public List<Change> recomputeMember(OrganizationMember member, Long actorCustomerId) {
        return recompute(member, entitledProducts(member.getOrganizationId()), actorCustomerId);
    }

    @Transactional
    public List<Change> recomputeOrganization(Long organizationId, Long actorCustomerId) {
        Map<Long, Product> entitled = entitledProducts(organizationId);
        List<Change> all = new ArrayList<>();
        memberRepository.findByOrganizationId(organizationId).stream()
            .sorted(Comparator.comparing(OrganizationMember::getId))
            .forEach(m -> all.addAll(recompute(m, entitled, actorCustomerId)));
        return all;
    }

    @Transactional
    public List<Change> recomputeRole(Long organizationId, OrgRole role, Long actorCustomerId) {
        Map<Long, Product> entitled = entitledProducts(organizationId);
        List<Change> all = new ArrayList<>();
        memberRepository.findByOrganizationId(organizationId).stream()
            .filter(m -> m.getOrgRole() == role)
            .sorted(Comparator.comparing(OrganizationMember::getId))
            .forEach(m -> all.addAll(recompute(m, entitled, actorCustomerId)));
        return all;
    }

    /** Entitlements changed (subscription created, suspended, reactivated, cancelled, expired): recalculate the organization. */
    @EventListener
    @Transactional
    public void onEntitlementsChanged(OrganizationEntitlementsChangedEvent event) {
        List<Change> changes = recomputeOrganization(event.organizationId(), null);
        notifyGroupedByMember(changes);
    }

    private List<Change> recompute(OrganizationMember member, Map<Long, Product> entitled, Long actorCustomerId) {
        Map<Long, OrganizationProductAccess> rows = accessRepository.findByOrganizationMemberId(member.getId()).stream()
            .collect(Collectors.toMap(OrganizationProductAccess::getProductId, a -> a, (a, b) -> a));
        boolean active = member.getStatus() == MembershipStatus.ACTIVE;
        List<Change> changes = new ArrayList<>();

        List<Long> productIds = new ArrayList<>(entitled.keySet());
        rows.values().stream().filter(r -> r.getStatus() == MembershipStatus.ACTIVE && r.getSourceGroupId() == null)
            .map(OrganizationProductAccess::getProductId).filter(id -> !entitled.containsKey(id)).forEach(productIds::add);

        for (Long productId : productIds) {
            boolean desired = active && entitled.containsKey(productId) && memberAccessService.productState(member, productId).enabled();
            OrganizationProductAccess row = rows.get(productId);
            boolean rowActive = row != null && row.getStatus() == MembershipStatus.ACTIVE;
            String name = Optional.ofNullable(entitled.get(productId)).map(Product::getName)
                .orElseGet(() -> productRepository.findById(productId).map(Product::getName).orElse("Product " + productId));
            if (desired && !rowActive) {
                if (productSeatService.freeSeats(member.getOrganizationId(), productId) <= 0) {
                    changes.add(new Change(member.getId(), productId, name, Kind.NO_SEAT));
                    continue;
                }
                if (row == null) {
                    row = new OrganizationProductAccess();
                    row.setOrganizationMemberId(member.getId());
                    row.setProductId(productId);
                    row.setProductRole(DEFAULT_PRODUCT_ROLE);
                }
                row.setStatus(MembershipStatus.ACTIVE);
                row.setSourceGroupId(null);
                row.setAssignedAt(Instant.now());
                accessRepository.save(row);
                audit("PRODUCT_ACCESS_GRANTED", actorCustomerId, member, productId, "Product access turned on by effective access");
                changes.add(new Change(member.getId(), productId, name, Kind.GAINED));
            } else if (!desired && rowActive && row.getSourceGroupId() == null) {
                row.setStatus(MembershipStatus.INACTIVE);
                accessRepository.save(row);
                audit("PRODUCT_ACCESS_REVOKED", actorCustomerId, member, productId, "Product access turned off by effective access");
                changes.add(new Change(member.getId(), productId, name, Kind.LOST));
            }
        }
        return changes;
    }

    // ------------------------------------------------------------------ direct grant / revoke

    /**
     * Turns one product ON for one member now (individual override unless it equals the role default) with the given
     * product role (null = keep, else the default role). The caller has checked permissions; entitlement and seats are
     * checked here (BR-7, BR-8).
     */
    @Transactional
    public void grantProduct(OrganizationMember target, Long productId, String productRole, Long actorCustomerId) {
        Product product = productRepository.findById(productId)
            .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new IllegalArgumentException("This product is not currently available."));
        if (productSeatService.activeSubscription(target.getOrganizationId(), productId).isEmpty()) {
            throw new IllegalArgumentException("Your organization does not have an active subscription for this product.");
        }
        memberAccessService.setProductWanted(target, productId, true, actorCustomerId);
        OrganizationProductAccess row = accessRepository.findByOrganizationMemberIdAndProductId(target.getId(), productId)
            .orElseGet(OrganizationProductAccess::new);
        boolean wasActive = row.getId() != null && row.getStatus() == MembershipStatus.ACTIVE;
        if (!wasActive) {
            productSeatService.assertSeatFree(target.getOrganizationId(), productId, product.getName());
        }
        row.setOrganizationMemberId(target.getId());
        row.setProductId(productId);
        if (productRole != null && !productRole.isBlank()) {
            row.setProductRole(productRole);
        } else if (row.getProductRole() == null) {
            row.setProductRole(DEFAULT_PRODUCT_ROLE);
        }
        row.setStatus(MembershipStatus.ACTIVE);
        row.setSourceGroupId(null); // REQ-TEN-009 BR-PRJ-006: a direct assignment always wins over a project
        if (!wasActive || row.getAssignedAt() == null) {
            row.setAssignedAt(Instant.now());
        }
        accessRepository.save(row);
    }

    /** Turns one product OFF for one member now (individual override unless it equals the role default). */
    @Transactional
    public void revokeProduct(OrganizationMember target, Long productId, Long actorCustomerId) {
        memberAccessService.setProductWanted(target, productId, false, actorCustomerId);
        accessRepository.findByOrganizationMemberIdAndProductId(target.getId(), productId).ifPresent(row -> {
            if (row.getStatus() == MembershipStatus.ACTIVE) {
                row.setStatus(MembershipStatus.INACTIVE);
                accessRepository.save(row);
            }
        });
    }

    // ------------------------------------------------------------------ notices (AM-6: in-app and email)

    /** One notice to the member listing what changed for them; a failed notice is only logged. */
    public void notifyMember(OrganizationMember member, List<String> lines) {
        if (lines.isEmpty()) {
            return;
        }
        try {
            customerRepository.findById(member.getCustomerId()).ifPresent(c ->
                notificationService.notifyTemplated(c.getId(), c.getEmail(), NotificationCategory.ORGANIZATION, NotificationSeverity.INFO,
                    "NOTIF_ACCESS_CHANGED", java.util.Map.of("changes", String.join(" ", lines))));
        } catch (RuntimeException e) {
            log.warn("Could not notify member {} of an access change: {}", member.getId(), e.getMessage());
        }
    }

    /** Product gains and losses become one notice per member. */
    public void notifyGroupedByMember(List<Change> changes) {
        Map<Long, List<Change>> byMember = new LinkedHashMap<>();
        changes.stream().filter(c -> c.kind() != Kind.NO_SEAT).forEach(c -> byMember.computeIfAbsent(c.memberId(), k -> new ArrayList<>()).add(c));
        byMember.forEach((memberId, list) -> memberRepository.findById(memberId).ifPresent(m -> notifyMember(m, describe(list))));
    }

    public static List<String> describe(List<Change> changes) {
        List<String> lines = new ArrayList<>();
        changes.stream().filter(c -> c.kind() == Kind.GAINED).forEach(c -> lines.add("You can now open " + c.productName() + "."));
        changes.stream().filter(c -> c.kind() == Kind.LOST).forEach(c -> lines.add("You can no longer open " + c.productName() + "."));
        return lines;
    }

    private void audit(String action, Long actorCustomerId, OrganizationMember member, Long productId, String detail) {
        auditService.recordSuccess(action, null, actorCustomerId, null, "OrganizationProductAccess",
            productId + ":" + member.getId(), member.getOrganizationId(), detail + " (product " + productId + ", member " + member.getId() + ")");
    }
}
