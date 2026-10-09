package com.vyoog.eisplatform.modules.toolsync.repository;

import com.vyoog.eisplatform.modules.toolsync.model.ConnectorStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToolConnectorRepository extends JpaRepository<ToolConnector, Long> {

    List<ToolConnector> findByStatus(ConnectorStatus status);

    boolean existsByStatus(ConnectorStatus status);

    Optional<ToolConnector> findByProductCode(String productCode);

    Optional<ToolConnector> findByProductId(Long productId);

    Optional<ToolConnector> findByClientIdAndStatus(String clientId, ConnectorStatus status);
}
