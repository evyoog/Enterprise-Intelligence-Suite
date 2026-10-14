package com.vyoog.eisplatform.modules.serviceinstance.repository;

import com.vyoog.eisplatform.modules.serviceinstance.model.ServiceReporterKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ServiceReporterKeyRepository extends JpaRepository<ServiceReporterKey, Long> {
    Optional<ServiceReporterKey> findByKeyHash(String keyHash);

    List<ServiceReporterKey> findAllByOrderByCreatedAtDesc();
}
