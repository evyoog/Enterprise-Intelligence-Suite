package com.vyoog.eisplatform.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vyoog.eisplatform.modules.audit.repository.AuditLogRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** REQ-INT-001 (C61): API keys — create once, act as the owner, never
 * reach platform administration, revoke, /v1 alias. TC-INT-001..007. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiKeyAuthenticationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private ObjectMapper objectMapper;

    private String newCustomerSub() {
        Customer customer = new Customer();
        customer.setEmail("api-key-" + System.nanoTime() + "@test.example");
        customer.setFirstName("Api");
        customer.setLastName("Owner");
        String sub = "kc-" + System.nanoTime();
        customer.setKeycloakSub(sub);
        customerRepository.save(customer);
        return sub;
    }

    private RequestPostProcessor user(String sub, String... roles) {
        return jwt().jwt(j -> j.subject(sub)).authorities(java.util.Arrays.stream(roles).map(r -> (org.springframework.security.core.GrantedAuthority) new SimpleGrantedAuthority(r)).toList());
    }

    private JsonNode createKey(String sub, String name, String... roles) throws Exception {
        String body = mockMvc.perform(post("/me/api-keys").with(user(sub, roles))
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"" + name + "\"}"))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body);
    }

    @Test
    void theFullKeyIsShownOnceAndTheKeyActsAsItsOwner() throws Exception {
        String sub = newCustomerSub();
        JsonNode created = createKey(sub, "CRM sync", "ROLE_ACTIVITY_USER");
        String key = created.get("key").asText();
        assertThat(key).matches("eis_[A-Za-z0-9]{8}_[A-Za-z0-9]{40}");
        assertThat(created.get("prefix").asText()).isEqualTo(key.substring(0, 12));
        assertThat(created.get("status").asText()).isEqualTo("ACTIVE");

        mockMvc.perform(get("/me/api-keys").header("X-API-Key", key))
            .andExpect(status().isOk())
            .andExpect(header().string("API-Version", "1"))
            .andExpect(jsonPath("$[0].name").value("CRM sync"))
            .andExpect(jsonPath("$[0].key").doesNotExist())
            .andExpect(jsonPath("$[0].requestCount").value(1));
    }

    @Test
    void everyEndpointIsAlsoServedUnderV1() throws Exception {
        String sub = newCustomerSub();
        String key = createKey(sub, "Versioned").get("key").asText();
        mockMvc.perform(get("/v1/me/api-keys").header("X-API-Key", key))
            .andExpect(status().isOk())
            .andExpect(header().string("API-Version", "1"))
            .andExpect(jsonPath("$[0].name").value("Versioned"));
        mockMvc.perform(get("/v1/me/api-keys")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v1/admin/events").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void aKeyNeverReachesPlatformAdministrationEvenForAnAdmin() throws Exception {
        String sub = newCustomerSub();
        String key = createKey(sub, "Admin's key", "ROLE_ADMIN").get("key").asText();
        mockMvc.perform(get("/admin/events").header("X-API-Key", key)).andExpect(status().isForbidden());
        mockMvc.perform(get("/v1/admin/api-keys").header("X-API-Key", key)).andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/events").with(user(sub, "ROLE_ADMIN"))).andExpect(status().isOk());
    }

    @Test
    void anInvalidOrRevokedKeyIsRejectedAndTheRevokedUseIsAudited() throws Exception {
        mockMvc.perform(get("/me/api-keys").header("X-API-Key", "eis_nothere_" + "x".repeat(40)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value("API_KEY_INVALID"));

        String sub = newCustomerSub();
        JsonNode created = createKey(sub, "Soon revoked");
        long id = created.get("id").asLong();
        mockMvc.perform(post("/me/api-keys/" + id + "/revoke").with(user(sub)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REVOKED"));
        mockMvc.perform(post("/me/api-keys/" + id + "/revoke").with(user(sub))).andExpect(status().isConflict());
        mockMvc.perform(get("/me/api-keys").header("X-API-Key", created.get("key").asText()))
            .andExpect(status().isUnauthorized());
        assertThat(auditLogRepository.findAll()).anyMatch(a -> "API_KEY_REVOKED_USED".equals(a.getAction())
            && String.valueOf(id).equals(a.getTargetId()));
    }

    @Test
    void usersManageOnlyTheirOwnKeys() throws Exception {
        String owner = newCustomerSub();
        String other = newCustomerSub();
        long id = createKey(owner, "Mine").get("id").asLong();
        mockMvc.perform(post("/me/api-keys/" + id + "/revoke").with(user(other))).andExpect(status().isNotFound());
        mockMvc.perform(get("/me/api-keys").with(user(other))).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void aKeyCannotCreateAnotherKey() throws Exception {
        String sub = newCustomerSub();
        String key = createKey(sub, "Parent").get("key").asText();
        mockMvc.perform(post("/me/api-keys").header("X-API-Key", key)
                .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Child\"}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void atMostTenActiveKeysAndTheExpiryMustBeInTheFuture() throws Exception {
        String sub = newCustomerSub();
        for (int i = 0; i < 10; i++) {
            createKey(sub, "Key " + i);
        }
        mockMvc.perform(post("/me/api-keys").with(user(sub)).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Eleventh\"}"))
            .andExpect(status().isBadRequest());
        String other = newCustomerSub();
        mockMvc.perform(post("/me/api-keys").with(user(other)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Past\",\"expiresAt\":\"2020-01-01T00:00:00Z\"}"))
            .andExpect(status().isBadRequest());
        mockMvc.perform(post("/me/api-keys").with(user(other)).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"\"}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void administratorsSeeEveryKeysUsage() throws Exception {
        String sub = newCustomerSub();
        createKey(sub, "Visible to admins");
        mockMvc.perform(get("/admin/api-keys").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"))))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/admin/api-keys").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(20))
            .andExpect(jsonPath("$.items[0].requestCount").exists());
    }
}
