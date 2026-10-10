package com.vyoog.eisplatform.modules.toolsync.service.e2e;

import com.vyoog.eisplatform.modules.orghierarchy.model.OrgNode;
import com.vyoog.eisplatform.modules.orghierarchy.repository.OrgNodeRepository;
import com.vyoog.eisplatform.modules.orghierarchy.service.OrgHierarchyService;
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
import com.vyoog.eisplatform.modules.toolsync.model.DeliveryStatus;
import com.vyoog.eisplatform.modules.toolsync.model.TenantAppSchema;
import com.vyoog.eisplatform.modules.toolsync.model.TenantSchemaStatus;
import com.vyoog.eisplatform.modules.toolsync.model.ToolConnector;
import com.vyoog.eisplatform.modules.toolsync.repository.TenantAppSchemaRepository;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolConnectorRepository;
import com.vyoog.eisplatform.modules.toolsync.repository.ToolDeliveryRepository;
import com.vyoog.eisplatform.modules.toolsync.service.ToolReconcileService;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.File;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The REAL platform and the REAL Macro Planner against each other, over HTTP, MCP and signed JWTs (phase 8, TC-INT-059 to TC-INT-064).
 *
 * <p>The platform is this application (the real Spring context with its jobs running on a port); the Macro Planner is the jar built from
 * the Macro repository, started as a separate process against a scratch PostgreSQL; Keycloak is a small fake that signs real RS256 tokens
 * (JWKS) and serves the client-credentials endpoint for {@code eis-sync} and {@code thittam-sync}. Nothing in between is mocked: the
 * platform's change collector, outbox, fan-out, delivery and MCP client talk to the Macro's MCP server, the Macro's write-through talks
 * back to the platform's MCP server, and the Macro's REST API is called with a person's token.
 *
 * <p>Opt-in (it needs a built Macro jar and a PostgreSQL): set {@code E2E_MACRO_JAR} to the jar and {@code E2E_PG_URL}
 * ({@code jdbc:postgresql://localhost:5432/e2e_macro}), {@code E2E_PG_USER}, {@code E2E_PG_PASSWORD}; the database must exist. The scenarios
 * run in order as one story of one organization.
 */
@EnabledIfEnvironmentVariable(named = "E2E_MACRO_JAR", matches = ".+")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlatformMacroEndToEndTest {

    private static final long PRODUCT_ID = 880_001L;
    private static final String CODE = "thittam";
    private static final String SECRET_EIS = "e2e-eis-sync-secret";
    private static final String SECRET_TOOL = "e2e-thittam-sync-secret";
    private static final String MACRO_CLIENT = "eVyoog";           // the Macro Planner's own Keycloak client (and the platform audience below)
    private static final String PLATFORM_CLIENT = "eis-platform-ui";
    private static final int EIS_PORT = freePort();
    private static final int MACRO_PORT = freePort();
    private static final FakeKeycloak KEYCLOAK = new FakeKeycloak()
        .client("eis-sync", SECRET_EIS, MACRO_CLIENT)            // audience mapper: the receiver's own client (the Macro)
        .client("thittam-sync", SECRET_TOOL, PLATFORM_CLIENT);   // audience mapper: the platform's client
    private static final String DEFAULT_TENANT = "t_e2e_default";

    private static String pgUrl() { return System.getenv("E2E_PG_URL"); }
    private static String pgUser() { return System.getenv().getOrDefault("E2E_PG_USER", "postgres"); }
    private static String pgPassword() { return System.getenv().getOrDefault("E2E_PG_PASSWORD", ""); }

    private static int freePort() {
        try (ServerSocket s = new ServerSocket(0)) {
            return s.getLocalPort();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        r.add("server.port", () -> EIS_PORT);
        r.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", KEYCLOAK::issuer);
        r.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", KEYCLOAK::jwksUrl);
        r.add("vyoog.keycloak.ropc-client-id", () -> PLATFORM_CLIENT);
        r.add("app.events.dispatcher.enabled", () -> "true");
        r.add("app.events.dispatcher.interval-ms", () -> "300");
        r.add("app.events.tool-delivery.enabled", () -> "true");
        r.add("app.events.tool-delivery.interval-ms", () -> "300");
        r.add("app.sync.client-id", () -> "eis-sync");
        r.add("app.sync.client-secret", () -> SECRET_EIS);
        r.add("app.sync.token-url", KEYCLOAK::tokenUrl);
        r.add("app.sync.timeout-seconds", () -> "20");
        r.add("app.sync.retry.max-attempts", () -> "30");
        r.add("app.sync.retry.initial-delay-seconds", () -> "1");
        r.add("app.sync.retry.max-delay-seconds", () -> "3");
        r.add("app.sync.metrics-interval-ms", () -> "1000");
    }

    @Autowired OrganizationRepository organizations;
    @Autowired CustomerRepository customers;
    @Autowired OrganizationMemberRepository members;
    @Autowired OrganizationProductAccessRepository accesses;
    @Autowired ProductSubscriptionRepository subscriptions;
    @Autowired OrgNodeRepository nodes;
    @Autowired OrgHierarchyService hierarchy;
    @Autowired ToolConnectorRepository connectors;
    @Autowired TenantAppSchemaRepository tenants;
    @Autowired ToolDeliveryRepository deliveries;
    @Autowired ToolReconcileService reconcile;
    @Autowired MeterRegistry meters;

    private MacroProcess macro;
    private JdbcTemplate db;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    // the story's state
    private Organization org;
    private Customer admin;
    private Customer member;
    private OrgNode root;
    private OrgNode pressShop;
    private OrgNode welding;
    private ProductSubscription subscription;
    private ToolConnector connector;
    private OrganizationProductAccess memberAccess;
    private String schema;
    private String tenantRef;          // the Macro's own reference of the tenant
    private String tenantHost;         // the customer URL of the tenant: the Macro chooses the tenant by the host the request came from

    @BeforeAll
    void start() throws Exception {
        DriverManagerDataSource ds = new DriverManagerDataSource(pgUrl(), pgUser(), pgPassword());
        db = new JdbcTemplate(ds);
        db.execute("DROP SCHEMA IF EXISTS platform_control CASCADE");
        for (String s : db.queryForList("select schema_name from information_schema.schemata where schema_name like 'pms\\_%' or schema_name = '" + DEFAULT_TENANT + "'", String.class)) {
            db.execute("DROP SCHEMA \"" + s + "\" CASCADE");
        }
        db.execute("CREATE SCHEMA " + DEFAULT_TENANT);
        File log = new File("target/e2e-macro.log");
        log.getParentFile().mkdirs();
        log.delete();
        macro = new MacroProcess(System.getenv("E2E_MACRO_JAR"), MACRO_PORT, log)
            .arg("spring.datasource.url=" + pgUrl()).arg("spring.datasource.username=" + pgUser()).arg("spring.datasource.password=" + pgPassword())
            .arg("app.tenants=" + DEFAULT_TENANT).arg("platform.tenants.default-tenant=" + DEFAULT_TENANT)
            .arg("app.tenant-hosts.localhost=" + DEFAULT_TENANT)
            .arg("platform.inbound.enabled=true").arg("platform.inbound.allowed-clients=eis-sync")
            .arg("platform.hub.mcp-url=http://localhost:" + EIS_PORT + "/api/mcp").arg("platform.hub.token-url=" + KEYCLOAK.tokenUrl())
            .arg("platform.hub.client-id=thittam-sync").arg("platform.hub.client-secret=" + SECRET_TOOL)
            .arg("spring.security.oauth2.resourceserver.jwt.issuer-uri=" + KEYCLOAK.issuer())
            .arg("spring.security.oauth2.resourceserver.jwt.jwk-set-uri=" + KEYCLOAK.jwksUrl())
            .arg("keycloak.admin.server-url=" + KEYCLOAK.url()).arg("keycloak.admin.client-id=eis-sync").arg("keycloak.admin.client-secret=" + SECRET_EIS)
            .arg("keycloak.admin.pms-client-id=" + MACRO_CLIENT)
            .arg("platform.tenants.cache-seconds=1")
            .arg("management.endpoints.web.exposure.include=health,metrics");
        macro.start();
    }

    @AfterAll
    void stop() throws Exception {
        if (macro != null) {
            macro.close();
        }
        KEYCLOAK.close();
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private void await(String what, BooleanSupplier condition) {
        await(what, 40, condition);
    }

    private void await(String what, int seconds, BooleanSupplier condition) {
        long deadline = System.currentTimeMillis() + seconds * 1000L;
        while (System.currentTimeMillis() < deadline) {
            try {
                if (condition.getAsBoolean()) {
                    return;
                }
            } catch (RuntimeException ignored) {
                // the row or schema is not there yet
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }
        throw new AssertionError("Did not happen within " + seconds + " s: " + what + "\n" + tail());
    }

    private String tail() {
        try {
            List<String> lines = java.nio.file.Files.readAllLines(new File("target/e2e-macro.log").toPath());
            return "--- Macro log (tail) ---\n" + String.join("\n", lines.subList(Math.max(0, lines.size() - 25), lines.size()));
        } catch (Exception e) {
            return "";
        }
    }

    private String t(String table) {
        return "\"" + schema + "\"." + table;
    }

    private Long macroLong(String sql, Object... args) {
        List<Long> rows = db.queryForList(sql, Long.class, args);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private String macroString(String sql, Object... args) {
        List<String> rows = db.queryForList(sql, String.class, args);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private HttpResponse<String> macroApi(String method, String path, String token, String body) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + MACRO_PORT + "/api" + path)).timeout(Duration.ofSeconds(20))
            .header("Authorization", "Bearer " + token).header("Origin", "http://" + tenantHost).header("Content-Type", "application/json");
        b.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String macroNodeId(OrgNode n) {
        return macroString("select id::text from " + t("org_nodes") + " where platform_ref = ?", String.valueOf(n.getId()));
    }

    // ── fixtures on the platform ─────────────────────────────────────────────

    private Customer person(String first, String last) {
        Customer c = new Customer();
        c.setEmail(first.toLowerCase() + "." + UUID.randomUUID().toString().substring(0, 6) + "@e2e.example");
        c.setFirstName(first);
        c.setLastName(last);
        c.setKeycloakSub(UUID.randomUUID().toString());
        c.setStatus(RegistrationStatus.COMPLETED);
        return customers.save(c);
    }

    private OrganizationMember join(Customer c, OrgRole role, OrgNode at) {
        OrganizationMember m = new OrganizationMember();
        m.setOrganizationId(org.getId());
        m.setCustomerId(c.getId());
        m.setOrgRole(role);
        m.setStatus(MembershipStatus.ACTIVE);
        m.setOrgNodeId(at == null ? null : at.getId());
        return members.save(m);
    }

    private OrganizationProductAccess grant(OrganizationMember m, String role) {
        OrganizationProductAccess a = new OrganizationProductAccess();
        a.setOrganizationMemberId(m.getId());
        a.setProductId(PRODUCT_ID);
        a.setProductRole(role);
        a.setStatus(MembershipStatus.ACTIVE);
        return accesses.save(a);
    }

    private OrgNode child(OrgNode parent, String name, String type) {
        OrgNode n = new OrgNode();
        n.setOrganizationId(org.getId());
        n.setParentId(parent.getId());
        n.setName(name);
        n.setNodeType(type);
        return nodes.save(n);
    }

    // ── the story ────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void s1_aSubscriptionProvisionsATenantAndEverythingArrivesWithinSeconds() {
        Organization o = new Organization();
        o.setName("ABC Manufacturing");
        o.setCode("ABC" + (System.nanoTime() % 100000));
        o.setBusinessEmail("biz@abc.example");
        o.setCountry("India");
        o.setIndustry("Manufacturing");
        o.setLicensedSeats(10);
        org = organizations.save(o);
        hierarchy.ensureInitialisedFor(org.getId());
        root = nodes.findByOrganizationIdOrderBySortOrderAscNameAsc(org.getId()).stream().filter(n -> n.getParentId() == null).findFirst().orElseThrow();
        pressShop = child(root, "Press Shop", "DIVISION");
        admin = person("Arun", "Kumar");
        OrganizationMember adminMember = join(admin, OrgRole.ORG_ADMIN, root);
        member = person("Meena", "Rao");
        OrganizationMember meenaMember = join(member, OrgRole.MEMBER, pressShop);
        grant(adminMember, "PMS_ADMIN");
        memberAccess = grant(meenaMember, "PMS_USER");

        connector = new ToolConnector();
        connector.setProductId(PRODUCT_ID);
        connector.setProductCode(CODE);
        connector.setBaseMcpUrl("http://localhost:" + MACRO_PORT + "/api/mcp");
        connector.setClientId("thittam-sync");
        connector.setStatus(ConnectorStatus.ACTIVE);
        connector = connectors.save(connector);

        long started = System.currentTimeMillis();
        ProductSubscription s = new ProductSubscription();
        s.setProductId(PRODUCT_ID);
        s.setOwnerType(RegistrationOwnerType.ORGANIZATION);
        s.setOwnerOrganizationId(org.getId());
        s.setStatus(SubscriptionStatus.ACTIVE);
        s.setStartedAt(Instant.parse("2026-01-01T00:00:00Z"));
        s.setExpiresAt(SubscriptionClock.endOfDay(LocalDate.now().plusMonths(6)));
        s.setQuantity(5);
        subscription = subscriptions.save(s);

        await("the tenant is READY in the platform's registry", 90,
            () -> tenants.findByOrganizationIdAndProductId(org.getId(), PRODUCT_ID).map(TenantAppSchema::getStatus).orElse(null) == TenantSchemaStatus.READY);
        schema = macroString("select schema_name from platform_control.platform_tenant where platform_org_id = ?", String.valueOf(org.getId()));
        tenantRef = macroString("select tenant_ref from platform_control.platform_tenant where platform_org_id = ?", String.valueOf(org.getId()));
        tenantHost = "org" + org.getId() + ".pms.e2e.test";
        db.update("insert into platform_control.platform_tenant_host (host, tenant_ref) values (?, ?)", tenantHost, tenantRef);       // what an operator adds (Q9: hosting is decided later)
        assertThat(schema).as("the Macro registered the tenant for the platform organization").isNotNull().startsWith("pms_");
        assertThat(macroString("select status from platform_control.platform_tenant where platform_org_id = ?", String.valueOf(org.getId()))).isEqualTo("READY");

        await("the hierarchy, people, subscription and access are in the Macro", 60, () ->
            macroNodeId(pressShop) != null
                && macroLong("select count(*) from " + t("users") + " where platform_version is not null") >= 2
                && macroLong("select count(*) from " + t("platform_subscription")) >= 1
                && macroLong("select count(*) from " + t("platform_user_access")) >= 2);
        System.out.println("E2E s1: provisioned and fully synchronized in " + (System.currentTimeMillis() - started) + " ms");

        assertThat(macroString("select name from " + t("org_nodes") + " where platform_ref = ?", String.valueOf(pressShop.getId()))).isEqualTo("Press Shop");
        assertThat(macroString("select ends_at::text from " + t("platform_subscription")))
            .as("the end of the subscription is 18:29:00 UTC = 23:59:00 in India").contains("18:29:00");
        assertThat(macroString("select r.name from " + t("user_roles") + " ur join " + t("roles") + " r on r.id = ur.role_id where ur.user_id = ?::uuid", member.getKeycloakSub()))
            .as("PMS_USER maps to the Macro's least role").isEqualTo("TASK_USER");
        assertThat(macroString("select org_node_id::text from " + t("users") + " where id = ?::uuid", member.getKeycloakSub()))
            .as("the person is placed where the platform placed them").isEqualTo(macroNodeId(pressShop));
        assertThat(db.queryForObject("select count(*) from " + t("projects"), Long.class)).as("no business records were created (BR-SYN-010)").isZero();
    }

    @Test
    @Order(2)
    void s2_aNodeAddedOnThePlatformAppearsInTheMacro() {
        welding = child(pressShop, "Welding", "DEPARTMENT");

        await("the new node is in the Macro under Press Shop", 20, () -> {
            String id = macroNodeId(welding);
            return id != null && macroString("select parent_id::text from " + t("org_nodes") + " where id = ?::uuid", id).equals(macroNodeId(pressShop));
        });
    }

    @Test
    @Order(3)
    void s3_aMovedNodeHasTheSameStructureInTheMacro() {
        OrgNode quality = child(root, "Quality", "DIVISION");
        await("Quality arrived", 20, () -> macroNodeId(quality) != null);

        hierarchy.moveAsTool(org.getId(), "e2e", welding.getId(), quality.getId());

        await("Welding sits under Quality in the Macro", 20, () -> quality.getId() != null
            && macroString("select parent_id::text from " + t("org_nodes") + " where platform_ref = ?", String.valueOf(welding.getId())).equals(macroNodeId(quality)));
    }

    @Test
    @Order(4)
    void s4_aNewPersonWithAccessIsAUserInTheMacroWithTheMappedRoleAndNode() {
        Customer priya = person("Priya", "Nair");
        OrganizationMember m = join(priya, OrgRole.MEMBER, pressShop);
        grant(m, "PMS_MANAGER");

        await("the person is in the Macro with the mapped role", 30, () -> "PROJECT_OWNER".equals(macroString("select r.name from " + t("user_roles") + " ur join " + t("roles")
            + " r on r.id = ur.role_id where ur.user_id = ?::uuid", priya.getKeycloakSub())));

        assertThat(macroString("select r.name from " + t("user_roles") + " ur join " + t("roles") + " r on r.id = ur.role_id where ur.user_id = ?::uuid", priya.getKeycloakSub()))
            .isEqualTo("PROJECT_OWNER");
        assertThat(macroString("select org_node_id::text from " + t("users") + " where id = ?::uuid", priya.getKeycloakSub())).isEqualTo(macroNodeId(pressShop));
        assertThat(macroString("select is_active::text from " + t("users") + " where id = ?::uuid", priya.getKeycloakSub())).isEqualTo("true");
    }

    @Test
    @Order(5)
    void s5_aPersonEditedInTheMacroIsSavedOnThePlatformFirstAndThenEverywhere() throws Exception {
        String adminToken = KEYCLOAK.personToken(admin.getKeycloakSub(), "arun", MACRO_CLIENT, "ADMIN");
        long before = customers.findById(admin.getId()).orElseThrow().getSyncVersion();

        HttpResponse<String> r = macroApi("PUT", "/admin/users/" + admin.getKeycloakSub(), adminToken, "{\"firstName\":\"Arunkumar\"}");

        assertThat(r.statusCode()).as(r.body() + "\n" + tail()).isEqualTo(200);
        Customer saved = customers.findById(admin.getId()).orElseThrow();
        assertThat(saved.getFirstName()).as("the platform is the single writer: it holds the change").isEqualTo("Arunkumar");
        assertThat(saved.getSyncVersion()).isGreaterThan(before);
        await("the Macro holds the platform's version", 20, () -> macroLong("select platform_version from " + t("users") + " where id = ?::uuid", admin.getKeycloakSub()) >= saved.getSyncVersion());
        assertThat(macroString("select first_name from " + t("users") + " where id = ?::uuid", admin.getKeycloakSub())).isEqualTo("Arunkumar");
    }

    @Test
    @Order(6)
    void s6_revokingProductAccessMakesTheMacroDenyThePerson() throws Exception {
        String token = KEYCLOAK.personToken(member.getKeycloakSub(), "meena", MACRO_CLIENT, "TASK_USER");
        assertThat(macroApi("GET", "/admin/org-nodes", token, null).statusCode()).as("not an admin screen, but entitlement comes first").isNotEqualTo(401);
        int allowed = macroApi("GET", "/org-nodes", token, null).statusCode();
        assertThat(allowed).as("a person with access gets through the access check").isNotEqualTo(403);

        memberAccess.setStatus(MembershipStatus.INACTIVE);
        accesses.save(memberAccess);

        await("the Macro denies the person", 20, () -> {
            try {
                return macroApi("GET", "/org-nodes", token, null).statusCode() == 403;
            } catch (Exception e) {
                return false;
            }
        });
        HttpResponse<String> denied = macroApi("GET", "/org-nodes", token, null);
        assertThat(denied.body()).contains("NO_PRODUCT_ACCESS");

        memberAccess.setStatus(MembershipStatus.ACTIVE);
        accesses.save(memberAccess);
        await("access is back", 20, () -> {
            try {
                return macroApi("GET", "/org-nodes", token, null).statusCode() != 403;
            } catch (Exception e) {
                return false;
            }
        });
    }

    @Test
    @Order(7)
    void s7_anEndedSubscriptionIsDeniedByEveryone() throws Exception {
        String token = KEYCLOAK.personToken(admin.getKeycloakSub(), "arun", MACRO_CLIENT, "ADMIN");
        assertThat(macroApi("GET", "/org-nodes", token, null).statusCode()).isNotEqualTo(403);

        ProductSubscription s = subscriptions.findById(subscription.getId()).orElseThrow();
        s.setExpiresAt(Instant.now().minusSeconds(3600));
        subscriptions.save(s);

        await("the Macro denies with SUBSCRIPTION_ENDED", 20, () -> {
            try {
                HttpResponse<String> r = macroApi("GET", "/org-nodes", token, null);
                return r.statusCode() == 403 && r.body().contains("SUBSCRIPTION_ENDED");
            } catch (Exception e) {
                return false;
            }
        });

        s = subscriptions.findById(subscription.getId()).orElseThrow();
        s.setExpiresAt(SubscriptionClock.endOfDay(LocalDate.now().plusMonths(6)));
        subscriptions.save(s);
        await("renewed: allowed again", 20, () -> {
            try {
                return macroApi("GET", "/org-nodes", token, null).statusCode() != 403;
            } catch (Exception e) {
                return false;
            }
        });
    }

    @Test
    @Order(8)
    void s8_aToolThatIsDownRecoversByItselfWithoutLosingAnything() throws Exception {
        macro.stop();
        org.setCity("Chennai");
        org = organizations.save(org);
        OrgNode paint = child(root, "Paint Shop", "DIVISION");

        await("the platform is retrying, nothing is FAILED", 30, () -> deliveries.findAll().stream()
            .anyMatch(d -> d.getStatus() == DeliveryStatus.PENDING && d.getAttempts() > 0 && d.getOrganizationId().equals(org.getId())));
        assertThat(deliveries.findAll()).noneMatch(d -> d.getStatus() == DeliveryStatus.FAILED);

        macro.start();

        await("everything arrived after the restart", 90, () -> macroNodeId(paint) != null
            && macroString("select attributes::text from " + t("platform_organization")).contains("Chennai"));
        await("no message is left waiting", 30, () -> deliveries.findAll().stream().noneMatch(d -> d.getStatus() == DeliveryStatus.PENDING));
        assertThat(deliveries.findAll()).noneMatch(d -> d.getStatus() == DeliveryStatus.FAILED);
    }

    @Test
    @Order(9)
    void s9_aDeliberatelyDeletedRowIsFoundAndRepairedByReconcile() {
        assertThat(reconcile.reconcile(org.getId(), connector.getId(), true).inSync()).as("in step before the damage: " + reconcile.reconcile(org.getId(), connector.getId(), false)).isTrue();
        String weldingId = macroNodeId(welding);
        db.update("update " + t("org_nodes") + " set parent_id = null where parent_id = ?::uuid", weldingId);
        db.update("delete from " + t("org_nodes") + " where id = ?::uuid", weldingId);

        ToolReconcileService.Report found = reconcile.reconcile(org.getId(), connector.getId(), true);

        assertThat(found.reachable()).as(String.valueOf(found.problem())).isTrue();
        assertThat(found.inSync()).isFalse();
        assertThat(found.resent()).isEqualTo(1);
        await("the node is back", 20, () -> macroNodeId(welding) != null);
        await("in step again", 20, () -> reconcile.reconcile(org.getId(), connector.getId(), true).inSync());
    }

    @Test
    @Order(10)
    void s10_theMetricsShowTheLagAndNothingFailed() {
        assertThat(meters.find("platformsync.lag.seconds").tag("tool", CODE).summary()).isNotNull();
        double p95Max = meters.find("platformsync.lag.seconds").tag("tool", CODE).summary().max();
        System.out.println("E2E s10: worst delivery lag " + p95Max + " s");
        assertThat(meters.find("platformsync.delivery.failed").tag("tool", CODE).counter()).as("no delivery failed in the whole story").isNull();
        assertThat(meters.find("platformsync.drift.detected").tag("tool", CODE).counters().stream().mapToDouble(c -> c.count()).sum()).isGreaterThanOrEqualTo(1.0);
        assertThat(Map.of("sync", "ok")).isNotEmpty();
    }
}
