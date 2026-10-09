package com.vyoog.eisplatform.modules.toolsync;

import com.sun.net.httpserver.HttpServer;
import com.vyoog.eisplatform.modules.toolsync.config.PlatformMcpServerConfig;
import com.vyoog.eisplatform.modules.toolsync.config.ToolSyncProperties;
import com.vyoog.eisplatform.modules.toolsync.gateway.McpToolGateway;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolResult;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolUnavailableException;
import com.vyoog.eisplatform.modules.toolsync.mcp.ToolMcpResultConverter;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The platform calling a tool for real, over MCP: a token from a (fake) Keycloak with client credentials, the call with a bearer header,
 * the envelope as the {@code envelope} argument and the tool's result object read back. The tool is a small MCP server on embedded
 * Tomcat that behaves like the Macro Planner's (a tool taking an {@code envelope}). TC-INT-052.
 */
class McpToolGatewayTest {

    /** A tool of the kind the platform calls: takes the envelope, answers a result object. */
    public static class FakeToolTools {
        final List<Map<String, Object>> envelopes = new ArrayList<>();

        @Tool(name = "upsert_organization", resultConverter = ToolMcpResultConverter.class, description = "test")
        public ToolResult upsertOrganization(@ToolParam(description = "envelope") Map<String, Object> envelope) {
            envelopes.add(envelope);
            return "boom".equals(envelope.get("eventType")) ? ToolResult.retry("INTERNAL", "db busy") : ToolResult.applied(((Number) envelope.get("version")).longValue());
        }
    }

    final FakeToolTools tools = new FakeToolTools();
    final AtomicInteger tokenRequests = new AtomicInteger();
    final List<String> authorization = new ArrayList<>();
    volatile int tokenStatus = 200;
    HttpServer keycloak;
    Tomcat tomcat;
    McpSyncServer server;
    McpToolGateway gateway;
    ToolConnector connector;

    @BeforeEach
    void start() throws Exception {
        keycloak = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        keycloak.createContext("/token", ex -> {
            tokenRequests.incrementAndGet();
            String form = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] body = (tokenStatus == 200 && form.contains("grant_type=client_credentials") && form.contains("client_id=eis-sync") && form.contains("client_secret=s3cret")
                ? "{\"access_token\":\"tok-" + tokenRequests.get() + "\",\"expires_in\":300}" : "{\"error\":\"invalid_client\"}").getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(tokenStatus, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        keycloak.start();

        HttpServletStreamableServerTransportProvider transport = new PlatformMcpServerConfig().platformMcpTransport();
        server = McpServer.sync(transport).serverInfo("fake-tool", "1.0").capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
            .immediateExecution(true).tools(PlatformMcpServerConfig.toolSpecifications(tools)).build();
        tomcat = new Tomcat();
        tomcat.setPort(0);
        tomcat.setBaseDir(Files.createTempDirectory("gateway-tomcat").toString());
        Context ctx = tomcat.addContext("/api", null);
        Tomcat.addServlet(ctx, "auth", new jakarta.servlet.http.HttpServlet() {
            @Override
            protected void service(HttpServletRequest req, HttpServletResponse res) throws java.io.IOException, jakarta.servlet.ServletException {
                authorization.add(String.valueOf(req.getHeader("Authorization")));
                if (!String.valueOf(req.getHeader("Authorization")).startsWith("Bearer tok-")) {
                    res.setStatus(401);
                    return;
                }
                transport.service(req, res);
            }
        }).setAsyncSupported(true);
        ctx.addServletMappingDecoded("/mcp", "auth");
        tomcat.getConnector();
        tomcat.start();

        ToolSyncProperties props = new ToolSyncProperties();
        ReflectionTestUtils.setField(props, "clientId", "eis-sync");
        ReflectionTestUtils.setField(props, "clientSecret", "s3cret");
        ReflectionTestUtils.setField(props, "tokenUrl", "http://localhost:" + keycloak.getAddress().getPort() + "/token");
        ReflectionTestUtils.setField(props, "timeoutSeconds", 10);
        gateway = new McpToolGateway(props);
        connector = new ToolConnector();
        connector.setProductCode("thittam");
        connector.setBaseMcpUrl("http://localhost:" + tomcat.getConnector().getLocalPort() + "/api/mcp");
    }

    @AfterEach
    void stop() throws Exception {
        server.close();
        tomcat.stop();
        tomcat.destroy();
        keycloak.stop(0);
    }

    @Test
    void theEnvelopeGoesAsTheEnvelopeArgumentWithABearerTokenAndTheResultObjectComesBack() {
        Map<String, Object> envelope = Map.of("contractVersion", "1", "eventType", "OrganizationUpserted", "version", 7, "tenantRef", "100");

        ToolResult result = gateway.call(connector, "upsert_organization", Map.of("envelope", envelope));

        assertThat(result.status()).isEqualTo("applied");
        assertThat(result.version()).isEqualTo(7L);
        assertThat(tools.envelopes).hasSize(1);
        assertThat(tools.envelopes.get(0)).containsEntry("tenantRef", "100");
        assertThat(authorization).isNotEmpty().allSatisfy(h -> assertThat(h).startsWith("Bearer tok-"));
    }

    @Test
    void theTokenIsFetchedOnceAndReused() {
        Map<String, Object> envelope = Map.of("eventType", "OrganizationUpserted", "version", 1);

        gateway.call(connector, "upsert_organization", Map.of("envelope", envelope));
        gateway.call(connector, "upsert_organization", Map.of("envelope", envelope));

        assertThat(tokenRequests.get()).isEqualTo(1);
    }

    @Test
    void aRetryAnswerIsPassedOnSoTheDeliveryCanBackOff() {
        ToolResult result = gateway.call(connector, "upsert_organization", Map.of("envelope", Map.of("eventType", "boom", "version", 1)));

        assertThat(result.status()).isEqualTo("retry");
        assertThat(result.reason()).isEqualTo("INTERNAL");
    }

    @Test
    void aToolThatIsDownOrAnIdentityProviderThatRefusesIsUnavailableNotAnAnswer() throws Exception {
        tokenStatus = 401;
        assertThatThrownBy(() -> gateway.call(connector, "upsert_organization", Map.of("envelope", Map.of("version", 1))))
            .isInstanceOf(ToolUnavailableException.class).hasMessageContaining("refused a token");
        tokenStatus = 200;

        tomcat.stop();
        assertThatThrownBy(() -> gateway.call(connector, "upsert_organization", Map.of("envelope", Map.of("version", 1))))
            .isInstanceOf(ToolUnavailableException.class);
    }

    @Test
    void withoutAConnectionConfiguredNothingIsSent() {
        McpToolGateway unconfigured = new McpToolGateway(new ToolSyncProperties() {
            @Override
            public boolean gatewayConfigured() {
                return false;
            }
        });

        assertThatThrownBy(() -> unconfigured.call(connector, "upsert_organization", Map.of()))
            .isInstanceOf(ToolUnavailableException.class).hasMessageContaining("not configured");
    }
}
