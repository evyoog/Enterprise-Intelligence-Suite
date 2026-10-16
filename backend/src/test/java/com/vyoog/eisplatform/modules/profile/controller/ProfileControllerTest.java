package com.vyoog.eisplatform.modules.profile.controller;

import com.vyoog.eisplatform.modules.auth.service.FakeKeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static com.vyoog.eisplatform.modules.profile.service.ProfileTestImages.image;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * My profile (C107) over real HTTP through the real filter chain: authentication, the admin permission,
 * the validation error body, and the photo endpoints' bytes and headers.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(ProfileControllerTest.TestConfig.class)
class ProfileControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        KeycloakAdminClient fakeKeycloakAdminClient() {
            return new FakeKeycloakAdminClient();
        }
    }

    private static final String VALID_BODY = """
        {"personal":{"firstName":"Asha","lastName":"Rao","preferredName":"Ashu","mobile":"+91 98765 43210"},
         "work":{"jobTitle":"CTO","department":"Eng","companyName":"Acme","industry":"Software"},
         "address":{"city":"Bengaluru","postalCode":"560001","country":"India"},
         "online":{"websiteUrl":"https://asha.example.com"}}""";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private KeycloakAdminClient keycloakAdminClient;

    @BeforeEach
    void reset() {
        ((FakeKeycloakAdminClient) keycloakAdminClient).reset();
    }

    private Customer person() {
        Customer c = new Customer();
        c.setEmail("c" + System.nanoTime() + "@profile.example");
        c.setFirstName("Pat");
        c.setLastName("Person");
        c.setStatus(RegistrationStatus.COMPLETED);
        c.setKeycloakSub("sub-" + System.nanoTime());
        return customerRepository.save(c);
    }

    private static RequestPostProcessor as(Customer c) {
        return jwt().jwt(j -> j.subject(c.getKeycloakSub()).claim("email", c.getEmail()))
            .authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER"));
    }

    private static RequestPostProcessor admin() {
        return jwt().jwt(j -> j.subject("sub-admin-" + "x").claim("email", "admin@profile.example").claim("given_name", "Ad").claim("family_name", "Min"))
            .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static RequestPostProcessor newAdmin(String sub, String email) {
        return jwt().jwt(j -> j.subject(sub).claim("email", email).claim("given_name", "Ad").claim("family_name", "Min"))
            .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    private static MockMultipartFile upload(byte[] bytes, String type) {
        return new MockMultipartFile("file", "me.img", type, bytes);
    }

    // ---- authentication and permissions ----

    @Test
    void everyProfileEndpointNeedsASignedInCaller() throws Exception {
        mockMvc.perform(get("/me/profile")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/me/profile").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/me/profile/completion")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/me/profile/photo")).andExpect(status().isUnauthorized());
        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/photo").file(upload(image("png", 8, 8), "image/png")))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(delete("/me/profile/photo")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/admin/registrations/individuals/1/profile")).andExpect(status().isUnauthorized());
        mockMvc.perform(put("/admin/registrations/individuals/1/profile").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void theAdminEndpointsNeedTheRegistrationsPermission() throws Exception {
        Customer target = person();
        Customer user = person();
        mockMvc.perform(get("/admin/registrations/individuals/" + target.getId() + "/profile").with(as(user)))
            .andExpect(status().isForbidden());
        mockMvc.perform(put("/admin/registrations/individuals/" + target.getId() + "/profile").with(as(user))
            .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY)).andExpect(status().isForbidden());

        mockMvc.perform(get("/admin/registrations/individuals/" + target.getId() + "/profile").with(admin()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.account.email").value(target.getEmail()))
            .andExpect(jsonPath("$.personal.firstName").value("Pat"));
        mockMvc.perform(put("/admin/registrations/individuals/" + target.getId() + "/profile").with(admin())
            .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.personal.firstName").value("Asha"))
            .andExpect(jsonPath("$.address.city").value("Bengaluru"));
        mockMvc.perform(get("/admin/registrations/individuals/987654321/profile").with(admin())).andExpect(status().isNotFound());
    }

    @Test
    void theAdminEndpointReturnsTheSameValidationBody() throws Exception {
        Customer target = person();
        mockMvc.perform(put("/admin/registrations/individuals/" + target.getId() + "/profile").with(admin())
            .contentType(MediaType.APPLICATION_JSON).content("{\"personal\":{\"firstName\":\"\",\"lastName\":\"Rao\"}}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.fieldErrors['personal.firstName']").value("This field is required"));
    }

    // ---- reading and saving ----

    @Test
    void getReturnsTheCallersOwnProfileWithExplicitNulls() throws Exception {
        Customer me = person();
        mockMvc.perform(get("/me/profile").with(as(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(true))
            .andExpect(jsonPath("$.account.email").value(me.getEmail()))
            .andExpect(jsonPath("$.account.role").value("INDIVIDUAL"))
            .andExpect(jsonPath("$.account.organizationName").value(nullValue()))
            .andExpect(jsonPath("$.account.nameEditable").value(true))
            .andExpect(jsonPath("$.personal.firstName").value("Pat"))
            .andExpect(jsonPath("$.personal.bio").value(nullValue()))
            .andExpect(jsonPath("$.work.jobTitle").value(nullValue()))
            .andExpect(jsonPath("$.photo.present").value(false))
            .andExpect(jsonPath("$.photo.updatedAt").value(nullValue()))
            .andExpect(jsonPath("$.completion.total").value(11))
            .andExpect(jsonPath("$.completion.percent").value(0));
    }

    @Test
    void putSavesAndNormalisesAndTheCompletionEndpointFollows() throws Exception {
        Customer me = person();
        mockMvc.perform(put("/me/profile").with(as(me)).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.personal.mobile").value("+919876543210"))
            .andExpect(jsonPath("$.completion.filled").value(8));
        mockMvc.perform(get("/me/profile/completion").with(as(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(true))
            .andExpect(jsonPath("$.percent").value(73))
            .andExpect(jsonPath("$.filled").value(8))
            .andExpect(jsonPath("$.total").value(11))
            .andExpect(jsonPath("$.missing.length()").value(3));
    }

    @Test
    void invalidInputIs400WithOneFieldErrorPerBadField() throws Exception {
        Customer me = person();
        String body = """
            {"personal":{"firstName":"A<b>","lastName":"Rao","mobile":"12345"},
             "address":{"postalCode":"123","country":"India"},
             "online":{"linkedinUrl":"http://linkedin.com/in/x","websiteUrl":"nope"}}""";
        mockMvc.perform(put("/me/profile").with(as(me)).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Validation failed"))
            .andExpect(jsonPath("$.fieldErrors['personal.firstName']").exists())
            .andExpect(jsonPath("$.fieldErrors['personal.mobile']").exists())
            .andExpect(jsonPath("$.fieldErrors['address.postalCode']").value("A postal code in India has exactly 6 digits"))
            .andExpect(jsonPath("$.fieldErrors['online.linkedinUrl']").exists())
            .andExpect(jsonPath("$.fieldErrors['online.websiteUrl']").exists())
            .andExpect(jsonPath("$.fieldErrors['personal.lastName']").doesNotExist());
        mockMvc.perform(get("/me/profile").with(as(me))).andExpect(jsonPath("$.personal.firstName").value("Pat"));
    }

    @Test
    void keycloakFailureIs502WithTheStandardBody() throws Exception {
        Customer me = person();
        ((FakeKeycloakAdminClient) keycloakAdminClient).updateUserShouldFail = true;
        mockMvc.perform(put("/me/profile").with(as(me)).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isBadGateway())
            .andExpect(jsonPath("$.status").value(502))
            .andExpect(jsonPath("$.message").exists());
        assertThat(customerRepository.findById(me.getId()).orElseThrow().getFirstName()).isEqualTo("Pat");
    }

    @Test
    void aNonAdminWithoutARecordGets404AndAPlatformAdminGetsExistsFalseThenCreatesTheRecord() throws Exception {
        String suffix = String.valueOf(System.nanoTime());
        mockMvc.perform(get("/me/profile").with(jwt().jwt(j -> j.subject("ghost-" + suffix).claim("email", "ghost" + suffix + "@profile.example"))
            .authorities(new SimpleGrantedAuthority("ROLE_ACTIVITY_USER")))).andExpect(status().isNotFound());

        RequestPostProcessor root = newAdmin("root-" + suffix, "root" + suffix + "@profile.example");
        mockMvc.perform(get("/me/profile").with(root))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(false))
            .andExpect(jsonPath("$.account.role").value("PLATFORM_ADMIN"))
            .andExpect(jsonPath("$.personal.firstName").value("Ad"))
            .andExpect(jsonPath("$.completion.percent").value(0));
        mockMvc.perform(get("/me/profile/completion").with(root))
            .andExpect(status().isOk()).andExpect(jsonPath("$.exists").value(false));

        mockMvc.perform(put("/me/profile").with(root).contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.exists").value(true));
        mockMvc.perform(get("/me/profile").with(root)).andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void anAdminWhoseEmailBelongsToAnotherLoginGets409() throws Exception {
        Customer other = person();
        mockMvc.perform(put("/me/profile").with(newAdmin("someone-else-" + System.nanoTime(), other.getEmail()))
            .contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void otherErrorsKeepThePlatformsStandardBodyOutsideTheProfileControllers() throws Exception {
        // A malformed JSON body is not a profile error: it keeps the platform's generic handling (not fieldErrors).
        Customer me = person();
        MvcResult result = mockMvc.perform(put("/me/profile").with(as(me)).contentType(MediaType.APPLICATION_JSON).content("{not json"))
            .andReturn();
        assertThat(result.getResponse().getContentAsString()).doesNotContain("fieldErrors");
    }

    // ---- photo ----

    @Test
    void thePhotoIsUploadedServedWithItsHeadersAndRemoved() throws Exception {
        Customer me = person();
        mockMvc.perform(get("/me/profile/photo").with(as(me))).andExpect(status().isNotFound());

        byte[] png = image("png", 32, 32);
        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/photo").file(upload(png, "image/png")).with(as(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.photo.present").value(true))
            .andExpect(jsonPath("$.completion.missing").value(org.hamcrest.Matchers.hasItem("mobile")));

        MvcResult served = mockMvc.perform(get("/me/profile/photo").with(as(me)))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
            .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=0, must-revalidate, private"))
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(header().exists(HttpHeaders.ETAG))
            .andReturn();
        assertThat(served.getResponse().getContentAsByteArray()).isEqualTo(png);

        String etag = served.getResponse().getHeader(HttpHeaders.ETAG);
        mockMvc.perform(get("/me/profile/photo").with(as(me)).header(HttpHeaders.IF_NONE_MATCH, etag))
            .andExpect(status().isNotModified());

        mockMvc.perform(delete("/me/profile/photo").with(as(me)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.photo.present").value(false));
        mockMvc.perform(get("/me/profile/photo").with(as(me))).andExpect(status().isNotFound());
    }

    @Test
    void aPhotoIsNeverServedToAnotherPerson() throws Exception {
        Customer owner = person();
        Customer other = person();
        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/photo").file(upload(image("jpg", 16, 16), "image/jpeg")).with(as(owner)))
            .andExpect(status().isOk());
        mockMvc.perform(get("/me/profile/photo").with(as(other))).andExpect(status().isNotFound());
        mockMvc.perform(get("/me/profile/photo").with(as(owner))).andExpect(status().isOk());
    }

    @Test
    void badPhotosAreRefusedWithTheRightStatus() throws Exception {
        Customer me = person();
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/photo").file(upload(svg, "image/svg+xml")).with(as(me)))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").exists());
        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/photo").file(upload(svg, "image/png")).with(as(me)))
            .andExpect(status().isBadRequest());
        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/photo")
            .file(upload(new byte[1_048_577], "image/png")).with(as(me)))
            .andExpect(status().isPayloadTooLarge()).andExpect(jsonPath("$.status").value(413));
        mockMvc.perform(multipart(HttpMethod.PUT, "/me/profile/photo").file(upload(image("png", 4097, 8), "image/png")).with(as(me)))
            .andExpect(status().isBadRequest());
        mockMvc.perform(get("/me/profile/photo").with(as(me))).andExpect(status().isNotFound());
    }
}
