package com.vyoog.eisplatform.modules.integration.service;

import com.vyoog.eisplatform.modules.integration.dto.ApiKeyDto;
import com.vyoog.eisplatform.modules.integration.dto.CreateApiKeyRequest;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * REQ-INT-001.1 (C92, API-1 B): the permission check in front of {@link ApiKeyService}. Organization administrators
 * (those holding <em>Manage organization</em>) list, create and revoke the keys of their own organization.
 */
@Service
@RequiredArgsConstructor
public class OrganizationApiKeyService {

    static final String PERMISSION = "MANAGE_ORGANIZATION";

    private final ApiKeyService apiKeyService;
    private final OrganizationSelfService organizationSelfService;
    private final CurrentCustomerResolver currentCustomerResolver;

    public List<ApiKeyDto> list(Jwt jwt) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(customer(jwt).getId(), PERMISSION);
        return apiKeyService.listForOrganization(organizationId);
    }

    public ApiKeyDto create(Jwt jwt, Collection<String> authorities, CreateApiKeyRequest request) {
        Customer creator = customer(jwt);
        Long organizationId = organizationSelfService.requireOrganizationPermission(creator.getId(), PERMISSION);
        return apiKeyService.createForOrganization(creator, jwt.getSubject(), authorities, organizationId, request);
    }

    public ApiKeyDto revoke(Jwt jwt, Long keyId) {
        Customer actor = customer(jwt);
        Long organizationId = organizationSelfService.requireOrganizationPermission(actor.getId(), PERMISSION);
        return apiKeyService.revokeForOrganization(organizationId, actor.getId(), jwt.getSubject(), keyId);
    }

    private Customer customer(Jwt jwt) {
        return currentCustomerResolver.resolve(jwt);
    }
}
