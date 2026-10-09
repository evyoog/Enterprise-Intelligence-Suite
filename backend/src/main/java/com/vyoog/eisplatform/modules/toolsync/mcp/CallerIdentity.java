package com.vyoog.eisplatform.modules.toolsync.mcp;

/** Who is calling the platform's MCP server: the {@code azp} (authorized party) of the validated bearer token, which is the caller's Keycloak service client. */
public interface CallerIdentity {

    /** The {@code azp} claim, or null when the request carries no validated token. */
    String clientId();
}
