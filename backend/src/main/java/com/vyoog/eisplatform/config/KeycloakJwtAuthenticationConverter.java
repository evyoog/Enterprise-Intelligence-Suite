package com.vyoog.eisplatform.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mirrors vyg-pms's SecurityConfig exactly: Keycloak roles here are CLIENT roles
 * (JWT claim "resource_access.<clientId>.roles"), not realm roles
 * ("realm_access.roles") — confirmed via Keycloak's admin API, the "eVyoog" client
 * has its own ADMIN/PROJECT_OWNER/ACTIVITY_USER/TASK_USER client roles, shared with
 * vyg-pms and used the same way here.
 *
 * Which key to read out of "resource_access" isn't fixed, because it depends on
 * which client the token was issued to: "aud" only lists it if the client has an
 * Audience mapper configured, otherwise it only appears in "azp" (the client that
 * requested the token). Checking both, like vyg-pms does, means this doesn't
 * silently stop working if that mapper configuration ever changes.
 */
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, extractClientRoles(jwt));
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractClientRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess == null) {
            return List.of();
        }

        Set<String> clientsToCheck = new HashSet<>();
        List<String> audiences = jwt.getClaim("aud");
        if (audiences != null) {
            clientsToCheck.addAll(audiences);
        }
        String azp = jwt.getClaim("azp");
        if (azp != null) {
            clientsToCheck.add(azp);
        }

        List<String> roles = new ArrayList<>();
        for (String client : clientsToCheck) {
            if (!resourceAccess.containsKey(client)) {
                continue;
            }
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(client);
            List<String> clientRoles = (List<String>) clientAccess.get("roles");
            if (clientRoles != null) {
                roles.addAll(clientRoles);
            }
        }

        return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(Collectors.toList());
    }
}
