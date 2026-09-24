package com.vyoog.eisplatform.modules.registration.repository;

import com.vyoog.eisplatform.modules.registration.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    @Query("select o from Organization o where lower(o.code) = lower(:code)")
    Optional<Organization> findByCodeIgnoreCase(@Param("code") String code);

    boolean existsByCodeIgnoreCase(String code);
}
