package com.vyoog.eisplatform.modules.administration.repository;

import com.vyoog.eisplatform.modules.administration.model.PlatformRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformRegionRepository extends JpaRepository<PlatformRegion, Long> {

    Optional<PlatformRegion> findByCode(String code);

    boolean existsByCode(String code);
}
