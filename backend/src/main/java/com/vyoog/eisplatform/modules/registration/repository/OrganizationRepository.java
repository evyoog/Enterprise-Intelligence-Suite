package com.vyoog.eisplatform.modules.registration.repository;

import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationLifecycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    @Query("select o from Organization o where lower(o.code) = lower(:code)")
    Optional<Organization> findByCodeIgnoreCase(@Param("code") String code);

    boolean existsByCodeIgnoreCase(String code);

    /** 05.02.01.03 Assign region — used by OrganizationRegionUsageGuard to
     * block deleting a region still assigned to an organization. */
    boolean existsByRegionId(Long regionId);

    /** Platform admin dashboard: organizations currently usable (not
     * SUSPENDED/CLOSED) — see OrganizationLifecycleStatus's own javadoc. */
    long countByLifecycleStatus(OrganizationLifecycleStatus lifecycleStatus);
}
