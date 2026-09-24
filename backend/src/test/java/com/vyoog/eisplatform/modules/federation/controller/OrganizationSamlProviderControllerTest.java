package com.vyoog.eisplatform.modules.federation.controller;

import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationMemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Phase 4 (2026.3.3): "only authorized administrators may manage SAML" —
 * this is the one test that actually exercises the real enforcement point
 * ({@link OrganizationSamlProviderController} delegating to
 * {@code OrganizationSelfService#requireOrganizationManagement}, the same
 * MANAGE_ORGANIZATION gate the MFA policy toggle uses) end to end through
 * the real filter chain, rather than only testing SamlProviderService's own
 * internal logic directly (which SamlProviderServiceTest already does, but
 * bypasses this permission gate entirely by calling the service directly).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrganizationSamlProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrganizationMemberService organizationMemberService;

    private Organization newOrganization() {
        Organization organization = new Organization();
        organization.setName("SAML Controller Test Org " + System.nanoTime());
        organization.setCode("SAML-CTRL-" + System.nanoTime());
        organization.setBusinessEmail("biz-" + System.nanoTime() + "@test-org.example");
        organization.setCountry("India");
        organization.setLicensedSeats(10);
        return organizationRepository.save(organization);
    }

    private Customer newCustomer(String keycloakSub) {
        Customer customer = new Customer();
        customer.setEmail("saml-ctrl-" + System.nanoTime() + "@test-org.example");
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setStatus(RegistrationStatus.COMPLETED);
        customer.setKeycloakSub(keycloakSub);
        return customerRepository.save(customer);
    }

    @Test
    void orgAdminCanListTheirOwnOrganizationsSamlProviders() throws Exception {
        String sub = "sub-" + System.nanoTime();
        Organization org = newOrganization();
        Customer admin = newCustomer(sub);
        organizationMemberService.addMember(org.getId(), admin.getId(), OrgRole.ORG_ADMIN);

        mockMvc.perform(get("/organization/me/saml-providers").with(jwt().jwt(builder -> builder.subject(sub))))
            .andExpect(status().isOk());
    }

    @Test
    void regularMemberCannotManageSamlProviders() throws Exception {
        String sub = "sub-" + System.nanoTime();
        Organization org = newOrganization();
        Customer member = newCustomer(sub);
        organizationMemberService.addMember(org.getId(), member.getId(), OrgRole.MEMBER);

        mockMvc.perform(get("/organization/me/saml-providers").with(jwt().jwt(builder -> builder.subject(sub))))
            .andExpect(status().isForbidden());
    }

    @Test
    void anonymousCannotListSamlProviders() throws Exception {
        mockMvc.perform(get("/organization/me/saml-providers")).andExpect(status().isUnauthorized());
    }
}
