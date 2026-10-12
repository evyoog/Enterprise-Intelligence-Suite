package com.vyoog.eisplatform.modules.access.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.InvalidStateException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.access.dto.AccessDtos.*;
import com.vyoog.eisplatform.modules.access.service.AccessSyncService.Change;
import com.vyoog.eisplatform.modules.access.service.AccessSyncService.Kind;
import com.vyoog.eisplatform.modules.audit.dto.AuditLogPageDto;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.authorization.model.MemberAccessOverride;
import com.vyoog.eisplatform.modules.authorization.model.OrganizationRoleDefault;
import com.vyoog.eisplatform.modules.authorization.repository.OrganizationRoleDefaultRepository;
import com.vyoog.eisplatform.modules.authorization.service.AccessCatalog;
import com.vyoog.eisplatform.modules.authorization.service.AuthorizationService;
import com.vyoog.eisplatform.modules.authorization.service.MemberAccessService;
import com.vyoog.eisplatform.modules.authorization.service.MemberAccessService.ItemState;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductPlan;
import com.vyoog.eisplatform.modules.product.repository.ProductPlanRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.OrganizationGroup;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationGroupRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import com.vyoog.eisplatform.modules.registration.service.ProductSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REQ-TEN-005 (C90): the Access management screen's service. Everything is scoped to the caller's own organization and
 * needs the feature permission <em>Manage access</em> (an organization administrator holds it by role). The guard rails
 * BR-2 to BR-6 are enforced here; each refusal names its rule. Every change is audited (BR-13) and the member is told
 * in-app and by email (AM-6).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccessManagementService {

    public static final List<String> ACTIVITY_ACTIONS = List.of("ACCESS_PERMISSION_CHANGED", "ACCESS_PRODUCT_CHANGED",
        "ACCESS_OVERRIDE_RESET", "ACCESS_ROLE_DEFAULT_CHANGED", "ACCESS_BULK_CHANGED", "MEMBER_ROLE_CHANGED",
        "MEMBER_ACCESS_REVIEWED", "PRODUCT_ACCESS_GRANTED", "PRODUCT_ACCESS_REVOKED", "SUBSCRIPTION_SEATS_CHANGED");

    private final OrganizationSelfService selfService;
    private final MemberAccessService memberAccessService;
    private final AccessSyncService syncService;
    private final OrganizationMemberRepository memberRepository;
    private final CustomerRepository customerRepository;
    private final OrganizationRoleDefaultRepository roleDefaultRepository;
    private final OrganizationProductAccessRepository accessRepository;
    private final OrganizationGroupRepository groupRepository;
    private final ProductPlanRepository planRepository;
    private final ProductSeatService seatService;
    private final AuthorizationService authorizationService;
    private final AuditService auditService;

    // ------------------------------------------------------------------ catalogue and lists

    public List<PermissionCatalogEntry> catalogue(Long customerId) {
        OrganizationMember caller = selfService.requireCallerWithPermission(customerId, AccessCatalog.MANAGE_ACCESS);
        return AccessCatalog.ENTRIES.stream()
            .map(e -> new PermissionCatalogEntry(e.code(), e.name(), e.description(), e.area().name(),
                memberAccessService.hasPermission(caller, e.code())))
            .toList();
    }

    public MemberPage listMembers(Long customerId, String q, String role, Long productId, Boolean custom,
                                  Instant reviewedBefore, int page, int size) {
        OrganizationMember caller = selfService.requireCallerWithPermission(customerId, AccessCatalog.MANAGE_ACCESS);
        Map<Long, Product> entitled = syncService.entitledProducts(caller.getOrganizationId());
        OrgRole roleFilter = role == null || role.isBlank() ? null : parseRole(role);
        String needle = q == null ? "" : q.trim().toLowerCase(Locale.ROOT);

        List<OrganizationMember> members = memberRepository.findByOrganizationId(caller.getOrganizationId()).stream()
            .filter(m -> m.getStatus() != MembershipStatus.INACTIVE)
            .filter(m -> roleFilter == null || m.getOrgRole() == roleFilter)
            .toList();
        Map<Long, Customer> customers = customerRepository.findAllById(members.stream().map(OrganizationMember::getCustomerId).toList())
            .stream().collect(Collectors.toMap(Customer::getId, c -> c));
        Map<Long, List<OrganizationProductAccess>> access = accessRepository
            .findByOrganizationMemberIdIn(members.stream().map(OrganizationMember::getId).toList()).stream()
            .filter(a -> a.getStatus() == MembershipStatus.ACTIVE)
            .collect(Collectors.groupingBy(OrganizationProductAccess::getOrganizationMemberId));

        List<MemberRow> rows = new ArrayList<>();
        for (OrganizationMember m : members) {
            Customer c = customers.get(m.getCustomerId());
            String name = displayName(c);
            String email = c == null ? null : c.getEmail();
            if (!needle.isEmpty() && !(name.toLowerCase(Locale.ROOT).contains(needle)
                || (email != null && email.toLowerCase(Locale.ROOT).contains(needle)))) {
                continue;
            }
            List<OrganizationProductAccess> mine = access.getOrDefault(m.getId(), List.of());
            int on = (int) mine.stream().filter(a -> entitled.containsKey(a.getProductId())).count();
            if (productId != null && mine.stream().noneMatch(a -> a.getProductId().equals(productId))) {
                continue;
            }
            int customCount = memberAccessService.overridesOf(m.getId()).size();
            if (Boolean.TRUE.equals(custom) && customCount == 0) {
                continue;
            }
            if (reviewedBefore != null && m.getLastReviewedAt() != null && !m.getLastReviewedAt().isBefore(reviewedBefore)) {
                continue;
            }
            String lockedReason = memberLockedReason(caller, m);
            rows.add(new MemberRow(m.getId(), m.getCustomerId(), name, email, m.getOrgRole().name(), m.getStatus().name(),
                on, entitled.size(), customCount, m.getLastReviewedAt(), reviewerName(m), lockedReason == null, lockedReason));
        }
        rows.sort(Comparator.comparing((MemberRow r) -> r.name().toLowerCase(Locale.ROOT)).thenComparing(MemberRow::memberId));

        int safeSize = size <= 0 || size > 200 ? 20 : size;
        int safePage = Math.max(page, 0);
        int from = Math.min(safePage * safeSize, rows.size());
        int to = Math.min(from + safeSize, rows.size());
        return new MemberPage(rows.subList(from, to), rows.size(), safePage, safeSize);
    }

    public MemberDetail memberDetail(Long customerId, Long memberId) {
        OrganizationMember caller = selfService.requireCallerWithPermission(customerId, AccessCatalog.MANAGE_ACCESS);
        return buildDetail(caller, targetOf(caller, memberId));
    }

    // ------------------------------------------------------------------ individual overrides

    @Transactional
    public MemberDetail setOverrides(Long customerId, Long memberId, SetOverridesRequest request) {
        OrganizationMember caller = selfService.requireCallerWithPermission(customerId, AccessCatalog.MANAGE_ACCESS);
        OrganizationMember target = targetOf(caller, memberId);
        requireChangeable(caller, target);
        List<String> lines = new ArrayList<>();

        if (request != null && request.permissions() != null) {
            for (Map.Entry<String, Boolean> e : request.permissions().entrySet()) {
                String line = applyPermission(caller, target, e.getKey(), Boolean.TRUE.equals(e.getValue()), customerId);
                if (line != null) {
                    lines.add(line);
                }
            }
        }
        if (request != null && request.products() != null) {
            for (Map.Entry<Long, Boolean> e : request.products().entrySet()) {
                String line = applyProduct(caller, target, e.getKey(), Boolean.TRUE.equals(e.getValue()), customerId);
                if (line != null) {
                    lines.add(line);
                }
            }
        }
        if (request != null && request.reset() != null) {
            lines.addAll(applyResets(target, request.reset(), customerId));
        }
        // settle: a reset product returns to what the role says
        List<Change> settled = syncService.recomputeMember(target, customerId);
        lines.addAll(AccessSyncService.describe(settled));
        syncService.notifyMember(target, lines);
        return buildDetail(caller, target);
    }

    @Transactional
    public MemberDetail resetAll(Long customerId, Long memberId) {
        OrganizationMember caller = selfService.requireCallerWithPermission(customerId, AccessCatalog.MANAGE_ACCESS);
        OrganizationMember target = targetOf(caller, memberId);
        requireChangeable(caller, target);
        List<String> keys = new ArrayList<>();
        for (MemberAccessOverride o : memberAccessService.overridesOf(target.getId())) {
            if (MemberAccessService.ITEM_PERMISSION.equals(o.getItemType())) {
                keys.add("permission:" + o.getPermissionCode());
            } else if (o.getProductId() != null) {
                keys.add("product:" + o.getProductId());
            }
        }
        List<String> lines = new ArrayList<>(applyResets(target, keys, customerId));
        lines.addAll(AccessSyncService.describe(syncService.recomputeMember(target, customerId)));
        syncService.notifyMember(target, lines);
        return buildDetail(caller, target);
    }

    // ------------------------------------------------------------------ role defaults

    public RoleDefaults roleDefaults(Long customerId, String role) {
        OrganizationMember caller = selfService.requireCallerWithPermission(customerId, AccessCatalog.MANAGE_ACCESS);
        return buildRoleDefaults(caller.getOrganizationId(), parseRole(role));
    }

    @Transactional
    public RoleDefaultsResult saveRoleDefaults(Long customerId, String roleName, RoleDefaultsRequest request, boolean preview) {
        OrganizationMember caller = selfService.requireCallerWithPermission(customerId, AccessCatalog.MANAGE_ACCESS);
        OrgRole role = parseRole(roleName);
        Long orgId = caller.getOrganizationId();
        if (role == OrgRole.ORG_ADMIN && caller.getOrgRole() != OrgRole.ORG_ADMIN) {
            throw new ForbiddenException("Only an organization administrator can change the defaults of the Organization admin role.");
        }
        List<OrganizationMember> roleMembers = memberRepository.findByOrganizationId(orgId).stream()
            .filter(m -> m.getOrgRole() == role && m.getStatus() == MembershipStatus.ACTIVE).toList();
        Map<Long, Product> entitled = syncService.entitledProducts(orgId);
        List<RoleDefaultImpactItem> impact = new ArrayList<>();
        Map<String, Boolean> permissionChanges = new LinkedHashMap<>();
        Map<Long, Boolean> productChanges = new LinkedHashMap<>();

        if (request != null && request.permissions() != null) {
            for (Map.Entry<String, Boolean> e : request.permissions().entrySet()) {
                AccessCatalog.Entry entry = AccessCatalog.find(e.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Unknown permission: " + e.getKey()));
                boolean to = Boolean.TRUE.equals(e.getValue());
                if (role == OrgRole.ORG_ADMIN && !to) {
                    throw new ForbiddenException("Organization administrators always hold " + entry.name() + " (it is locked on).");
                }
                boolean from = memberAccessService.roleDefaultPermission(orgId, role, entry.code());
                if (from == to) {
                    continue;
                }
                requireHolds(caller, entry);
                int affected = (int) roleMembers.stream().filter(m -> !hasPermissionOverride(m, entry.code())).count();
                impact.add(new RoleDefaultImpactItem("permission:" + entry.code(), entry.name(), from, to, affected));
                permissionChanges.put(entry.code(), to);
            }
        }
        if (request != null && request.products() != null) {
            for (Map.Entry<Long, Boolean> e : request.products().entrySet()) {
                Product product = entitled.get(e.getKey());
                if (product == null) {
                    throw new IllegalArgumentException("Product " + e.getKey() + " is not in the organization's active subscriptions.");
                }
                boolean to = Boolean.TRUE.equals(e.getValue());
                boolean from = memberAccessService.roleDefaultProduct(orgId, role, product.getId());
                if (from == to) {
                    continue;
                }
                int affected = (int) roleMembers.stream().filter(m -> !hasProductOverride(m, product.getId())).count();
                impact.add(new RoleDefaultImpactItem("product:" + product.getId(), product.getName(), from, to, affected));
                productChanges.put(product.getId(), to);
            }
        }
        int affectedDistinct = (int) roleMembers.stream().filter(m ->
            permissionChanges.keySet().stream().anyMatch(code -> !hasPermissionOverride(m, code))
                || productChanges.keySet().stream().anyMatch(id -> !hasProductOverride(m, id))).count();

        if (preview) {
            return new RoleDefaultsResult(true, affectedDistinct, 0, impact, buildRoleDefaults(orgId, role));
        }
        permissionChanges.forEach((code, to) -> storeRoleDefault(orgId, role, MemberAccessService.ITEM_PERMISSION, code, null, to,
            authorizationService.hasOrganizationPermission(role, code), customerId));
        productChanges.forEach((id, to) -> storeRoleDefault(orgId, role, MemberAccessService.ITEM_PRODUCT, null, id, to, true, customerId));
        for (RoleDefaultImpactItem item : impact) {
            auditService.recordSuccess("ACCESS_ROLE_DEFAULT_CHANGED", null, customerId, null, "OrganizationRoleDefault",
                role.name() + ":" + item.item(), orgId,
                "Role " + role + ": " + item.name() + " " + onOff(item.from()) + " -> " + onOff(item.to())
                    + " (" + item.affectedMembers() + " members affected)");
        }
        List<Change> changes = syncService.recomputeRole(orgId, role, customerId);
        syncService.notifyGroupedByMember(changes);
        // permission changes also change what members can do: tell the affected members
        if (!permissionChanges.isEmpty()) {
            for (OrganizationMember m : roleMembers) {
                List<String> lines = new ArrayList<>();
                permissionChanges.forEach((code, to) -> {
                    if (!hasPermissionOverride(m, code)) {
                        lines.add((to ? "You can now: " : "You can no longer: ") + AccessCatalog.find(code).map(AccessCatalog.Entry::name).orElse(code) + ".");
                    }
                });
                syncService.notifyMember(m, lines);
            }
        }
        int noSeat = (int) changes.stream().filter(c -> c.kind() == Kind.NO_SEAT).count();
        return new RoleDefaultsResult(false, affectedDistinct, noSeat, impact, buildRoleDefaults(orgId, role));
    }

    // ------------------------------------------------------------------ bulk

    @Transactional
    public BulkResult bulk(Long customerId, BulkRequest request) {
        OrganizationMember caller = selfService.requireCallerWithPermission(customerId, AccessCatalog.MANAGE_ACCESS);
        if (request == null || request.item() == null || request.memberIds() == null || request.memberIds().isEmpty()) {
            throw new IllegalArgumentException("Choose an item and at least one member.");
        }
        boolean grant = "GRANT".equalsIgnoreCase(request.action());
        if (!grant && !"REMOVE".equalsIgnoreCase(request.action())) {
            throw new IllegalArgumentException("Action must be GRANT or REMOVE.");
        }
        String[] parts = request.item().split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Item must look like product:7 or permission:MANAGE_ORDERS.");
        }
        Long orgId = caller.getOrganizationId();
        boolean isProduct = "product".equals(parts[0]);
        boolean isPermission = "permission".equals(parts[0]);
        if (!isProduct && !isPermission) {
            throw new IllegalArgumentException("Item must look like product:7 or permission:MANAGE_ORDERS.");
        }
        Long productId = isProduct ? parseLong(parts[1]) : null;
        Product product = null;
        AccessCatalog.Entry entry = null;
        if (isProduct) {
            product = syncService.entitledProducts(orgId).get(productId);
            if (product == null) {
                throw new IllegalArgumentException("That product is not in the organization's active subscriptions.");
            }
        } else {
            entry = AccessCatalog.find(parts[1]).orElseThrow(() -> new IllegalArgumentException("Unknown permission: " + parts[1]));
        }
        String itemName = isProduct ? product.getName() : entry.name();

        long seatBudget = isProduct && grant ? seatService.freeSeats(orgId, productId) : Long.MAX_VALUE;
        int applied = 0;
        List<BulkSkipped> skipped = new ArrayList<>();
        for (Long memberId : request.memberIds().stream().distinct().toList()) {
            Optional<OrganizationMember> found = memberRepository.findById(memberId)
                .filter(m -> m.getOrganizationId().equals(orgId));
            if (found.isEmpty()) {
                skipped.add(new BulkSkipped(memberId, null, "Member not found."));
                continue;
            }
            OrganizationMember target = found.get();
            String name = displayName(customerRepository.findById(target.getCustomerId()).orElse(null));
            String reason = memberLockedReason(caller, target);
            if (reason == null && isPermission) {
                if (target.getOrgRole() == OrgRole.ORG_ADMIN) {
                    reason = "Organization administrators always have every permission.";
                } else if (!memberAccessService.hasPermission(caller, entry.code())) {
                    reason = "You do not hold this permission, so you cannot " + (grant ? "grant" : "remove") + " it.";
                }
            }
            boolean alreadyThere;
            if (reason == null) {
                alreadyThere = isPermission
                    ? memberAccessService.permissionState(target, entry.code()).enabled() == grant
                    : isProductActive(target, productId) == grant;
                if (alreadyThere) {
                    reason = grant ? "Already on." : "Already off.";
                } else if (isProduct && grant && seatBudget <= 0) {
                    reason = "No free seats on " + product.getName() + ".";
                } else if (isProduct && !grant && isProjectSourced(target, productId)) {
                    reason = "The access comes from a project.";
                }
            }
            if (reason != null) {
                skipped.add(new BulkSkipped(memberId, name, reason));
                continue;
            }
            if (!request.preview()) {
                String line = isPermission
                    ? applyPermission(caller, target, entry.code(), grant, customerId)
                    : applyProduct(caller, target, productId, grant, customerId);
                syncService.notifyMember(target, line == null ? List.of() : List.of(line));
            }
            if (isProduct && grant && seatBudget != Long.MAX_VALUE) {
                seatBudget--;
            }
            applied++;
        }
        if (!request.preview()) {
            auditService.recordSuccess("ACCESS_BULK_CHANGED", null, customerId, null, "OrganizationMember", request.item(), orgId,
                (grant ? "Granted " : "Removed ") + itemName + " for " + applied + " members, " + skipped.size() + " skipped");
        }
        return new BulkResult(request.preview(), request.item(), itemName, grant ? "GRANT" : "REMOVE", applied, skipped);
    }

    // ------------------------------------------------------------------ activity

    public AuditLogPageDto activity(Long customerId, int page, int size) {
        Long orgId = selfService.requireOrganizationPermission(customerId, AccessCatalog.MANAGE_ACCESS);
        return auditService.searchActions(orgId, ACTIVITY_ACTIONS, page, size);
    }

    // ------------------------------------------------------------------ internals: changes

    private String applyPermission(OrganizationMember caller, OrganizationMember target, String code, boolean enabled, Long actor) {
        AccessCatalog.Entry entry = AccessCatalog.find(code)
            .orElseThrow(() -> new IllegalArgumentException("Unknown permission: " + code));
        if (target.getOrgRole() == OrgRole.ORG_ADMIN) {
            throw new ForbiddenException("Organization administrators always hold " + entry.name() + " (it is locked on).");
        }
        requireHolds(caller, entry);
        boolean from = memberAccessService.permissionState(target, code).enabled();
        memberAccessService.setPermissionWanted(target, code, enabled, actor);
        if (from == enabled) {
            return null;
        }
        ItemState now = memberAccessService.permissionState(target, code);
        auditService.recordSuccess("ACCESS_PERMISSION_CHANGED", null, actor, null, "OrganizationMember", String.valueOf(target.getId()),
            target.getOrganizationId(), "Member " + target.getId() + ": " + entry.name() + " " + onOff(from) + " -> " + onOff(enabled)
                + " (" + now.source() + ", by " + authority(caller) + ")");
        return (enabled ? "You can now: " : "You can no longer: ") + entry.name() + ".";
    }

    private String applyProduct(OrganizationMember caller, OrganizationMember target, Long productId, boolean enabled, Long actor) {
        Product product = syncService.entitledProducts(target.getOrganizationId()).get(productId);
        if (product == null) {
            throw new IllegalArgumentException("Product " + productId + " is not in the organization's active subscriptions.");
        }
        boolean from = isProductActive(target, productId);
        if (enabled) {
            syncService.grantProduct(target, productId, null, actor);
        } else {
            if (isProjectSourced(target, productId)) {
                throw new InvalidStateException("This access comes from a project. Remove the member from the project or the product from the project.");
            }
            syncService.revokeProduct(target, productId, actor);
        }
        if (from == enabled) {
            return null;
        }
        ItemState now = memberAccessService.productState(target, productId);
        auditService.recordSuccess("ACCESS_PRODUCT_CHANGED", null, actor, null, "OrganizationMember", String.valueOf(target.getId()),
            target.getOrganizationId(), "Member " + target.getId() + ": " + product.getName() + " " + onOff(from) + " -> " + onOff(enabled)
                + " (" + now.source() + ", by " + authority(caller) + ")");
        return enabled ? "You can now open " + product.getName() + "." : "You can no longer open " + product.getName() + ".";
    }

    private List<String> applyResets(OrganizationMember target, List<String> keys, Long actor) {
        List<String> lines = new ArrayList<>();
        for (String key : keys) {
            String[] parts = key.split(":", 2);
            if (parts.length != 2) {
                throw new IllegalArgumentException("Reset item must look like product:7 or permission:MANAGE_ORDERS.");
            }
            boolean removed;
            if ("permission".equals(parts[0])) {
                AccessCatalog.Entry entry = AccessCatalog.find(parts[1])
                    .orElseThrow(() -> new IllegalArgumentException("Unknown permission: " + parts[1]));
                boolean before = memberAccessService.permissionState(target, entry.code()).enabled();
                removed = memberAccessService.resetPermission(target, entry.code());
                boolean after = memberAccessService.permissionState(target, entry.code()).enabled();
                if (before != after) {
                    lines.add((after ? "You can now: " : "You can no longer: ") + entry.name() + ".");
                }
            } else if ("product".equals(parts[0])) {
                removed = memberAccessService.resetProduct(target, parseLong(parts[1]));
            } else {
                throw new IllegalArgumentException("Reset item must look like product:7 or permission:MANAGE_ORDERS.");
            }
            if (removed) {
                auditService.recordSuccess("ACCESS_OVERRIDE_RESET", null, actor, null, "OrganizationMember", String.valueOf(target.getId()),
                    target.getOrganizationId(), "Member " + target.getId() + ": " + key + " reset to the role default");
            }
        }
        return lines;
    }

    private void storeRoleDefault(Long orgId, OrgRole role, String itemType, String code, Long productId, boolean enabled,
                                  boolean baseline, Long actor) {
        Optional<OrganizationRoleDefault> existing = MemberAccessService.ITEM_PERMISSION.equals(itemType)
            ? roleDefaultRepository.findByOrganizationIdAndOrgRoleAndItemTypeAndPermissionCode(orgId, role.name(), itemType, code)
            : roleDefaultRepository.findByOrganizationIdAndOrgRoleAndItemTypeAndProductId(orgId, role.name(), itemType, productId);
        if (enabled == baseline) {
            existing.ifPresent(roleDefaultRepository::delete); // back to the platform definition
            return;
        }
        OrganizationRoleDefault row = existing.orElseGet(OrganizationRoleDefault::new);
        row.setOrganizationId(orgId);
        row.setOrgRole(role.name());
        row.setItemType(itemType);
        row.setPermissionCode(code);
        row.setProductId(productId);
        row.setEnabled(enabled);
        row.setUpdatedByCustomerId(actor);
        row.setUpdatedAt(Instant.now());
        roleDefaultRepository.save(row);
    }

    // ------------------------------------------------------------------ internals: guard rails

    private OrganizationMember targetOf(OrganizationMember caller, Long memberId) {
        return memberRepository.findById(memberId)
            .filter(m -> m.getOrganizationId().equals(caller.getOrganizationId()))
            .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
    }

    /** BR-3, BR-4 and an active member: the reason a member's access cannot be changed by the caller, or null. */
    private String memberLockedReason(OrganizationMember caller, OrganizationMember target) {
        if (caller.getId().equals(target.getId())) {
            return "You cannot change your own access.";
        }
        if (target.getOrgRole() == OrgRole.ORG_ADMIN && caller.getOrgRole() != OrgRole.ORG_ADMIN) {
            return "Only organization administrators can change an administrator's access.";
        }
        if (target.getStatus() != MembershipStatus.ACTIVE) {
            return "This member is not active.";
        }
        return null;
    }

    private void requireChangeable(OrganizationMember caller, OrganizationMember target) {
        String reason = memberLockedReason(caller, target);
        if (reason != null) {
            throw new ForbiddenException(reason);
        }
    }

    /** BR-2: a delegated administrator can grant or remove only what they hold themselves. */
    private void requireHolds(OrganizationMember caller, AccessCatalog.Entry entry) {
        if (!memberAccessService.hasPermission(caller, entry.code())) {
            throw new ForbiddenException("You do not hold \"" + entry.name() + "\", so you cannot grant or remove it.");
        }
    }

    private boolean hasPermissionOverride(OrganizationMember m, String code) {
        return memberAccessService.overridesOf(m.getId()).stream()
            .anyMatch(o -> MemberAccessService.ITEM_PERMISSION.equals(o.getItemType()) && code.equals(o.getPermissionCode()));
    }

    private boolean hasProductOverride(OrganizationMember m, Long productId) {
        return memberAccessService.overridesOf(m.getId()).stream()
            .anyMatch(o -> MemberAccessService.ITEM_PRODUCT.equals(o.getItemType()) && productId.equals(o.getProductId()));
    }

    private boolean isProductActive(OrganizationMember m, Long productId) {
        return accessRepository.findByOrganizationMemberIdAndProductId(m.getId(), productId)
            .filter(a -> a.getStatus() == MembershipStatus.ACTIVE).isPresent();
    }

    private boolean isProjectSourced(OrganizationMember m, Long productId) {
        return accessRepository.findByOrganizationMemberIdAndProductId(m.getId(), productId)
            .filter(a -> a.getStatus() == MembershipStatus.ACTIVE && a.getSourceGroupId() != null).isPresent();
    }

    // ------------------------------------------------------------------ internals: views

    private MemberDetail buildDetail(OrganizationMember caller, OrganizationMember target) {
        Customer c = customerRepository.findById(target.getCustomerId()).orElse(null);
        String memberReason = memberLockedReason(caller, target);
        Long orgId = target.getOrganizationId();
        Map<Long, Product> entitled = syncService.entitledProducts(orgId);

        List<ProductItem> products = new ArrayList<>();
        for (Product p : entitled.values()) {
            Optional<OrganizationProductAccess> row = accessRepository.findByOrganizationMemberIdAndProductId(target.getId(), p.getId());
            boolean active = row.filter(a -> a.getStatus() == MembershipStatus.ACTIVE).isPresent();
            ItemState wanted = memberAccessService.productState(target, p.getId());
            Optional<ProductSubscription> subscription = seatService.activeSubscription(orgId, p.getId());
            String plan = subscription.map(ProductSubscription::getPlanId).flatMap(planRepository::findById).map(ProductPlan::getName).orElse(null);
            long inUse = seatService.seatsInUse(orgId, p.getId());
            int quantity = subscription.map(ProductSubscription::getQuantity).orElse(0);
            String source = active && row.get().getSourceGroupId() != null ? "PROJECT" : wanted.source().name();
            String reason = null;
            if (active && row.get().getSourceGroupId() != null) {
                reason = "This access comes from project " + groupRepository.findById(row.get().getSourceGroupId())
                    .map(OrganizationGroup::getName).orElse("a project") + ".";
            } else if (!active && seatService.freeSeats(orgId, p.getId()) <= 0) {
                reason = "No free seats on " + p.getName() + ".";
            }
            products.add(new ProductItem(p.getId(), p.getName(), plan, active, wanted.enabled(), source,
                row.filter(a -> active).map(OrganizationProductAccess::getProductRole).orElse(null),
                new SeatInfo(inUse, quantity), reason));
        }
        List<PermissionItem> permissions = new ArrayList<>();
        for (AccessCatalog.Entry e : AccessCatalog.ENTRIES) {
            ItemState state = memberAccessService.permissionState(target, e.code());
            String reason = null;
            if (target.getOrgRole() == OrgRole.ORG_ADMIN) {
                reason = "Organization administrators always have this permission.";
            } else if (!memberAccessService.hasPermission(caller, e.code())) {
                reason = "You do not hold this permission, so you cannot grant or remove it.";
            }
            permissions.add(new PermissionItem(e.code(), e.name(), e.description(), e.area().name(), state.enabled(),
                state.source().name(), reason));
        }
        return new MemberDetail(target.getId(), displayName(c), c == null ? null : c.getEmail(), target.getOrgRole().name(),
            target.getStatus().name(), target.getLastReviewedAt(), reviewerName(target), memberReason == null, memberReason,
            products, permissions);
    }

    private RoleDefaults buildRoleDefaults(Long orgId, OrgRole role) {
        List<RoleDefaultPermission> permissions = new ArrayList<>();
        for (AccessCatalog.Entry e : AccessCatalog.ENTRIES) {
            boolean locked = role == OrgRole.ORG_ADMIN;
            boolean customized = roleDefaultRepository.findByOrganizationIdAndOrgRoleAndItemTypeAndPermissionCode(
                orgId, role.name(), MemberAccessService.ITEM_PERMISSION, e.code()).isPresent();
            permissions.add(new RoleDefaultPermission(e.code(), e.name(), e.description(), e.area().name(),
                memberAccessService.roleDefaultPermission(orgId, role, e.code()), locked, customized));
        }
        List<RoleDefaultProduct> products = new ArrayList<>();
        for (Product p : syncService.entitledProducts(orgId).values()) {
            boolean customized = roleDefaultRepository.findByOrganizationIdAndOrgRoleAndItemTypeAndProductId(
                orgId, role.name(), MemberAccessService.ITEM_PRODUCT, p.getId()).isPresent();
            products.add(new RoleDefaultProduct(p.getId(), p.getName(), memberAccessService.roleDefaultProduct(orgId, role, p.getId()), customized));
        }
        long members = memberRepository.findByOrganizationId(orgId).stream()
            .filter(m -> m.getOrgRole() == role && m.getStatus() == MembershipStatus.ACTIVE).count();
        return new RoleDefaults(role.name(), members, permissions, products);
    }

    private String reviewerName(OrganizationMember m) {
        if (m.getLastReviewedByCustomerId() == null) {
            return null;
        }
        return customerRepository.findById(m.getLastReviewedByCustomerId()).map(AccessManagementService::displayName).orElse(null);
    }

    private static String displayName(Customer c) {
        if (c == null) {
            return "";
        }
        return ((c.getFirstName() == null ? "" : c.getFirstName()) + " " + (c.getLastName() == null ? "" : c.getLastName())).trim();
    }

    private static String authority(OrganizationMember caller) {
        return caller.getOrgRole() == OrgRole.ORG_ADMIN ? "organization admin" : "delegated administrator";
    }

    private static String onOff(boolean value) {
        return value ? "on" : "off";
    }

    private static OrgRole parseRole(String role) {
        try {
            return OrgRole.valueOf(role.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Role must be ORG_ADMIN or MEMBER.");
        }
    }

    private static Long parseLong(String value) {
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid product id: " + value);
        }
    }
}
