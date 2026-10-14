package com.vyoog.eisplatform.modules.audit.repository;

import com.vyoog.eisplatform.modules.audit.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {
    /** C98: the latest administrator changes to one object, for example a payment gateway's history. */
    java.util.List<AuditLog> findTop10ByTargetTypeAndTargetIdOrderByTimestampDesc(String targetType, String targetId);
}
