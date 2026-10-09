package com.vyoog.eisplatform.modules.toolsync.mcp;

import com.vyoog.eisplatform.modules.toolsync.gateway.ToolResult;
import com.vyoog.eisplatform.modules.toolsync.service.ToolInboundService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The tools a tool calls on the platform (contract v1 section 5), served by the MCP server at {@code /api/mcp}. MCP is only the
 * transport: every call goes to {@link ToolInboundService}, which checks who is calling, applies the platform's own rules and answers
 * with a result object. A tool method never throws to the caller. This class only reads the request; it touches no repository.
 */
@Component
public class PlatformMcpTools {

    private static final String KEY = "A UUID chosen by the caller for this call; a repeat with the same key is answered with the first result.";
    private static final String TENANT = "The platform organization id of the tenant this call is about.";

    private final ToolInboundService inbound;
    private final CallerIdentity caller;

    public PlatformMcpTools(ToolInboundService inbound, CallerIdentity caller) {
        this.inbound = inbound;
        this.caller = caller;
    }

    @Tool(name = "report_user_created", resultConverter = ToolMcpResultConverter.class,
          description = "Tool → platform: a person registered inside the tool. The platform links or creates the person and the membership by "
              + "Keycloak id (never by an unverified e-mail) and answers with their snapshots. The person gets no product access.")
    public ToolResult reportUserCreated(
            @ToolParam(description = KEY) String idempotencyKey,
            @ToolParam(description = TENANT) String tenantRef,
            @ToolParam(description = "The person's Keycloak user id.") String sub,
            @ToolParam(description = "E-mail address.") String email,
            @ToolParam(description = "Whether the e-mail address has been verified.") Boolean emailVerified,
            @ToolParam(description = "First name.") String firstName,
            @ToolParam(description = "Last name.") String lastName,
            @ToolParam(required = false, description = "Phone number.") String phone,
            @ToolParam(required = false, description = "The platform hierarchy node to place the person on.") String nodeId) {
        return inbound.reportUserCreated(caller.clientId(), args("idempotencyKey", idempotencyKey, "tenantRef", tenantRef, "sub", sub,
            "email", email, "emailVerified", emailVerified, "firstName", firstName, "lastName", lastName, "phone", phone, "nodeId", nodeId));
    }

    @Tool(name = "update_user_profile", resultConverter = ToolMcpResultConverter.class,
          description = "Tool → platform: change a member's profile (firstName, lastName, phone, attributes). A lower expectedVersion than the current one is rejected with STALE_VERSION and the current snapshot.")
    public ToolResult updateUserProfile(
            @ToolParam(description = KEY) String idempotencyKey,
            @ToolParam(description = TENANT) String tenantRef,
            @ToolParam(description = "The person's Keycloak user id.") String sub,
            @ToolParam(description = "The fields to change: any of firstName, lastName, phone, attributes.") Map<String, Object> changes,
            @ToolParam(required = false, description = "The version of the person the caller last saw.") Long expectedVersion) {
        return inbound.updateUserProfile(caller.clientId(), args("idempotencyKey", idempotencyKey, "tenantRef", tenantRef, "sub", sub,
            "changes", changes, "expectedVersion", expectedVersion));
    }

    @Tool(name = "create_org_node", resultConverter = ToolMcpResultConverter.class,
          description = "Tool → platform: create a hierarchy node under a parent, with the platform's hierarchy rules (level order, unique sibling names).")
    public ToolResult createOrgNode(
            @ToolParam(description = KEY) String idempotencyKey,
            @ToolParam(description = TENANT) String tenantRef,
            @ToolParam(description = "The platform id of the parent node.") String parentId,
            @ToolParam(description = "Node name (at most 150 characters).") String name,
            @ToolParam(description = "Level type, for example DEPARTMENT.") String type,
            @ToolParam(required = false, description = "Code.") String code,
            @ToolParam(required = false, description = "Description.") String description,
            @ToolParam(required = false, description = "Sort order.") Integer sortOrder) {
        return inbound.createOrgNode(caller.clientId(), args("idempotencyKey", idempotencyKey, "tenantRef", tenantRef, "parentId", parentId,
            "name", name, "type", type, "code", code, "description", description, "sortOrder", sortOrder));
    }

