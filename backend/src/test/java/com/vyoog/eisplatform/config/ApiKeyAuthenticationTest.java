package com.vyoog.eisplatform.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.audit.repository.AuditLogRepository;
import com.vyoog.eisplatform.modules.integration.model.ApiKey;
import com.vyoog.eisplatform.modules.integration.repository.ApiKeyRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** REQ-INT-001 (C61, C92): organization-owned API keys with scopes — created once by an organization administrator, act
 * as the organization, never reach platform administration, revoke, /v1 alias. TC-INT-001..007, TC-INT-030..034. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiKeyAuthenticationTest {

    private static final String KEYS = "/organization/me/api-keys";

    @Autowired private MockMvc mockMvc;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private OrganizationRepository organizationRepository;
    @Autowired private OrganizationMemberService memberService;
    @Autowired private ApiKeyRepository apiKeyRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private ObjectMapper objectMapper;

    /** An organization with one administrator (and a login) and the administrator's subject. */
    private record Org(Organization organization, String adminSub, Customer admin, OrganizationMember member) {
    }

    private Customer customer(String prefix) {
        Customer customer = new Customer();
        customer.setEmail(prefix + System.nanoTime() + "@test.example");
        customer.setFirstName("Api");
        customer.setLastName("Admin");
        customer.setKeycloakSub("kc-" + System.nanoTime());
        return customerRepository.save(customer);
    }

    private Org newOrg() {
        Organization o = new Organization();
        o.setName("Key Org " + System.nanoTime());
        o.setCode("KEY-" + System.nanoTime());
        o.setBusinessEmail("keys@test.example");
        o.setCountry("India");
        o.setLicensedSeats(20);
        o = organizationRepository.save(o);
        Customer admin = customer("admin-");
        OrganizationMember member = memberService.addMember(o.getId(), admin.getId(), OrgRole.ORG_ADMIN);
        return new Org(o, admin.getKeycloakSub(), admin, member);
    }

    private RequestPostProcessor user(String sub, String... roles) {
        return jwt().jwt(j -> j.subject(sub)).authorities(Arrays.stream(roles).map(r -> (GrantedAuthority) new SimpleGrantedAuthority(r)).toList());
    }

    private JsonNode createKeyWithBody(String sub, String body, String... roles) throws Exception {
        String response = mockMvc.perform(post(KEYS).with(user(sub, roles)).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response);
    }

    private JsonNode createKey(String sub, String name, String scope, String... roles) throws Exception {
        return createKeyWithBody(sub, "{\"name\":\"" + name + "\"" + (scope == null ? "" : ",\"scope\":\"" + scope + "\"") + "}", roles);
    }

    // TC-INT-001
    @Test
    void theFullKeyIsShownOnceAndTheKeyActsAsAnAdministratorOfTheOrganization() throws Exception {
        Org org = newOrg();
        JsonNode created = createKey(org.adminSub(), "CRM sync", "READ_WRITE", "ROLE_ACTIVITY_USER");
        String key = created.get("key").asText();
        assertThat(key).matches("eis_[A-Za-z0-9]{8}_[A-Za-z0-9]{40}");
        assertThat(created.get("prefix").asText()).isEqualTo(key.substring(0, 12));
        assertThat(created.get("status").asText()).isEqualTo("ACTIVE");
        assertThat(created.get("scope").asText()).isEqualTo("READ_WRITE");
        assertThat(created.get("personal").asBoolean()).isFalse();

        mockMvc.perform(get("/organization/me").header("X-API-Key", key))
            .andExpect(status().isOk())
            .andExpect(header().string("API-Version", "1"))
            .andExpect(jsonPath("$.name").value(org.organization().getName()));
        mockMvc.perform(get(KEYS).header("X-API-Key", key))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("CRM sync"))
            .andExpect(jsonPath("$[0].key").doesNotExist())
            .andExpect(jsonPath("$[0].requestCount").value(2));
    }

    // TC-INT-002
    @Test
    void everyEndpointIsAlsoServedUnderV1() throws Exception {
        Org org = newOrg();
        String key = createKey(org.adminSub(), "Versioned", "READ").get("key").asText();
        mockMvc.perform(get("/v1/organization/me").header("X-API-Key", key))
            .andExpect(status().isOk())
            .andExpect(header().string("API-Version", "1"));
        mockMvc.perform(get("/v1" + KEYS)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v1/admin/events").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
    }

    // TC-INT-003
    @Test
    void aKeyNeverReachesPlatformAdministrationEvenWhenItsCreatorIsAnAdmin() throws Exception {
        Org org = newOrg();
        String key = createKey(org.adminSub(), "Admin's key", "READ_WRITE", "ROLE_ADMIN").get("key").asText();
        mockMvc.perform(get("/admin/events").header("X-API-Key", key)).andExpect(status().isForbidden());
        mockMvc.perform(get("/v1/admin/api-keys").header("X-API-Key", key)).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/events").with(user(org.adminSub(), "ROLE_ADMIN"))).andExpect(status().isOk());
    }

    // TC-INT-004
    @Test
    void anInvalidOrRevokedKeyIsRejectedAndTheRevokedUseIsAudited() throws Exception {
        mockMvc.perform(get(KEYS).header("X-API-Key", "eis_nothere_" + "x".repeat(40)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("API_KEY_INVALID"));

        Org org = newOrg();
        JsonNode created = createKey(org.adminSub(), "Soon revoked", "READ");
        long id = created.get("id").asLong();
        mockMvc.perform(post(KEYS + "/" + id + "/revoke").with(user(org.adminSub())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REVOKED"));
        mockMvc.perform(post(KEYS + "/" + id + "/revoke").with(user(org.adminSub()))).andExpect(status().isConflict());
        mockMvc.perform(get(KEYS).header("X-API-Key", created.get("key").asText())).andExpect(status().isUnauthorized());
        assertThat(auditLogRepository.findAll()).anyMatch(a -> "API_KEY_REVOKED_USED".equals(a.getAction())
            && String.valueOf(id).equals(a.getTargetId()));
    }

    // TC-INT-030 (API-1 B)
    @Test
    void onlyOrganizationAdministratorsCreateKeysAndOnlyForTheirOwnOrganization() throws Exception {
        Org a = newOrg();
        Org b = newOrg();
        long keyOfA = createKey(a.adminSub(), "A's key", "READ").get("id").asLong();

        // an ordinary member of the organization may not create, list or revoke
        Customer member = customer("member-");
        memberService.addMember(a.organization().getId(), member.getId(), OrgRole.MEMBER);
        mockMvc.perform(post(KEYS).with(user(member.getKeycloakSub())).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Nope\"}"))
            .andExpect(status().isForbidden());
        mockMvc.perform(get(KEYS).with(user(member.getKeycloakSub()))).andExpect(status().isForbidden());
        // a person who belongs to no organization may not either
        Customer loner = customer("loner-");
        mockMvc.perform(post(KEYS).with(user(loner.getKeycloakSub())).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Nope\"}"))
            .andExpect(status().isNotFound());
        // another organization's administrator sees and revokes nothing of A's
        mockMvc.perform(get(KEYS).with(user(b.adminSub()))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mockMvc.perform(post(KEYS + "/" + keyOfA + "/revoke").with(user(b.adminSub()))).andExpect(status().isNotFound());
        // the old personal endpoint no longer creates keys
        mockMvc.perform(post("/me/api-keys").with(user(a.adminSub())).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Mine\"}"))
            .andExpect(status().isForbidden());
        mockMvc.perform(get(KEYS).with(user(a.adminSub()))).andExpect(jsonPath("$[0].createdByName").value("Api Admin"));
    }

    // TC-INT-005
    @Test
    void aKeyCannotCreateOrRevokeKeys() throws Exception {
        Org org = newOrg();
        JsonNode parent = createKey(org.adminSub(), "Parent", "READ_WRITE");
        mockMvc.perform(post(KEYS).header("X-API-Key", parent.get("key").asText())
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Child\"}"))
            .andExpect(status().isForbidden());
        mockMvc.perform(post(KEYS + "/" + parent.get("id").asLong() + "/revoke").header("X-API-Key", parent.get("key").asText()))
            .andExpect(status().isForbidden());
    }

    // TC-INT-006
    @Test
    void atMostTenActiveKeysPerOrganizationAndTheInputIsValidated() throws Exception {
        Org org = newOrg();
        for (int i = 0; i < 10; i++) {
            createKey(org.adminSub(), "Key " + i, "READ");
        }
        mockMvc.perform(post(KEYS).with(user(org.adminSub())).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Eleventh\"}"))
            .andExpect(status().isBadRequest());
        Org other = newOrg();
        mockMvc.perform(post(KEYS).with(user(other.adminSub())).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Past\",\"expiresAt\":\"2020-01-01T00:00:00Z\"}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post(KEYS).with(user(other.adminSub())).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post(KEYS).with(user(other.adminSub())).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Odd\",\"scope\":\"ADMIN\"}"))
            .andExpect(status().isBadRequest());
    }

    // TC-INT-007
    @Test
    void administratorsSeeEveryKeysUsageWithItsOrganizationAndScope() throws Exception {
        Org org = newOrg();
        createKey(org.adminSub(), "Visible to admins", "READ");
        mockMvc.perform(get("/admin/api-keys").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/api-keys").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.items[0].requestCount").exists())
            .andExpect(jsonPath("$.items[0].scope").value("READ"))
            .andExpect(jsonPath("$.items[0].organizationName").value(org.organization().getName()));
    }

    // TC-INT-031 (API-4 B)
    @Test
    void aReadOnlyKeyCanOnlyReadAndAReadWriteKeyCanAlsoWrite() throws Exception {
        Org org = newOrg();
        String readOnly = createKey(org.adminSub(), "Reporting", null).get("key").asText(); // no scope given = READ
        String readWrite = createKey(org.adminSub(), "Automation", "READ_WRITE").get("key").asText();
        String body = "{\"mfaRequired\":false}";

        mockMvc.perform(get("/organization/me").header("X-API-Key", readOnly)).andExpect(status().isOk());
        mockMvc.perform(patch("/organization/me/mfa-policy").header("X-API-Key", readOnly).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("API_KEY_READ_ONLY"));
        mockMvc.perform(patch("/v1/organization/me/mfa-policy").header("X-API-Key", readWrite).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isOk());
        mockMvc.perform(get(KEYS).with(user(org.adminSub()))).andExpect(jsonPath("$[?(@.name=='Reporting')].scope").value("READ"));
    }

    // TC-INT-032 (API-1 B)
    @Test
    void anOrganizationKeyOutlivesItsCreatorAsLongAsAnAdministratorRemains() throws Exception {
        Org org = newOrg();
        String key = createKey(org.adminSub(), "Integration", "READ").get("key").asText();
        Customer second = customer("second-");
        memberService.addMember(org.organization().getId(), second.getId(), OrgRole.ORG_ADMIN);

        memberService.removeMember(org.member().getId()); // the creator leaves
        mockMvc.perform(get("/organization/me").header("X-API-Key", key)).andExpect(status().isOk());

        OrganizationMember secondMember = memberService.listActiveMembers(org.organization().getId()).get(0);
        memberService.removeMember(secondMember.getId()); // nobody is left to act
        mockMvc.perform(get("/organization/me").header("X-API-Key", key)).andExpect(status().isUnauthorized());
        assertThat(auditLogRepository.findAll()).anyMatch(a -> "API_KEY_NO_ADMINISTRATOR".equals(a.getAction()));
    }

    // TC-INT-033
    @Test
    void aLegacyPersonalKeyKeepsWorkingWithFullRightsButCannotBeCreatedAnymore() throws Exception {
        Customer person = customer("legacy-");
        String full = "eis_legacyab_" + "k".repeat(40);
        ApiKey legacy = new ApiKey();
        legacy.setOwnerCustomerId(person.getId());
        legacy.setOwnerKeycloakSub(person.getKeycloakSub());
        legacy.setName("Old key");
        legacy.setKeyPrefix("eis_legacyab");
        legacy.setKeyHash(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(full.getBytes(StandardCharsets.UTF_8))));
        legacy.setCreatedAt(Instant.now());
        apiKeyRepository.save(legacy);

        assertThat(legacy.getScope()).isEqualTo("READ_WRITE");
        mockMvc.perform(get("/me/api-keys").header("X-API-Key", full))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].personal").value(true))
            .andExpect(jsonPath("$[0].scope").value("READ_WRITE"));
        mockMvc.perform(post("/me/api-keys").with(user(person.getKeycloakSub())).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"New\"}"))
            .andExpect(status().isForbidden());
        mockMvc.perform(post("/me/api-keys/" + legacy.getId() + "/revoke").with(user(person.getKeycloakSub()))).andExpect(status().isOk());
        mockMvc.perform(get("/me/api-keys").header("X-API-Key", full)).andExpect(status().isUnauthorized());
    }
}
