package com.vyoog.eisplatform.modules.toolsync.gateway;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.toolsync.config.ToolSyncProperties;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

/**
 * Calls a tool's MCP server over Streamable HTTP with a token from Keycloak (client credentials, the platform's service client
 * {@code eis-sync}). The token is cached until shortly before it expires and dropped when a tool answers 401. A call is bounded by
 * {@code app.sync.timeout-seconds}. A tool's answer is always a result object; anything else is treated as "retry" so nothing is guessed.
 */
@Component
public class McpToolGateway implements ToolGateway {

    private static final ObjectMapper JSON = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final ToolSyncProperties props;
    private final HttpClient http;

    private String token;
    private Instant tokenExpires = Instant.EPOCH;

    public McpToolGateway(ToolSyncProperties props) {
        this.props = props;
        this.http = HttpClient.newBuilder().connectTimeout(timeout()).build();
    }

    @Override
    public ToolResult call(ToolConnector connector, String tool, Map<String, Object> arguments) {
        if (!props.gatewayConfigured()) {
            throw new ToolUnavailableException("the platform's connection to tools is not configured (app.sync.*)");
        }
        URI uri = URI.create(connector.getBaseMcpUrl().trim());
        String origin = uri.getScheme() + "://" + uri.getRawAuthority();
        String bearer = "Bearer " + token();
        Duration timeout = timeout();
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(origin)
            .endpoint(uri.getRawPath())
            .clientBuilder(HttpClient.newBuilder().connectTimeout(timeout))
            .customizeRequest(r -> r.header("Authorization", bearer))
            .build();
        try (McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(timeout)
                .initializationTimeout(timeout)
                .clientInfo(new McpSchema.Implementation("eis-platform", "1"))
                .build()) {
            try {
                client.initialize();
            } catch (RuntimeException e) {
                if (String.valueOf(e.getMessage()).contains("401")) {
                    forgetToken();
                }
                throw new ToolUnavailableException("the tool did not accept the connection", e);
            }
            McpSchema.CallToolResult answer;
            try {
                answer = client.callTool(new McpSchema.CallToolRequest(tool, arguments));
            } catch (RuntimeException e) {
                throw new ToolUnavailableException("the tool did not answer " + tool, e);
            }
            String text = answer.content().isEmpty() || !(answer.content().get(0) instanceof McpSchema.TextContent t) ? "" : t.text();
            return parse(text, Boolean.TRUE.equals(answer.isError()));
        }
    }

    static ToolResult parse(String text, boolean isError) {
        try {
            JsonNode node = JSON.readTree(text);
            if (node != null && node.hasNonNull("status")) {
                return JSON.treeToValue(node, ToolResult.class);
            }
        } catch (IOException ignored) {
            // falls through
        }
        return ToolResult.retry("INTERNAL", isError ? "The tool reported an error." : "The tool's answer was not understood.");
    }

    private synchronized void forgetToken() {
        token = null;
        tokenExpires = Instant.EPOCH;
    }

    private synchronized String token() {
        if (token != null && Instant.now().isBefore(tokenExpires)) {
            return token;
        }
        String form = "grant_type=client_credentials&client_id=" + URLEncoder.encode(props.getClientId(), StandardCharsets.UTF_8)
            + "&client_secret=" + URLEncoder.encode(props.getClientSecret(), StandardCharsets.UTF_8);
        try {
            HttpResponse<String> res = http.send(HttpRequest.newBuilder(URI.create(props.getTokenUrl().trim())).timeout(timeout())
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(form)).build(), HttpResponse.BodyHandlers.ofString());
            JsonNode body = JSON.readTree(res.body());
            if (res.statusCode() != 200 || !body.hasNonNull("access_token")) {
                throw new ToolUnavailableException("the identity provider refused a token (HTTP " + res.statusCode() + ")");
            }
            token = body.get("access_token").asText();
            tokenExpires = Instant.now().plusSeconds(Math.max(0, body.path("expires_in").asLong(60) - 30));
            return token;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ToolUnavailableException("interrupted while getting a token", e);
        } catch (IOException e) {
            throw new ToolUnavailableException("the identity provider could not be reached", e);
        }
    }

    private Duration timeout() {
        return Duration.ofSeconds(Math.max(1, props.getTimeoutSeconds()));
    }
}
