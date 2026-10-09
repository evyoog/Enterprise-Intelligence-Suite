package com.vyoog.eisplatform.modules.toolsync.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.integration.service.PlatformEvent;
import com.vyoog.eisplatform.modules.integration.service.PlatformEventHandler;
import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolConnectorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The first step of delivery: a platform event of the synchronization (see {@link ToolSyncPublisher}) becomes one
 * {@code tool_delivery} row per tool and organization that must hear about it. It only creates rows (fast, no network), so a slow or
 * stopped tool never delays the outbox dispatcher or another tool: each row is sent, retried and failed on its own
 * ({@link ToolDeliveryService}).
 *
 * <p>Which tools: all active connectors for an organization-level change; the one named in the payload for a subscription; the one
 * in the aggregate id for product access. A tool whose tenant is not READY gets nothing yet: its start is a full resync after provisioning.
 * An organization with an ACTIVE subscription to a tool's product and no tenant there gets a provisioning request instead.
 */
@Component
@RequiredArgsConstructor
public class ToolFanOutHandler implements PlatformEventHandler {

    static final Set<String> TYPES = Set.of(
        PlatformEventTypes.ORGANIZATION_UPSERTED, PlatformEventTypes.ORG_NODE_UPSERTED, PlatformEventTypes.ORG_NODE_DELETED,
        PlatformEventTypes.USER_UPSERTED, PlatformEventTypes.MEMBERSHIP_CHANGED, PlatformEventTypes.SUBSCRIPTION_SYNCED,
        PlatformEventTypes.USER_PRODUCT_ACCESS_GRANTED, PlatformEventTypes.USER_PRODUCT_ACCESS_REVOKED);

    private final ToolConnectorRepository connectors;
    private final ToolSyncService sync;
    private final ObjectMapper json;

    @Override
    public String name() {
        return "tool-fan-out";
    }

    @Override
    public boolean handles(String eventType) {
        return TYPES.contains(eventType);
    }

    @Override
    public void handle(PlatformEvent event) {
        Map<String, Object> payload = read(event.payload());
        List<Long> organizations = ToolSyncPublisher.organizationsOf(payload);
        Long version = payload.get("version") instanceof Number n ? n.longValue() : null;
        boolean subscriptionEvent = PlatformEventTypes.SUBSCRIPTION_SYNCED.equals(event.eventType());
        for (ToolConnector connector : connectors.findAll()) {       // a PAUSED tool collects its messages; they are sent when it is resumed
            if (!concerns(event, payload, connector)) {
                continue;
            }
            for (Long organizationId : organizations) {
                if (subscriptionEvent) {
                    sync.requestProvisioning(organizationId, connector);
                }
                if (sync.eligible(organizationId, connector, subscriptionEvent)) {
                    sync.enqueueForEvent(event.eventId(), connector, organizationId, event.eventType(), event.aggregateType(),
                        event.aggregateId(), version);
                }
            }
        }
    }

    /** A subscription event is for the tool of its product only; a product-access event for the tool whose code ends the aggregate id. */
    private static boolean concerns(PlatformEvent event, Map<String, Object> payload, ToolConnector connector) {
        if (PlatformEventTypes.SUBSCRIPTION_SYNCED.equals(event.eventType())) {
            return payload.get("productId") instanceof Number n && n.longValue() == connector.getProductId();
        }
        if (PlatformEventTypes.AGGREGATE_USER_ACCESS.equals(event.aggregateType())) {
            return event.aggregateId().endsWith(":" + connector.getProductCode());
        }
        return true;
    }

    private Map<String, Object> read(String payload) {
        try {
            return json.readValue(payload, new TypeReference<>() { });
        } catch (Exception e) {
            throw new IllegalArgumentException("The event payload is not JSON", e);
        }
    }
}
