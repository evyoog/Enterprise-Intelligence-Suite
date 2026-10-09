package com.vyoog.eisplatform.modules.toolsync;

import com.vyoog.eisplatform.modules.integration.model.OutboxEvent;
import com.vyoog.eisplatform.modules.integration.repository.OutboxEventRepository;
import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNode;
import com.vyoog.eisplatform.modules.orghierarchy.repository.OrgNodeRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.OrganizationProductAccess;
import com.vyoog.eisplatform.modules.registration.model.ProductSubscription;
import com.vyoog.eisplatform.modules.registration.model.RegistrationOwnerType;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.model.SubscriptionStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationProductAccessRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.repository.ProductSubscriptionRepository;
import com.vyoog.eisplatform.modules.registration.service.SubscriptionClock;
import com.vyoog.eisplatform.modules.toolsync.model.ConnectorStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import com.vyoog.eisplatform.modules.toolsync.repository.TenantAppSchemaRepository;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolConnectorRepository;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolDeliveryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The whole platform on an in-memory database (the "test" profile) with the objects the synchronization works on: an organization, its
 * hierarchy, people, memberships, a subscription, product access and a connected tool. Every test removes the connectors and deliveries it
 * made, because "no tool connected" is a state the rest of the platform relies on (nothing is published then).
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(FakeToolGatewayConfig.class)
public abstract class ToolSyncTestBase {

    protected static final AtomicLong SEQ = new AtomicLong(System.nanoTime() % 1_000_000);
    public static final long PRODUCT_ID = 777_001L;
    public static final String PRODUCT_CODE = "thittam";

    @Autowired protected OrganizationRepository organizations;
    @Autowired protected CustomerRepository customers;
    @Autowired protected OrganizationMemberRepository members;
    @Autowired protected OrganizationProductAccessRepository accesses;
    @Autowired protected ProductSubscriptionRepository subscriptions;
    @Autowired protected OrgNodeRepository nodes;
    @Autowired protected ToolConnectorRepository connectors;
    @Autowired protected ToolDeliveryRepository deliveries;
    @Autowired protected TenantAppSchemaRepository tenants;
    @Autowired protected OutboxEventRepository outbox;

    /** The outbox is shared by every test of the JVM; events left by an earlier test would be fanned out in this one. */
    @BeforeEach
    void startWithAnEmptyOutbox() {
        outbox.deleteAll();
        deliveries.deleteAll();
        tenants.deleteAll();
        connectors.deleteAll();
    }

    @AfterEach
    void removeToolSyncState() {
        deliveries.deleteAll();
        tenants.deleteAll();
        connectors.deleteAll();
    }

    protected ToolConnector connector() {
        ToolConnector c = new ToolConnector();
        c.setProductId(PRODUCT_ID);
        c.setProductCode(PRODUCT_CODE);
        c.setBaseMcpUrl("http://tool.test/api/mcp");
        c.setClientId("thittam-sync");
        c.setStatus(ConnectorStatus.ACTIVE);
        return connectors.save(c);
    }

    protected Organization organization() {
        Organization o = new Organization();
        long n = SEQ.incrementAndGet();
        o.setName("Org " + n);
        o.setCode("ORG" + n);
        o.setBusinessEmail("biz" + n + "@org.example");
        o.setCountry("India");
        o.setLicensedSeats(10);
        return organizations.save(o);
    }

    protected Customer customer(String sub) {
        Customer c = new Customer();
        long n = SEQ.incrementAndGet();
        c.setEmail("person" + n + "@org.example");
        c.setFirstName("Pat" + n);
        c.setLastName("Lee");
        c.setKeycloakSub(sub);
        c.setStatus(RegistrationStatus.COMPLETED);
        return customers.save(c);
    }

    protected OrganizationMember member(Organization org, Customer customer, OrgRole role) {
        OrganizationMember m = new OrganizationMember();
        m.setOrganizationId(org.getId());
        m.setCustomerId(customer.getId());
        m.setOrgRole(role);
        m.setStatus(MembershipStatus.ACTIVE);
        return members.save(m);
    }

    protected OrgNode node(Organization org, OrgNode parent, String name, String type) {
        OrgNode n = new OrgNode();
        n.setOrganizationId(org.getId());
        n.setParentId(parent == null ? null : parent.getId());
        n.setName(name);
        n.setNodeType(type);
        return nodes.save(n);
    }

    protected ProductSubscription subscription(Organization org, SubscriptionStatus status, LocalDate lastDay) {
        return subscription(org, PRODUCT_ID, status, lastDay);
    }

    protected ProductSubscription subscription(Organization org, long productId, SubscriptionStatus status, LocalDate lastDay) {
        ProductSubscription s = new ProductSubscription();
        s.setProductId(productId);
        s.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        s.setOwnerOrganizationId(org.getId());
        s.setStatus(status);
        s.setStartedAt(Instant.parse("2025-11-01T00:00:00Z"));
        s.setExpiresAt(lastDay == null ? null : SubscriptionClock.endOfDay(lastDay));
        s.setQuantity(5);
        return subscriptions.save(s);
    }

    protected OrganizationProductAccess access(OrganizationMember member, String role, MembershipStatus status) {
        OrganizationProductAccess a = new OrganizationProductAccess();
        a.setOrganizationMemberId(member.getId());
        a.setProductId(PRODUCT_ID);
        a.setProductRole(role);
        a.setStatus(status);
        return accesses.save(a);
    }

    protected String newSub() {
        return UUID.randomUUID().toString();
    }

    protected List<OutboxEvent> events(String aggregateType, Object id) {
        return outbox.findByAggregateTypeAndAggregateIdOrderByIdAsc(aggregateType, String.valueOf(id));
    }
}
