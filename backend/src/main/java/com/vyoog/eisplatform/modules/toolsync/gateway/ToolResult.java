package com.vyoog.eisplatform.modules.toolsync.gateway;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * The result object every tool answers with (contract v1 section 6).
 *
 * @param status  applied, duplicate, rejected or retry
 * @param version the version now held for the aggregate, when known
 * @param reason  machine-readable code for rejected and retry
 * @param message short text without personal data
 * @param data    extra information (the schema version of a provisioned tenant, a digest, a list of versions)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ToolResult(String status, Long version, String reason, String message, Map<String, Object> data) {

    public static final String APPLIED = "applied";
    public static final String DUPLICATE = "duplicate";
    public static final String REJECTED = "rejected";
    public static final String RETRY = "retry";

    public static ToolResult applied(Long version) {
        return new ToolResult(APPLIED, version, null, null, null);
    }

    public static ToolResult duplicate(Long version) {
        return new ToolResult(DUPLICATE, version, null, null, null);
    }

    public static ToolResult rejected(String reason, String message) {
        return new ToolResult(REJECTED, null, reason, message, null);
    }

    public static ToolResult retry(String reason, String message) {
        return new ToolResult(RETRY, null, reason, message, null);
    }

    public ToolResult withData(Map<String, Object> data) {
        return new ToolResult(status, version, reason, message, data);
    }

    /** Applied or duplicate: the tool holds the state; nothing more to do. */
    public boolean succeeded() {
        return APPLIED.equals(status) || DUPLICATE.equals(status);
    }

    public boolean rejectedForGood() {
        return REJECTED.equals(status);
    }
}
