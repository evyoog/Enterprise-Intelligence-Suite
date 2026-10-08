package com.vyoog.eisplatform.modules.authorization.repository;

import com.vyoog.eisplatform.modules.authorization.model.MemberAccessOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberAccessOverrideRepository extends JpaRepository<MemberAccessOverride, Long> {

    Optional<MemberAccessOverride> findByOrganizationMemberIdAndItemTypeAndPermissionCode(
        Long organizationMemberId, String itemType, String permissionCode);

    List<MemberAccessOverride> findByOrganizationMemberId(Long organizationMemberId);

    List<MemberAccessOverride> findByPermissionCodeAndGrantedTrue(String permissionCode);
}
