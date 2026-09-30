package com.vyoog.eisplatform.modules.platform.repository;

import com.vyoog.eisplatform.modules.platform.model.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformRepository extends JpaRepository<Platform, Long> {

    /** Used by {@code CatalogSeeder} to seed each eVyoog product suite idempotently. */
    Optional<Platform> findByName(String name);
}
