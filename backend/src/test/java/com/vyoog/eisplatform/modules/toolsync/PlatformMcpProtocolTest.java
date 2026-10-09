package com.vyoog.eisplatform.modules.toolsync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.toolsync.config.PlatformMcpServerConfig;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolResult;
import com.vyoog.eisplatform.modules.toolsync.mcp.CallerIdentity;
import com.vyoog.eisplatform.modules.toolsync.mcp.PlatformMcpTools;
import com.vyoog.eisplatform.modules.toolsync.service.ToolInboundService;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.nio.file.Files;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Speaks real MCP to the platform's tools on an embedded Tomcat (no Spring context, no database): the eight tools of contract v1
 * section 5 are listed, a call reaches {@link ToolInboundService} with the caller's client id and its arguments, and the result object
 * (applied / rejected / retry) comes back as the tool's content, never as a protocol error. TC-INT-056.
 */
class PlatformMcpProtocolTest {

    final ToolInboundService inbound = mock(ToolInboundService.class);
    final CallerIdentity identity = () -> "thittam-sync";
    final ObjectMapper json = new ObjectMapper();

    Tomcat tomcat;
    McpSyncServer server;
    McpSyncClient client;

    @BeforeEach
    void start() throws Exception {
        HttpServletStreamableServerTransportProvider transport = new PlatformMcpServerConfig().platformMcpTransport();
        server = McpServer.sync(transport).serverInfo("eis-platform", "1.0")
            .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
            .immediateExecution(true)
            .tools(PlatformMcpServerConfig.toolSpecifications(new PlatformMcpTools(inbound, identity))).build();

        tomcat = new Tomcat();
        tomcat.setPort(0);
        tomcat.setBaseDir(Files.createTempDirectory("eis-mcp-tomcat").toString());
        Context ctx = tomcat.addContext("/api", null);
        Tomcat.addServlet(ctx, "mcp", transport).setAsyncSupported(true);
        ctx.addServletMappingDecoded("/mcp", "mcp");
        tomcat.getConnector();
        tomcat.start();

        client = McpClient.sync(HttpClientStreamableHttpTransport.builder("http://localhost:" + tomcat.getConnector().getLocalPort())
                .endpoint("/api/mcp").build())
            .requestTimeout(Duration.ofSeconds(10))
            .clientInfo(new McpSchema.Implementation("thittam-sync", "1.0")).build();
        client.initialize();
    }

    @AfterEach
    void stop() throws Exception {
        client.close();
        server.close();
        tomcat.stop();
        tomcat.destroy();
    }

    @Test
    void theEightToolsOfTheContractAreListed() {
        assertThat(client.listTools().tools()).extracting(McpSchema.Tool::name).containsExactlyInAnyOrder(
            "report_user_created", "update_user_profile", "create_org_node", "update_org_node", "move_org_node", "delete_org_node",
            "get_entitlement", "report_provisioning_result");
    }

    @Test
    @SuppressWarnings("unchecked")
    void aCallReachesTheServiceWithTheCallersClientAndItsArgumentsAndTheResultComesBack() throws Exception {
        when(inbound.reportUserCreated(eq("thittam-sync"), any())).thenReturn(ToolResult.applied(1L));

        McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest("report_user_created", Map.of(
            "idempotencyKey", "k-1", "tenantRef", "100", "sub", "36401029-823c-4cce-994a-877b69b4255d", "email", "a@b.example",
            "emailVerified", true, "firstName", "A", "lastName", "B")));

        assertThat(result.isError()).isFalse();
        JsonNode out = json.readTree(((McpSchema.TextContent) result.content().get(0)).text());
        assertThat(out.get("status").asText()).isEqualTo("applied");
        assertThat(out.get("version").asLong()).isEqualTo(1);
        ArgumentCaptor<Map<String, Object>> seen = ArgumentCaptor.forClass(Map.class);
        verify(inbound).reportUserCreated(eq("thittam-sync"), seen.capture());
        assertThat(seen.getValue()).containsEntry("tenantRef", "100").containsEntry("idempotencyKey", "k-1")
            .containsEntry("emailVerified", true).doesNotContainKey("phone");
    }

    @Test
    void aRejectionAndAnEntitlementAnswerAreResultsNotToolErrors() throws Exception {
        when(inbound.deleteOrgNode(eq("thittam-sync"), any())).thenReturn(ToolResult.rejected("NODE_IN_USE", "has children"));
        when(inbound.getEntitlement(eq("thittam-sync"), any())).thenReturn(
            ToolResult.applied(null).withData(Map.of("allowed", true, "reason", "OK", "productRole", "PMS_USER")));

        McpSchema.CallToolResult rejected = client.callTool(new McpSchema.CallToolRequest("delete_org_node",
            Map.of("idempotencyKey", "k-2", "tenantRef", "100", "id", "5")));
        McpSchema.CallToolResult entitlement = client.callTool(new McpSchema.CallToolRequest("get_entitlement",
            Map.of("tenantRef", "100", "sub", "36401029-823c-4cce-994a-877b69b4255d", "productCode", "thittam")));

        assertThat(rejected.isError()).isFalse();
        JsonNode r = json.readTree(((McpSchema.TextContent) rejected.content().get(0)).text());
        assertThat(r.get("status").asText()).isEqualTo("rejected");
        assertThat(r.get("reason").asText()).isEqualTo("NODE_IN_USE");
        JsonNode e = json.readTree(((McpSchema.TextContent) entitlement.content().get(0)).text());
        assertThat(e.get("data").get("allowed").asBoolean()).isTrue();
        assertThat(e.get("data").get("productRole").asText()).isEqualTo("PMS_USER");
    }

    @Test
    void everyToolRoutesToItsOwnServiceMethod() {
        when(inbound.updateUserProfile(any(), any())).thenReturn(ToolResult.applied(2L));
        when(inbound.createOrgNode(any(), any())).thenReturn(ToolResult.applied(1L));
        when(inbound.updateOrgNode(any(), any())).thenReturn(ToolResult.applied(2L));
        when(inbound.moveOrgNode(any(), any())).thenReturn(ToolResult.applied(3L));
        when(inbound.reportProvisioningResult(any(), any())).thenReturn(ToolResult.applied(null));

        client.callTool(new McpSchema.CallToolRequest("update_user_profile", Map.of("idempotencyKey", "k", "tenantRef", "1", "sub", "s", "changes", Map.of("firstName", "X"))));
        client.callTool(new McpSchema.CallToolRequest("create_org_node", Map.of("idempotencyKey", "k", "tenantRef", "1", "parentId", "2", "name", "N", "type", "TEAM")));
        client.callTool(new McpSchema.CallToolRequest("update_org_node", Map.of("idempotencyKey", "k", "tenantRef", "1", "id", "2", "name", "N")));
        client.callTool(new McpSchema.CallToolRequest("move_org_node", Map.of("idempotencyKey", "k", "tenantRef", "1", "id", "2", "newParentId", "3")));
        client.callTool(new McpSchema.CallToolRequest("report_provisioning_result", Map.of("tenantRef", "1", "productCode", "thittam", "status", "READY")));

        verify(inbound).updateUserProfile(eq("thittam-sync"), any());
        verify(inbound).createOrgNode(eq("thittam-sync"), any());
        verify(inbound).updateOrgNode(eq("thittam-sync"), any());
        verify(inbound).moveOrgNode(eq("thittam-sync"), any());
        verify(inbound).reportProvisioningResult(eq("thittam-sync"), any());
    }
}
