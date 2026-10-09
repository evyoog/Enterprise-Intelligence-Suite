package com.vyoog.eisplatform.modules.toolsync.gateway;

import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;

import java.util.Map;

/**
 * The platform calling a tool (contract v1 section 4). One attempt per call, a bounded wait; retries and backoff belong to
 * {@code ToolDeliveryService}. MCP is only the transport, so tests replace this with a fake.
 */
public interface ToolGateway {

    /**
     * @throws ToolUnavailableException when the tool or the identity provider cannot be reached or does not answer in time
     */
    ToolResult call(ToolConnector connector, String tool, Map<String, Object> arguments);
}
