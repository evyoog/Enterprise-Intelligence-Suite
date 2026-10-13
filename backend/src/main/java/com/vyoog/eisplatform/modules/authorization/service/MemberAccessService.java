package com.vyoog.eisplatform.modules.authorization.service;

import com.vyoog.eisplatform.modules.authorization.model.MemberAccessOverride;
import com.vyoog.eisplatform.modules.authorization.model.OrganizationRoleDefault;
import com.vyoog.eisplatform.modules.authorization.repository.MemberAccessOverrideRepository;
import com.vyoog.eisplatform.modules.authorization.repository.OrganizationRoleDefaultRepository;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Effective organization permissions of one member (REQ-TEN-005, BR-ACC-001): an organization administrator always
 * holds every catalogue permission (locked ON, BR-6); otherwise the member's individual override wins; otherwise the
 * organization's own role default; otherwise the platform role definition of {@link AuthorizationService}. A member
 * without overrides or organization role defaults behaves exactly as the platform role says.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAccessService {

    public static final String ITEM_PERMISSION = "PERMISSION";
    public static final String ITEM_PRODUCT = "PRODUCT";

    /** Where an effective value comes from. */
    public enum Source { LOCKED, OVERRIDE, ROLE }

    public record ItemState(boolean enabled, Source source) {
    }

    private final MemberAccessOverrideRepository overrideRepository;
    private final OrganizationRoleDefaultRepository roleDefaultRepository;
    private final AuthorizationService authorizationService;

    public ItemState permissionState(OrganizationMember member, String permissionName) {
        if (member.getOrgRole() == OrgRole.ORG_ADMIN && AccessCatalog.isOrganizationPermission(permissionName)) {
            return new ItemState(true, Source.LOCKED);
        }
        Optional<MemberAccessOverride> override = overrideRepository
            .findByOrganizationMemberIdAndItemTypeAndPermissionCode(member.getId(), ITEM_PERMISSION, permissionName);
        if (override.isPresent()) {
            return new ItemState(override.get().isGranted(), Source.OVERRIDE);
        }
        return new ItemState(roleDefaultPermission(member.getOrganizationId(), member.getOrgRole(), permissionName), Source.ROLE);
    }

    public boolean hasPermission(OrganizationMember member, String permissionName) {
        return permissionState(member, permissionName).enabled();
    }

    /** Product access wanted for the member (before entitlement and seats): override, else the organization's role default, else ON (AM-1). */
    public ItemState productState(OrganizationMember member, Long productId) {
        Optional<MemberAccessOverride> override = overrideRepository
            .findByOrganizationMemberIdAndItemTypeAndProductId(member.getId(), ITEM_PRODUCT, productId);
        if (override.isPresent()) {
            return new ItemState(override.get().isGranted(), Source.OVERRIDE);
        }
        return new ItemState(roleDefaultProduct(member.getOrganizationId(), member.getOrgRole(), productId), Source.ROLE);
    }

    public boolean roleDefaultProduct(Long organizationId, OrgRole role, Long productId) {
        return roleDefaultRepository
            .findByOrganizationIdAndOrgRoleAndItemTypeAndProductId(organizationId, role.name(), ITEM_PRODUCT, productId)
            .map(OrganizationRoleDefault::isEnabled).orElse(true);
    }

    /**
     * Stores or removes the individual product override so that the member's wanted state becomes {@code enabled}:
     * a value equal to the role default is stored as no override at all (the member simply follows the role).
     */
    @Transactional
    public void setProductWanted(OrganizationMember target, Long productId, boolean enabled, Long actorCustomerId) {
        Optional<MemberAccessOverride> existing = overrideRepository
            .findByOrganizationMemberIdAndItemTypeAndProductId(target.getId(), ITEM_PRODUCT, productId);
        if (enabled == roleDefaultProduct(target.getOrganizationId(), target.getOrgRole(), productId)) {
            existing.ifPresent(overrideRepository::delete);
            return;
        }
        MemberAccessOverride o = existing.orElseGet(MemberAccessOverride::new);
        o.setOrganizationMemberId(target.getId());
        o.setItemType(ITEM_PRODUCT);
        o.setPermissionCode(null);
        o.setProductId(productId);
        o.setGranted(enabled);
        o.setSetByCustomerId(actorCustomerId);
        o.setSetAt(Instant.now());
        overrideRepository.save(o);
    }

    /** Same for a feature permission: a value equal to the role default is stored as no override. */
    @Transactional
    public void setPermissionWanted(OrganizationMember target, String permissionName, boolean enabled, Long actorCustomerId) {
        if (enabled == roleDefaultPermission(target.getOrganizationId(), target.getOrgRole(), permissionName)) {
            overrideRepository.findByOrganizationMemberIdAndItemTypeAndPermissionCode(target.getId(), ITEM_PERMISSION, permissionName)
                .ifPresent(overrideRepository::delete);
            return;
        }
        setGranted(target, permissionName, enabled, actorCustomerId);
    }

    /** Removes one override (BR-10); true when there was one. */
    @Transactional
    public boolean resetPermission(OrganizationMember target, String permissionName) {
        Optional<MemberAccessOverride> o = overrideRepository
            .findByOrganizationMemberIdAndItemTypeAndPermissionCode(target.getId(), ITEM_PERMISSION, permissionName);
        o.ifPresent(overrideRepository::delete);
        return o.isPresent();
    }

    @Transactional
    public boolean resetProduct(OrganizationMember target, Long productId) {
        Optional<MemberAccessOverride> o = overrideRepository
            .findByOrganizationMemberIdAndItemTypeAndProductId(target.getId(), ITEM_PRODUCT, productId);
        o.ifPresent(overrideRepository::delete);
        return o.isPresent();
    }

    public List<MemberAccessOverride> overridesOf(Long memberId) {
        return overrideRepository.findByOrganizationMemberId(memberId);
    }

    /** What the member's role gives for this permission in this organization (no override considered). */
    public boolean roleDefaultPermission(Long organizationId, OrgRole role, String permissionName) {
        if (role == OrgRole.ORG_ADMIN && AccessCatalog.isOrganizationPermission(permissionName)) {
            return true;
        }
        Optional<OrganizationRoleDefault> own = roleDefaultRepository
            .findByOrganizationIdAndOrgRoleAndItemTypeAndPermissionCode(organizationId, role.name(), ITEM_PERMISSION, permissionName);
        if (own.isPresent()) {
            return own.get().isEnabled();
        }
        return authorizationService.hasOrganizationPermission(role, permissionName);
    }

    /** Effective permissions: the catalogue entries that are on, plus any other permission the platform role carries. */
    public List<String> listPermissions(OrganizationMember member) {
        List<String> result = new ArrayList<>();
        for (AccessCatalog.Entry entry : AccessCatalog.ENTRIES) {
            if (hasPermission(member, entry.code())) {
                result.add(entry.code());
            }
        }
        for (String name : authorizationService.listOrganizationPermissions(member.getOrgRole())) {
            if (!AccessCatalog.isOrganizationPermission(name) && !result.contains(name)) {
                result.add(name);
            }
        }
        return result;
    }

    /** Ids of members holding an individual grant of the permission (a member of a role that has it by default is not listed). */
    public List<Long> grantedMemberIds(String permissionName) {
        return overrideRepository.findByPermissionCodeAndGrantedTrue(permissionName).stream()
            .map(MemberAccessOverride::getOrganizationMemberId).toList();
    }

    /** Stores an individual override (granted or removed). Guard rails are the caller's job (AccessManagementService). */
    @Transactional
    public void setGranted(OrganizationMember target, String permissionName, boolean granted, Long actorCustomerId) {
        if (target.getOrgRole() == OrgRole.ORG_ADMIN && AccessCatalog.isOrganizationPermission(permissionName)) {
            throw new IllegalArgumentException("An organization administrator already holds this permission.");
        }
        MemberAccessOverride o = overrideRepository
            .findByOrganizationMemberIdAndItemTypeAndPermissionCode(target.getId(), ITEM_PERMISSION, permissionName)
            .orElseGet(MemberAccessOverride::new);
        o.setOrganizationMemberId(target.getId());
        o.setItemType(ITEM_PERMISSION);
        o.setPermissionCode(permissionName);
        o.setProductId(null);
        o.setGranted(granted);
        o.setSetByCustomerId(actorCustomerId);
        o.setSetAt(Instant.now());
        overrideRepository.save(o);
    }
}
