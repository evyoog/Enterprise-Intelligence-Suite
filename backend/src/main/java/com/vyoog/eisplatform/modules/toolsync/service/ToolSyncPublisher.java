package com.vyoog.eisplatform.modules.toolsync.service;

import com.vyoog.eisplatform.modules.integration.service.OutboxService;
import com.vyoog.eisplatform.modules.integration.service.PlatformEventTypes;
import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNode;
import com.vyoog.eisplatform.modules.orghierarchy.repository.OrgNodeRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolConnectorRepository;
import com.vyoog.eisplatform.modules.toolsync.tracking.ToolSyncChangeRecorder.Change;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Turns the changes of one transaction into platform events ({@code OutboxService}), in that transaction, just before it commits
 * (called by {@code ToolSyncChangeRecorder}). The event names WHAT changed and for which organization(s); the content every tool
 * receives is built from the current state when it is sent ({@code ToolMessageBuilder}), so nothing is copied into the event and a
 * retry or replay never sends something old. Payloads carry ids, versions and organization ids only (BR-2).
 *
 * <p>Nothing is published while no tool is connected (no {@code tool_connector} row): the platform then behaves as it did before. A PAUSED tool
 * still gets its events turned into waiting deliveries, so nothing is lost while it is paused.
 * A change is skipped when no tool could use it: a person without a Keycloak id, a membership of such a person, an individual's
 * subscription, an access to a product that has no tool.
 */
@Service
@RequiredArgsConstructor
public class ToolSyncPublisher {

    private final OutboxService outbox;
    private final ToolConnectorRepository connectors;
    private final CustomerRepository customers;
    private final OrganizationMemberRepository members;
    private final OrganizationProductAccessRepository accesses;
    private final ProductSubscriptionRepository subscriptions;
    private final OrgNodeRepository nodes;

    @Transactional(propagation = Propagation.REQUIRED)
    public void publish(List<Change> changes) {
        if (changes.isEmpty() || connectors.count() == 0) {
            return;
        }
        Set<String> usersDone = new HashSet<>();
        for (Change c : changes) {
            switch (c.aggregateType()) {
                case "Organization" -> outbox.publish(PlatformEventTypes.ORGANIZATION_UPSERTED, PlatformEventTypes.AGGREGATE_ORGANIZATION,
                    c.id(), payload(c.version(), "organizationId", c.id()));
                case "OrgNode" -> node(c);
                case "Customer" -> customers.findById(c.id()).ifPresent(customer -> user(customer, c.version(), usersDone));
                case "Member" -> member(c, usersDone);
                case "Subscription" -> subscription(c);
                case "Access" -> access(c);
                default -> { }
            }
        }
    }

    private void node(Change c) {
        if (c.deleted()) {
            outbox.publish(PlatformEventTypes.ORG_NODE_DELETED, PlatformEventTypes.AGGREGATE_ORG_NODE, c.id(),
                payload(c.version(), "organizationId", c.organizationId()));
            return;
        }
        Optional<OrgNode> node = nodes.findById(c.id());
        node.ifPresent(n -> outbox.publish(PlatformEventTypes.ORG_NODE_UPSERTED, PlatformEventTypes.AGGREGATE_ORG_NODE, n.getId(),
            payload(c.version(), "organizationId", n.getOrganizationId())));
    }

    /** The person changed: every organization the person belongs to has tools that must hear it. */
    private void user(Customer customer, long version, Set<String> done) {
        if (customer.getKeycloakSub() == null || customer.getKeycloakSub().isBlank() || !done.add(customer.getKeycloakSub())) {
            return;
        }
        List<Long> organizations = members.findByCustomerId(customer.getId()).stream().map(OrganizationMember::getOrganizationId).distinct().toList();
        if (organizations.isEmpty()) {
            return;
        }
        outbox.publish(PlatformEventTypes.USER_UPSERTED, PlatformEventTypes.AGGREGATE_USER, customer.getKeycloakSub(),
            payload(version, "organizationIds", organizations));
    }

    private void member(Change c, Set<String> usersDone) {
        if (c.deleted()) {
            return;                  // memberships are deactivated, not deleted; nothing a tool could be told
        }
        Optional<OrganizationMember> found = members.findById(c.id());
        if (found.isEmpty()) {
            return;
        }
        OrganizationMember m = found.get();
        Optional<Customer> customer = customers.findById(m.getCustomerId());
        if (customer.isEmpty() || customer.get().getKeycloakSub() == null || customer.get().getKeycloakSub().isBlank()) {
            return;
        }
        user(customer.get(), customer.get().getSyncVersion(), usersDone);       // the tool needs the person before the membership
        outbox.publish(PlatformEventTypes.MEMBERSHIP_CHANGED, PlatformEventTypes.AGGREGATE_MEMBERSHIP, customer.get().getKeycloakSub(),
            payload(c.version(), "organizationId", m.getOrganizationId()));
    }

    private void subscription(Change c) {
        Optional<ProductSubscription> found = subscriptions.findById(c.id());
        if (found.isEmpty() || found.get().getOwnerType() != RegistrationOwnerType.ORGANIZATION || found.get().getOwnerOrganizationId() == null) {
            return;
        }
        ProductSubscription s = found.get();
        Map<String, Object> payload = payload(c.version(), "organizationId", s.getOwnerOrganizationId());
        payload.put("productId", s.getProductId());
        outbox.publish(PlatformEventTypes.SUBSCRIPTION_SYNCED, PlatformEventTypes.AGGREGATE_SUBSCRIPTION, s.getId(), payload);
    }

    private void access(Change c) {
        if (c.deleted()) {
            return;
        }
        Optional<OrganizationProductAccess> found = accesses.findById(c.id());
        if (found.isEmpty()) {
            return;
        }
        OrganizationProductAccess a = found.get();
        Optional<ToolConnector> connector = connectors.findByProductId(a.getProductId());
        Optional<OrganizationMember> member = members.findById(a.getOrganizationMemberId());
        if (connector.isEmpty() || member.isEmpty()) {
            return;
        }
        Optional<Customer> customer = customers.findById(member.get().getCustomerId());
        if (customer.isEmpty() || customer.get().getKeycloakSub() == null || customer.get().getKeycloakSub().isBlank()) {
            return;
        }
        String type = a.getStatus() == MembershipStatus.ACTIVE ? PlatformEventTypes.USER_PRODUCT_ACCESS_GRANTED : PlatformEventTypes.USER_PRODUCT_ACCESS_REVOKED;
        Map<String, Object> payload = payload(c.version(), "organizationId", member.get().getOrganizationId());
        payload.put("productId", a.getProductId());
        outbox.publish(type, PlatformEventTypes.AGGREGATE_USER_ACCESS, customer.get().getKeycloakSub() + ":" + connector.get().getProductCode(), payload);
    }

    private static Map<String, Object> payload(long version, String key, Object value) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("version", version);
        p.put(key, value);
        return p;
    }

    /** For tests: the ids of the organizations in an event payload that names one or several. */
    @SuppressWarnings("unchecked")
    public static List<Long> organizationsOf(Map<String, Object> payload) {
        List<Long> out = new ArrayList<>();
        Object one = payload.get("organizationId");
        if (one instanceof Number n) {
            out.add(n.longValue());
        }
        Object many = payload.get("organizationIds");
        if (many instanceof List<?> list) {
            list.forEach(o -> { if (o instanceof Number n) out.add(n.longValue()); });
        }
        return out;
    }
}
