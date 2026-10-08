package com.vyoog.eisplatform.modules.authorization.service;

import com.vyoog.eisplatform.modules.authorization.model.MemberAccessOverride;
import com.vyoog.eisplatform.modules.authorization.repository.MemberAccessOverrideRepository;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Effective organization permissions of one member: the role defaults of
 * {@link AuthorizationService} with the member's individual overrides applied (REQ-TEN-005 slice,
 * REQ-TEN-008). A member without overrides behaves exactly as before.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAccessService {

    public static final String ITEM_PERMISSION = "PERMISSION";

    private final MemberAccessOverrideRepository overrideRepository;
    private final AuthorizationService authorizationService;

    public boolean hasPermission(OrganizationMember member, String permissionName) {
        Optional<MemberAccessOverride> override = overrideRepository
            .findByOrganizationMemberIdAndItemTypeAndPermissionCode(member.getId(), ITEM_PERMISSION, permissionName);
        if (override.isPresent()) {
            return override.get().isGranted();
        }
        return authorizationService.hasOrganizationPermission(member.getOrgRole(), permissionName);
    }

    /** Role defaults plus granted overrides minus removed ones — what /me/permissions reports. */
    public List<String> listPermissions(OrganizationMember member) {
        List<String> result = new ArrayList<>(authorizationService.listOrganizationPermissions(member.getOrgRole()));
        for (MemberAccessOverride o : overrideRepository.findByOrganizationMemberId(member.getId())) {
            if (!ITEM_PERMISSION.equals(o.getItemType())) {
                continue;
            }
            if (o.isGranted() && !result.contains(o.getPermissionCode())) {
                result.add(o.getPermissionCode());
            } else if (!o.isGranted()) {
                result.remove(o.getPermissionCode());
            }
        }
        return result;
    }

    /** Ids of members holding an individual grant of the permission (a member of a role that has it by default is not listed). */
    public List<Long> grantedMemberIds(String permissionName) {
        return overrideRepository.findByPermissionCodeAndGrantedTrue(permissionName).stream()
            .map(MemberAccessOverride::getOrganizationMemberId).toList();
    }

    @Transactional
    public void setGranted(OrganizationMember target, String permissionName, boolean granted, Long actorCustomerId) {
        if (target.getOrgRole() == OrgRole.ORG_ADMIN) {
            throw new IllegalArgumentException("An organization administrator already holds this permission.");
        }
        MemberAccessOverride o = overrideRepository
            .findByOrganizationMemberIdAndItemTypeAndPermissionCode(target.getId(), ITEM_PERMISSION, permissionName)
            .orElseGet(MemberAccessOverride::new);
        o.setOrganizationMemberId(target.getId());
        o.setItemType(ITEM_PERMISSION);
        o.setPermissionCode(permissionName);
        o.setGranted(granted);
        o.setSetByCustomerId(actorCustomerId);
        o.setSetAt(java.time.Instant.now());
        overrideRepository.save(o);
    }
}
