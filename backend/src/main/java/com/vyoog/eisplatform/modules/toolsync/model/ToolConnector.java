package com.vyoog.eisplatform.modules.toolsync.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/** A tool (a product with an application that keeps a copy of platform data) as the platform reaches it. REQ-INT-003, table {@code tool_connector}. */
@Entity
@Table(name = "tool_connector")
@Getter
@Setter
public class ToolConnector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    /** The product code in every message and in the tool's own configuration, for example {@code thittam}. */
    @Column(name = "product_code", nullable = false, unique = true, length = 100)
    private String productCode;

    /** The tool's MCP endpoint, for example {@code https://pms.evyoog.com/api/mcp}. */
    @Column(name = "base_mcp_url", nullable = false, length = 500)
    private String baseMcpUrl;

    /** The tool's Keycloak service client: the {@code azp} the platform accepts from that tool on its own MCP endpoint. */
    @Column(name = "client_id", nullable = false, length = 100)
    private String clientId;

    @Column(name = "contract_version", nullable = false, length = 10)
    private String contractVersion = "1";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ConnectorStatus status = ConnectorStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();
}
