package com.vyoog.eisplatform.modules.toolsync.service.e2e;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Just enough Keycloak for the end-to-end run: the realm's signing keys (JWKS), the client-credentials token endpoint for the
 * synchronization service clients, tokens for people, and a permissive admin REST API (the Macro Planner makes best-effort role
 * calls there; every GET answers an empty list, every change 204).
 */
public class FakeKeycloak implements AutoCloseable {

    public static final String REALM = "eVyoog";

    private final HttpServer server;
    private final RSAKey key;
    private final Map<String, String> clientSecrets = new HashMap<>();
    private final Map<String, List<String>> clientAudiences = new HashMap<>();

    public FakeKeycloak() {
        try {
            key = new RSAKeyGenerator(2048).keyID("e2e-key").generate();
            server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        String base = "/realms/" + REALM + "/protocol/openid-connect";
        server.createContext(base + "/certs", ex -> {
            byte[] body = new JWKSet(key.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.createContext(base + "/token", ex -> {
            String form = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Map<String, String> p = new HashMap<>();
            for (String pair : form.split("&")) {
                int i = pair.indexOf('=');
                if (i > 0) {
                    p.put(URLDecoder.decode(pair.substring(0, i), StandardCharsets.UTF_8), URLDecoder.decode(pair.substring(i + 1), StandardCharsets.UTF_8));
                }
            }
            String client = p.get("client_id");
            boolean ok = "client_credentials".equals(p.get("grant_type")) && client != null && p.get("client_secret") != null
                && p.get("client_secret").equals(clientSecrets.get(client));
            byte[] body = (ok ? "{\"access_token\":\"" + token(UUID.nameUUIDFromBytes(("service-account-" + client).getBytes(StandardCharsets.UTF_8)).toString(), client, clientAudiences.getOrDefault(client, List.of()), Map.of())
                + "\",\"expires_in\":300,\"token_type\":\"Bearer\"}" : "{\"error\":\"invalid_client\"}").getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(ok ? 200 : 401, body.length);
            ex.getResponseBody().write(body);
            ex.close();
        });
        server.createContext("/admin", ex -> {
            boolean get = "GET".equals(ex.getRequestMethod());
            byte[] body = get ? "[]".getBytes(StandardCharsets.UTF_8) : new byte[0];
            ex.getRequestBody().readAllBytes();
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(get ? 200 : 204, get ? body.length : -1);
            if (get) {
                ex.getResponseBody().write(body);
            }
            ex.close();
        });
        server.start();
    }

    /** A service client that may fetch tokens, and the audiences its tokens carry (the audience mapper of the Keycloak guide). */
    public FakeKeycloak client(String clientId, String secret, String... audiences) {
        clientSecrets.put(clientId, secret);
        clientAudiences.put(clientId, List.of(audiences));
        return this;
    }

    public String url() {
        return "http://localhost:" + server.getAddress().getPort();
    }

    public String issuer() {
        return url() + "/realms/" + REALM;
    }

    public String jwksUrl() {
        return issuer() + "/protocol/openid-connect/certs";
    }

    public String tokenUrl() {
        return issuer() + "/protocol/openid-connect/token";
    }

    /** A signed access token. */
    public String token(String sub, String azp, List<String> audiences, Map<String, Object> extra) {
        try {
            JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder().issuer(issuer()).subject(sub).audience(audiences).claim("azp", azp)
                .issueTime(new Date()).expirationTime(new Date(System.currentTimeMillis() + 3_600_000)).jwtID(UUID.randomUUID().toString());
            extra.forEach(claims::claim);
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claims.build());
            jwt.sign(new RSASSASigner(key));
            return jwt.serialize();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /** A person's token for the Macro Planner's client with the given Keycloak client roles. */
    public String personToken(String sub, String username, String clientId, String... roles) {
        return token(sub, clientId, List.of(clientId), Map.of("preferred_username", username, "email", username + "@e2e.example",
            "resource_access", Map.of(clientId, Map.of("roles", List.of(roles)))));
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
