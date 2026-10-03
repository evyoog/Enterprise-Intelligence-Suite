package com.vyoog.eisplatform.modules.platform.repository;

import com.vyoog.eisplatform.modules.platform.model.Platform;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlatformRepository extends JpaRepository<Platform, Long> {

    /** Looks a platform up by its unique name. */
    Optional<Platform> findByName(String name);
}
