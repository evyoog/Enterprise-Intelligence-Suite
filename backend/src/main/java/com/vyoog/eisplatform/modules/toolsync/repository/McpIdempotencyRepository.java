package com.vyoog.eisplatform.modules.toolsync.repository;

import com.vyoog.eisplatform.modules.toolsync.model.McpIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface McpIdempotencyRepository extends JpaRepository<McpIdempotency, McpIdempotency.Key> {

    @Modifying
    @Query("delete from McpIdempotency m where m.createdAt < :before")
    int deleteCreatedBefore(@Param("before") Instant before);
}
