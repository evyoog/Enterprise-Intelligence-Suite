package com.vyoog.eisplatform.modules.toolsync.repository;

import com.vyoog.eisplatform.modules.toolsync.model.TenantAppSchema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantAppSchemaRepository extends JpaRepository<TenantAppSchema, Long> {

    Optional<TenantAppSchema> findByOrganizationIdAndProductId(Long organizationId, Long productId);

    List<TenantAppSchema> findByOrganizationId(Long organizationId);
}