    @Tool(name = "update_org_node", resultConverter = ToolMcpResultConverter.class,
          description = "Tool → platform: change a hierarchy node's name, type, code, description, sort order or active flag.")
    public ToolResult updateOrgNode(
            @ToolParam(description = KEY) String idempotencyKey,
            @ToolParam(description = TENANT) String tenantRef,
            @ToolParam(description = "The platform id of the node.") String id,
            @ToolParam(description = "Node name.") String name,
            @ToolParam(required = false, description = "Level type.") String type,
            @ToolParam(required = false, description = "Code.") String code,
            @ToolParam(required = false, description = "Description.") String description,
            @ToolParam(required = false, description = "Sort order.") Integer sortOrder,
            @ToolParam(required = false, description = "Active flag.") Boolean active,
            @ToolParam(required = false, description = "Confirm deactivating a node that has children or members.") Boolean force,
            @ToolParam(required = false, description = "The version of the node the caller last saw.") Long expectedVersion) {
        return inbound.updateOrgNode(caller.clientId(), args("idempotencyKey", idempotencyKey, "tenantRef", tenantRef, "id", id, "name", name,
            "type", type, "code", code, "description", description, "sortOrder", sortOrder, "active", active, "force", force,
            "expectedVersion", expectedVersion));
    }

    @Tool(name = "move_org_node", resultConverter = ToolMcpResultConverter.class,
          description = "Tool → platform: move a node under another parent; a move that would make a cycle is rejected with CYCLE.")
    public ToolResult moveOrgNode(
            @ToolParam(description = KEY) String idempotencyKey,
            @ToolParam(description = TENANT) String tenantRef,
            @ToolParam(description = "The platform id of the node.") String id,
            @ToolParam(description = "The platform id of the new parent.") String newParentId,
            @ToolParam(required = false, description = "The version of the node the caller last saw.") Long expectedVersion) {
        return inbound.moveOrgNode(caller.clientId(), args("idempotencyKey", idempotencyKey, "tenantRef", tenantRef, "id", id,
            "newParentId", newParentId, "expectedVersion", expectedVersion));
    }

    @Tool(name = "delete_org_node", resultConverter = ToolMcpResultConverter.class,
          description = "Tool → platform: delete a node; rejected with NODE_IN_USE while it has children or members.")
    public ToolResult deleteOrgNode(
            @ToolParam(description = KEY) String idempotencyKey,
            @ToolParam(description = TENANT) String tenantRef,
            @ToolParam(description = "The platform id of the node.") String id,
            @ToolParam(required = false, description = "The version of the node the caller last saw.") Long expectedVersion) {
        return inbound.deleteOrgNode(caller.clientId(), args("idempotencyKey", idempotencyKey, "tenantRef", tenantRef, "id", id,
            "expectedVersion", expectedVersion));
    }

    @Tool(name = "get_entitlement", resultConverter = ToolMcpResultConverter.class,
          description = "Tool → platform: the authoritative answer to \"may this person use this product now\": data {allowed, reason, endsAt, productRole}. "
              + "Reasons: OK, NOT_A_MEMBER, USER_INACTIVE, ORG_INACTIVE, NO_SUBSCRIPTION, SUBSCRIPTION_ENDED, NO_PRODUCT_ACCESS, NOT_PROVISIONED.")
    public ToolResult getEntitlement(
            @ToolParam(description = TENANT) String tenantRef,
            @ToolParam(description = "The person's Keycloak user id.") String sub,
            @ToolParam(description = "The product code of the calling tool.") String productCode,
            @ToolParam(required = false, description = "The action being attempted (informational).") String action) {
        return inbound.getEntitlement(caller.clientId(), args("tenantRef", tenantRef, "sub", sub, "productCode", productCode, "action", action));
    }

    @Tool(name = "report_provisioning_result", resultConverter = ToolMcpResultConverter.class,
          description = "Tool → platform: how provisioning of the tenant went. READY lets the platform send the organization's data; FAILED records the reason.")
    public ToolResult reportProvisioningResult(
            @ToolParam(description = TENANT) String tenantRef,
            @ToolParam(description = "The product code of the calling tool.") String productCode,
            @ToolParam(description = "READY or FAILED.") String status,
            @ToolParam(required = false, description = "The schema version the tenant reached.") String schemaVersion,
            @ToolParam(required = false, description = "Why it failed (no personal data).") String error,
            @ToolParam(required = false, description = KEY) String idempotencyKey) {
        return inbound.reportProvisioningResult(caller.clientId(), args("tenantRef", tenantRef, "productCode", productCode, "status", status,
            "schemaVersion", schemaVersion, "error", error, "idempotencyKey", idempotencyKey));
    }

    /** Name/value pairs to a map, leaving out what the caller did not send. */
    private static Map<String, Object> args(Object... pairs) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            if (pairs[i + 1] != null) {
                m.put((String) pairs[i], pairs[i + 1]);
            }
        }
        return m;
    }
}
