package com.vyoog.eisplatform.modules.authorization.repository;

import com.vyoog.eisplatform.modules.authorization.model.PrivilegedAccessAuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrivilegedAccessAuditEntryRepository extends JpaRepository<PrivilegedAccessAuditEntry, Long> {

    List<PrivilegedAccessAuditEntry> findByRequestIdOrderByOccurredAtAsc(Long requestId);
}
