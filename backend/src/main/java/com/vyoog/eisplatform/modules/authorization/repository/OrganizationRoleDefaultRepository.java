package com.vyoog.eisplatform.modules.authorization.repository;

import com.vyoog.eisplatform.modules.authorization.model.OrganizationRoleDefault;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRoleDefaultRepository extends JpaRepository<OrganizationRoleDefault, Long> {

    Optional<OrganizationRoleDefault> findByOrganizationIdAndOrgRoleAndItemTypeAndPermissionCode(
        Long organizationId, String orgRole, String itemType, String permissionCode);

    Optional<OrganizationRoleDefault> findByOrganizationIdAndOrgRoleAndItemTypeAndProductId(
        Long organizationId, String orgRole, String itemType, Long productId);

    List<OrganizationRoleDefault> findByOrganizationIdAndOrgRole(Long organizationId, String orgRole);
}
