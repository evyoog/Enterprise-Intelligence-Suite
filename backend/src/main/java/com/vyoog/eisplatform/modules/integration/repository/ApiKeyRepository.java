package com.vyoog.eisplatform.modules.integration.repository;

import com.vyoog.eisplatform.modules.integration.model.ApiKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    Optional<ApiKey> findByKeyPrefix(String keyPrefix);

    List<ApiKey> findByOwnerCustomerIdOrderByCreatedAtDesc(Long ownerCustomerId);

    Page<ApiKey> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("select count(k) from ApiKey k where k.ownerCustomerId = :owner and k.revokedAt is null "
        + "and (k.expiresAt is null or k.expiresAt > :now)")
    long countActive(@Param("owner") Long ownerCustomerId, @Param("now") Instant now);

    /** BR-8: one successful use. */
    @Modifying
    @Query("update ApiKey k set k.lastUsedAt = :now, k.requestCount = k.requestCount + 1 where k.id = :id")
    int recordUse(@Param("id") Long id, @Param("now") Instant now);
}
