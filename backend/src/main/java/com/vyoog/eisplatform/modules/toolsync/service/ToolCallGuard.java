package com.vyoog.eisplatform.modules.toolsync.service;

import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.toolsync.gateway.ToolResult;
import com.vyoog.eisplatform.modules.toolsync.model.ConnectorStatus;
import com.vyoog.eisplatform.modules.toolsync.model.TenantAppSchema;
import com.vyoog.eisplatform.modules.toolsync.model.TenantSchemaStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import com.vyoog.eisplatform.modules.toolsync.repository.TenantAppSchemaRepository;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolConnectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Who may call the platform's MCP tools, for which organization (contract v1 section 7):
 * <ol>
 *   <li>the token's {@code azp} must be the service client of an ACTIVE tool connector ({@code NOT_ALLOWED_CLIENT});</li>
 *   <li>{@code tenantRef} must be an organization of the platform ({@code UNKNOWN_TENANT});</li>
 *   <li>that tool must hold an ACTIVE subscription of the organization ({@code NO_ACTIVE_SUBSCRIPTION_FOR_TENANT});</li>
 *   <li>unless the call is the provisioning report, the tenant must be READY ({@code TENANT_NOT_READY}).</li>
 * </ol>
 * The caller is a system principal: the answer never depends on a person, and no user is created for it.
 */
@Service
@RequiredArgsConstructor
public class ToolCallGuard {

    /** An allowed call: which tool, for which organization. */
    public record Caller(ToolConnector connector, long organizationId) {
    }

    /** Either the caller, or the result to answer with. */
    public record Outcome(Caller caller, ToolResult rejection) {
        public boolean allowed() {
            return caller != null;
        }
    }

    private final ToolConnectorRepository connectors;
    private final TenantAppSchemaRepository tenants;
    private final OrganizationRepository organizations;
    private final ProductSubscriptionRepository subscriptions;

    /** For a call that changes something: a subscription and a READY tenant are required. */
    public Outcome check(String azp, String tenantRef, boolean requireReadyTenant) {
        return check(azp, tenantRef, true, requireReadyTenant);
    }

    /**
     * @param requireSubscription the tool must hold an ACTIVE subscription of the organization; false only for {@code get_entitlement},
     *                            which answers "no subscription" / "subscription ended" itself instead of refusing the question
     */
    @Transactional(readOnly = true)
    public Outcome check(String azp, String tenantRef, boolean requireSubscription, boolean requireReadyTenant) {
        if (azp == null || azp.isBlank()) {
            return deny("NOT_ALLOWED_CLIENT", "The call carries no client identity.");
        }
        ToolConnector connector = connectors.findByClientIdAndStatus(azp, ConnectorStatus.ACTIVE).orElse(null);
        if (connector == null) {
            return deny("NOT_ALLOWED_CLIENT", "This client is not an active tool of the platform.");
        }
        long organizationId;
        try {
            organizationId = Long.parseLong(tenantRef == null ? "" : tenantRef.trim());
        } catch (NumberFormatException e) {
            return deny("UNKNOWN_TENANT", "tenantRef is not an organization of the platform.");
        }
        if (!organizations.existsById(organizationId)) {
            return deny("UNKNOWN_TENANT", "tenantRef is not an organization of the platform.");
        }
        boolean subscribed = subscriptions.findByOwnerOrganizationIdAndProductId(organizationId, connector.getProductId())
            .map(s -> s.getStatus() == SubscriptionStatus.ACTIVE).orElse(false);
        if (requireSubscription && !subscribed) {
            return deny("NO_ACTIVE_SUBSCRIPTION_FOR_TENANT", "The organization has no active subscription to this tool.");
        }
        if (requireReadyTenant) {
            boolean ready = tenants.findByOrganizationIdAndProductId(organizationId, connector.getProductId())
                .map(TenantAppSchema::getStatus).orElse(null) == TenantSchemaStatus.READY;
            if (!ready) {
                return deny("TENANT_NOT_READY", "The tenant of this organization is not ready in the tool.");
            }
        }
        return new Outcome(new Caller(connector, organizationId), null);
    }

    private static Outcome deny(String reason, String message) {
        return new Outcome(null, ToolResult.rejected(reason, message));
    }
}
