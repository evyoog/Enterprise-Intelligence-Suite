package com.vyoog.eisplatform.modules.toolsync.config;

import com.vyoog.eisplatform.modules.toolsync.mcp.PlatformMcpTools;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.ai.mcp.McpToolUtils;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * The platform's MCP server at {@code /api/mcp} (Streamable HTTP, JSON-RPC 2.0), for the tools it is connected to (contract v1).
 * Wired by hand because the Spring AI MCP Boot starter needs Spring Boot 3.4+. The path is a plain servlet, so the security filter
 * chain (bearer token) runs on it like on any other request; {@code immediateExecution(true)} runs tools on the request thread,
 * where the SecurityContext lives. The tools themselves check the caller ({@code ToolCallGuard}).
 */
@Configuration
public class PlatformMcpServerConfig {

    static final String ENDPOINT = "/mcp";

    @Bean
    public HttpServletStreamableServerTransportProvider platformMcpTransport() {
        return HttpServletStreamableServerTransportProvider.builder().mcpEndpoint(ENDPOINT).build();
    }

    @Bean
    public ServletRegistrationBean<HttpServletStreamableServerTransportProvider> platformMcpServlet(HttpServletStreamableServerTransportProvider transport) {
        ServletRegistrationBean<HttpServletStreamableServerTransportProvider> bean = new ServletRegistrationBean<>(transport, ENDPOINT);
        bean.setName("platformMcp");
        bean.setAsyncSupported(true);
        return bean;
    }

    @Bean(destroyMethod = "closeGracefully")
    public McpSyncServer platformMcpServer(HttpServletStreamableServerTransportProvider transport, PlatformMcpTools tools) {
        return McpServer.sync(transport)
            .serverInfo("eis-platform", "1.0")
            .instructions("eVyoog platform: the tools a connected product calls to report people, change the organization hierarchy, ask for an entitlement and report provisioning.")
            .capabilities(McpSchema.ServerCapabilities.builder().tools(true).build())
            .immediateExecution(true)
            .tools(toolSpecifications(tools))
            .build();
    }

    public static List<McpServerFeatures.SyncToolSpecification> toolSpecifications(Object... toolBeans) {
        return McpToolUtils.toSyncToolSpecification(List.of(MethodToolCallbackProvider.builder().toolObjects(toolBeans).build().getToolCallbacks()));
    }
}
