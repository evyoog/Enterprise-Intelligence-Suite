package com.vyoog.eisplatform.modules.profile.service;

import com.vyoog.eisplatform.common.exception.ForbiddenException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.model.AuditLog;
import com.vyoog.eisplatform.modules.audit.repository.AuditLogRepository;
import com.vyoog.eisplatform.modules.auth.service.FakeKeycloakAdminClient;
import com.vyoog.eisplatform.modules.auth.service.KeycloakAdminClient;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Address;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.CompletionResponse;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Online;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Personal;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.ProfileDto;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.ProfileUpdateRequest;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Work;
import com.vyoog.eisplatform.modules.profile.repository.CustomerPhotoRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.model.RegistrationStatus;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.vyoog.eisplatform.modules.profile.service.ProfileTestImages.image;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * My profile (C107): the service against the real (H2) database and the in-memory Keycloak fake. The pure
 * validation, photo and completion rules have their own unit tests (ProfileValidatorTest,
 * ProfilePhotoInspectorTest, PersonProfileCompletenessTest); this class covers what needs persistence.
 */
@SpringBootTest
@ActiveProfiles("test")
@Import(ProfileServiceTest.TestConfig.class)
class ProfileServiceTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        KeycloakAdminClient fakeKeycloakAdminClient() {
            return new FakeKeycloakAdminClient();
        }
    }

    /**
     * The platform admin registry (C108) for this test class: a temp JSON file with placeholder identities.
     * The people in it hold no admin permission (USER authorities below), so only the registry can make them admins.
     */
    private static final Path REGISTRY_FILE = writeRegistry();

    private static Path writeRegistry() {
        try {
            Path file = Files.createTempFile("platform-admins-profile-service", ".json");
            file.toFile().deleteOnExit();
            Files.writeString(file, """
                {"version":1,"platformAdmins":[
                  {"email":"c108-listed-sub@profile.example","keycloakSub":"c108-sub-listed","enabled":true},
                  {"email":"C108-Listed-Email@Profile.Example","keycloakSub":"","enabled":true},
                  {"email":"c108-disabled@profile.example","keycloakSub":"c108-sub-disabled","enabled":false},
                  {"email":"c108-apikey@profile.example","keycloakSub":"c108-sub-apikey","enabled":true},
                  {"email":"c108-conflict@profile.example","keycloakSub":"c108-sub-conflict","enabled":true},
                  {"email":"c108-existing@profile.example","keycloakSub":"c108-sub-existing","enabled":true}
                ]}""");
            return file;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @DynamicPropertySource
    static void registryLocation(DynamicPropertyRegistry registry) {
        registry.add("eis.platform-admins.location", () -> "file:" + REGISTRY_FILE.toAbsolutePath());
        registry.add("eis.platform-admins.refresh-seconds", () -> "3600");
    }

    private static final List<String> ADMIN = List.of("ROLE_ADMIN");
    private static final List<String> USER = List.of("ROLE_ACTIVITY_USER");

    @Autowired
    private ProfileService service;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private OrganizationMemberRepository memberRepository;
    @Autowired
    private CustomerPhotoRepository photoRepository;
    @Autowired
    private AuditLogRepository auditLogRepository;
    @Autowired
    private KeycloakAdminClient keycloakAdminClient;

    private FakeKeycloakAdminClient keycloak() {
        return (FakeKeycloakAdminClient) keycloakAdminClient;
    }

    @BeforeEach
    void resetKeycloak() {
        keycloak().reset();
    }

    // ---- helpers ----

    private static String unique(String prefix) {
        return prefix + System.nanoTime();
    }

    private static Jwt jwt(String sub, String email, String given, String family) {
        Jwt.Builder b = Jwt.withTokenValue("token").header("alg", "none").subject(sub);
        if (email != null) b.claim("email", email);
        if (given != null) b.claim("given_name", given);
        if (family != null) b.claim("family_name", family);
        return b.build();
    }

    private Customer person(String first, String last) {
        Customer c = new Customer();
        c.setEmail(unique("p") + "@profile.example");
        c.setFirstName(first);
        c.setLastName(last);
        c.setStatus(RegistrationStatus.COMPLETED);
        c.setKeycloakSub(unique("sub-"));
        return customerRepository.save(c);
    }

    private static Jwt jwtOf(Customer c) {
        return jwt(c.getKeycloakSub(), c.getEmail(), c.getFirstName(), c.getLastName());
    }

    private static ProfileUpdateRequest request(String first, String last) {
        return new ProfileUpdateRequest(new Personal(first, last, null, null, null, null),
            new Work(null, null, null, null), new Address(null, null, null, null, null, null), new Online(null, null));
    }

    private static ProfileUpdateRequest fullRequest() {
        return new ProfileUpdateRequest(
            new Personal("  Asha ", "Rao", "Ashu", "+91 98765 43210", "+442079460958", "Engineer and mentor"),
            new Work("CTO", "Engineering", "Acme", "Software"),
            new Address("1 Main Road", "Floor 2", "Bengaluru", "Karnataka", "560001", "India"),
            new Online("https://www.linkedin.com/in/asha-rao", "https://asha.example.com"));
    }

    private List<AuditLog> audit(Customer target) {
        return auditLogRepository.findTop10ByTargetTypeAndTargetIdOrderByTimestampDesc("Customer", String.valueOf(target.getId()));
    }

    private static MockMultipartFile file(byte[] bytes, String type) {
        return new MockMultipartFile("file", "me.img", type, bytes);
    }

    // ---- own profile: read and save ----

    @Test
    void savingPersistsEveryFieldNormalisedAndReturnsTheFullProfile() {
        Customer me = person("Old", "Name");
        ProfileDto saved = service.updateMine(jwtOf(me), USER, fullRequest());

        assertThat(saved.exists()).isTrue();
        assertThat(saved.personal().firstName()).isEqualTo("Asha");
        assertThat(saved.personal().mobile()).isEqualTo("+919876543210");
        assertThat(saved.personal().alternatePhone()).isEqualTo("+442079460958");
        assertThat(saved.work().department()).isEqualTo("Engineering");
        assertThat(saved.address().postalCode()).isEqualTo("560001");
        assertThat(saved.online().linkedinUrl()).isEqualTo("https://www.linkedin.com/in/asha-rao");

        Customer stored = customerRepository.findById(me.getId()).orElseThrow();
        assertThat(stored.getPreferredName()).isEqualTo("Ashu");
        assertThat(stored.getBio()).isEqualTo("Engineer and mentor");
        assertThat(stored.getAddressLine2()).isEqualTo("Floor 2");
        assertThat(stored.getWebsiteUrl()).isEqualTo("https://asha.example.com");
        assertThat(stored.getProfileUpdatedAt()).isNotNull();
        assertThat(service.getMine(jwtOf(me), USER)).usingRecursiveComparison().isEqualTo(saved);
    }

    @Test
    void theEmailIsNeverChangedByASave() {
        Customer me = person("Asha", "Rao");
        String email = me.getEmail();
        ProfileDto saved = service.updateMine(jwtOf(me), USER, fullRequest());
        assertThat(saved.account().email()).isEqualTo(email);
        assertThat(customerRepository.findById(me.getId()).orElseThrow().getEmail()).isEqualTo(email);
    }

    @Test
    void aSaveIsAFullReplaceSoOmittedFieldsAreCleared() {
        Customer me = person("Asha", "Rao");
        service.updateMine(jwtOf(me), USER, fullRequest());
        ProfileDto cleared = service.updateMine(jwtOf(me), USER, request("Asha", "Rao"));

        assertThat(cleared.personal().mobile()).isNull();
        assertThat(cleared.work().jobTitle()).isNull();
        assertThat(cleared.address().city()).isNull();
        assertThat(cleared.online().websiteUrl()).isNull();
        assertThat(cleared.completion().filled()).isZero();
    }

    @Test
    void aFailedValidationSavesNothingAndCarriesEveryFieldError() {
        Customer me = person("Keep", "Me");
        ProfileUpdateRequest bad = new ProfileUpdateRequest(new Personal("", "R2", null, "123", null, null),
            new Work(null, null, null, null), new Address(null, null, null, null, "12", "India"), new Online(null, null));

        assertThatThrownBy(() -> service.updateMine(jwtOf(me), USER, bad))
            .isInstanceOfSatisfying(ProfileValidationException.class, e -> assertThat(e.getFieldErrors())
                .containsOnlyKeys("personal.firstName", "personal.lastName", "personal.mobile", "address.postalCode"));

        assertThat(customerRepository.findById(me.getId()).orElseThrow().getFirstName()).isEqualTo("Keep");
        assertThat(keycloak().updateUserCalls).isEmpty();
        assertThat(audit(me)).isEmpty();
    }

    @Test
    void theAccountSectionShowsRoleOrganizationAndMembership() {
        Organization org = new Organization();
        org.setName("Profile Org");
        org.setCode(unique("PRF"));
        org.setBusinessEmail("biz@profile.example");
        org.setCountry("India");
        org.setLicensedSeats(5);
        org = organizationRepository.save(org);
        Customer boss = person("Boss", "Person");
        Customer member = person("Mem", "Ber");
        Customer solo = person("Solo", "Person");
        for (Object[] row : new Object[][] {{boss, OrgRole.ORG_ADMIN}, {member, OrgRole.MEMBER}}) {
            OrganizationMember m = new OrganizationMember();
            m.setOrganizationId(org.getId());
            m.setCustomerId(((Customer) row[0]).getId());
            m.setOrgRole((OrgRole) row[1]);
            memberRepository.save(m);
        }

        ProfileDto asBoss = service.getMine(jwtOf(boss), USER);
        assertThat(asBoss.account().role()).isEqualTo("ORG_ADMIN");
        assertThat(asBoss.account().organizationName()).isEqualTo("Profile Org");
        assertThat(asBoss.account().organizationCode()).isEqualTo(org.getCode());
        assertThat(asBoss.account().status()).isEqualTo("ACTIVE");
        assertThat(asBoss.account().memberSince()).isNotNull();
        assertThat(asBoss.account().nameEditable()).isTrue();
        assertThat(service.getMine(jwtOf(member), USER).account().role()).isEqualTo("MEMBER");

        ProfileDto asSolo = service.getMine(jwtOf(solo), USER);
        assertThat(asSolo.account().role()).isEqualTo("INDIVIDUAL");
        assertThat(asSolo.account().organizationName()).isNull();
        assertThat(asSolo.account().status()).isNull();
        assertThat(asSolo.account().memberSince()).isNotNull();

        assertThat(service.getMine(jwtOf(solo), ADMIN).account().role()).isEqualTo("PLATFORM_ADMIN");
    }

    // ---- completion ----

    @Test
    void completionCountsFilledItemsAndThePhoto() {
        Customer me = person("Asha", "Rao");
        CompletionResponse start = service.completionMine(jwtOf(me));
        assertThat(start.exists()).isTrue();
        assertThat(start.percent()).isZero();
        assertThat(start.total()).isEqualTo(11);
        assertThat(start.missing()).hasSize(11).contains("photo", "country");

        service.updateMine(jwtOf(me), USER, fullRequest());
        CompletionResponse filled = service.completionMine(jwtOf(me));
        assertThat(filled.filled()).isEqualTo(10);
        assertThat(filled.percent()).isEqualTo(91);
        assertThat(filled.missing()).containsExactly("photo");

        service.putPhoto(jwtOf(me), USER, file(image("png", 16, 16), "image/png"));
        CompletionResponse done = service.completionMine(jwtOf(me));
        assertThat(done.percent()).isEqualTo(100);
        assertThat(done.missing()).isEmpty();
        assertThat(service.getMine(jwtOf(me), USER).completion().percent()).isEqualTo(100);
    }

    @Test
    void completionForASignedInPersonWithoutARecordIsExistsFalseNotAnError() {
        CompletionResponse none = service.completionMine(jwt(unique("nosub"), "nobody@profile.example", null, null));
        assertThat(none.exists()).isFalse();
        assertThat(none.percent()).isZero();
        assertThat(none.filled()).isZero();
        assertThat(none.total()).isEqualTo(11);
        assertThat(none.missing()).hasSize(11);
    }

    // ---- Keycloak mirroring ----

    @Test
    void aNameChangeIsMirroredToKeycloak() {
        Customer me = person("Old", "Name");
        service.updateMine(jwtOf(me), USER, request("New", "Surname"));

        assertThat(keycloak().updateUserCalls).containsExactly(
            new FakeKeycloakAdminClient.UpdateUserCall(me.getKeycloakSub(), "New", "Surname"));
    }

    @Test
    void savingWithoutChangingTheNameDoesNotCallKeycloak() {
        Customer me = person("Asha", "Rao");
        service.updateMine(jwtOf(me), USER, fullRequest()); // the other personal fields change, the names do not
        assertThat(keycloak().updateUserCalls).isEmpty();
    }

    @Test
    void aPersonNotLinkedToKeycloakYetIsSavedWithoutACall() {
        Customer me = person("Old", "Name");
        me.setKeycloakSub(null);
        customerRepository.save(me);
        ProfileDto admin = service.updateForAdmin(me.getId(), jwt(unique("adm"), "adm@profile.example", null, null), request("New", "Name"));
        assertThat(admin.personal().firstName()).isEqualTo("New");
        assertThat(keycloak().updateUserCalls).isEmpty();
    }

    @Test
    void ifKeycloakRefusesTheWholeSaveIsRolledBackWith502() {
        Customer me = person("Old", "Name");
        keycloak().updateUserShouldFail = true;

        assertThatThrownBy(() -> service.updateMine(jwtOf(me), USER, fullRequestWithNames("New", "Surname")))
            .isInstanceOfSatisfying(ProfileException.class, e -> assertThat(e.getStatus().value()).isEqualTo(502));

        Customer stored = customerRepository.findById(me.getId()).orElseThrow();
        assertThat(stored.getFirstName()).isEqualTo("Old");
        assertThat(stored.getLastName()).isEqualTo("Name");
        assertThat(stored.getMobile()).isNull();
        assertThat(stored.getProfileUpdatedAt()).isNull();
        assertThat(audit(me)).as("the audit record rolls back with the save").isEmpty();
    }

    private static ProfileUpdateRequest fullRequestWithNames(String first, String last) {
        ProfileUpdateRequest full = fullRequest();
        return new ProfileUpdateRequest(new Personal(first, last, "Ashu", "+919876543210", null, null), full.work(), full.address(), full.online());
    }

    // ---- platform admin without a customer row ----

    @Test
    void aPlatformAdminWithoutARecordGetsAProfileBuiltFromTheLogin() {
        ProfileDto dto = service.getMine(jwt(unique("adm"), "root@profile.example", "Root", "Admin"), ADMIN);

        assertThat(dto.exists()).isFalse();
        assertThat(dto.account().email()).isEqualTo("root@profile.example");
        assertThat(dto.account().role()).isEqualTo("PLATFORM_ADMIN");
        assertThat(dto.account().nameEditable()).isTrue();
        assertThat(dto.personal().firstName()).isEqualTo("Root");
        assertThat(dto.personal().lastName()).isEqualTo("Admin");
        assertThat(dto.personal().mobile()).isNull();
        assertThat(dto.photo().present()).isFalse();
        assertThat(dto.completion().percent()).isZero();
        assertThat(dto.completion().total()).isEqualTo(11);
        assertThat(dto.completion().missing()).hasSize(11);
    }

    @Test
    void aPlatformAdminCreatesTheirOwnRecordOnFirstSave() {
        String sub = unique("adm");
        String email = unique("root") + "@profile.example";
        Jwt jwt = jwt(sub, email, "Root", "Admin");

        ProfileDto created = service.updateMine(jwt, ADMIN, request("Root", "Admin"));

        assertThat(created.exists()).isTrue();
        assertThat(created.account().role()).isEqualTo("PLATFORM_ADMIN");
        Customer row = customerRepository.findByKeycloakSub(sub).orElseThrow();
        assertThat(row.getEmail()).isEqualTo(email);
        assertThat(row.getStatus()).isEqualTo(RegistrationStatus.COMPLETED);
        assertThat(audit(row)).extracting(AuditLog::getAction).containsExactly("PROFILE_CREATED");
        assertThat(keycloak().updateUserCalls).as("names equal the login's own, nothing to mirror").isEmpty();
        assertThat(service.getMine(jwt, ADMIN).exists()).isTrue();
    }

    @Test
    void aPlatformAdminWhoChangesTheirNameWhileCreatingTheRecordIsMirrored() {
        String sub = unique("adm");
        service.updateMine(jwt(sub, unique("root") + "@profile.example", "Root", "Admin"), ADMIN, request("Rootie", "Admin"));
        assertThat(keycloak().updateUserCalls).containsExactly(new FakeKeycloakAdminClient.UpdateUserCall(sub, "Rootie", "Admin"));
    }

    @Test
    void aPlatformAdminWhoseEmailBelongsToAnotherLoginGets409AndNothingIsCreated() {
        Customer other = person("Other", "Login");
        String sub = unique("adm");

        assertThatThrownBy(() -> service.updateMine(jwt(sub, other.getEmail().toUpperCase(), "Root", "Admin"), ADMIN, request("Root", "Admin")))
            .isInstanceOfSatisfying(ProfileException.class, e -> assertThat(e.getStatus().value()).isEqualTo(409));
        assertThat(customerRepository.findByKeycloakSub(sub)).isEmpty();
        assertThat(customerRepository.findById(other.getId()).orElseThrow().getKeycloakSub()).isNotEqualTo(sub);
    }

    @Test
    void aLoginWithoutAnEmailCannotCreateARecord() {
        assertThatThrownBy(() -> service.updateMine(jwt(unique("adm"), null, "Root", "Admin"), ADMIN, request("Root", "Admin")))
            .isInstanceOfSatisfying(ProfileException.class, e -> assertThat(e.getStatus().value()).isEqualTo(400));
    }

    @Test
    void aPlatformAdminWithoutARecordCannotAddAPhotoYet() {
        Jwt jwt = jwt(unique("adm"), "x@profile.example", "R", "A");
        assertThatThrownBy(() -> service.putPhoto(jwt, ADMIN, file(image("png", 8, 8), "image/png")))
            .isInstanceOfSatisfying(ProfileException.class, e -> assertThat(e.getStatus().value()).isEqualTo(409));
        assertThatThrownBy(() -> service.getPhoto(jwt)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ---- platform admin registry (C108): listed logins without the admin permission ----

    private static Jwt verifiedJwt(String sub, String email) {
        return Jwt.withTokenValue("token").header("alg", "none").subject(sub)
            .claim("email", email).claim("email_verified", true).claim("given_name", "Reg").claim("family_name", "Admin").build();
    }

    @Test
    void aListedAdminWithoutTheAdminPermissionGetsExistsFalseCreatesTheOwnRecordThenAddsAPhoto() {
        String email = "c108-created@profile.example";
        // listed by Keycloak sub; the token email is a different, unlisted one (sub alone is enough)
        Jwt jwt = verifiedJwt("c108-sub-listed", email);

        ProfileDto before = service.getMine(jwt, USER);
        assertThat(before.exists()).isFalse();
        assertThat(before.account().role()).isEqualTo("PLATFORM_ADMIN");
        assertThat(before.account().email()).isEqualTo(email);
        assertThat(service.completionMine(jwt).exists()).isFalse();
        assertThatThrownBy(() -> service.putPhoto(jwt, USER, file(image("png", 8, 8), "image/png")))
            .isInstanceOfSatisfying(ProfileException.class, e -> assertThat(e.getStatus().value()).isEqualTo(409));

        ProfileDto created = service.updateMine(jwt, USER, request("Reg", "Admin"));
        assertThat(created.exists()).isTrue();
        assertThat(created.account().role()).isEqualTo("PLATFORM_ADMIN");
        Customer row = customerRepository.findByKeycloakSub("c108-sub-listed").orElseThrow();
        assertThat(row.getEmail()).isEqualTo(email);
        assertThat(audit(row)).extracting(AuditLog::getAction).containsExactly("PROFILE_CREATED");

        ProfileDto withPhoto = service.putPhoto(jwt, USER, file(image("png", 8, 8), "image/png"));
        assertThat(withPhoto.photo().present()).isTrue();
        assertThat(withPhoto.account().role()).isEqualTo("PLATFORM_ADMIN");
        assertThat(service.getMine(jwt, USER).exists()).isTrue();
        assertThat(service.removePhoto(jwt, USER).photo().present()).isFalse();
    }

    @Test
    void aListedAdminMatchedByVerifiedEmailCreatesTheirRecordButAnUnverifiedEmailDoesNot() {
        String email = "C108-LISTED-EMAIL@profile.example"; // different case from the file on purpose
        Jwt unverified = Jwt.withTokenValue("token").header("alg", "none").subject("c108-sub-by-email-unverified")
            .claim("email", email).claim("email_verified", false).build();
        assertThatThrownBy(() -> service.getMine(unverified, USER))
            .isInstanceOf(ResourceNotFoundException.class).hasMessage("No Vyoog account is linked to this login");

        Jwt verified = verifiedJwt("c108-sub-by-email", email);
        assertThat(service.getMine(verified, USER).account().role()).isEqualTo("PLATFORM_ADMIN");
        assertThat(service.updateMine(verified, USER, request("Reg", "Admin")).exists()).isTrue();
        assertThat(customerRepository.findByKeycloakSub("c108-sub-by-email")).isPresent();
    }

    @Test
    void aLoginThatIsNeitherLinkedNorListedStillGets404WithTheStandardMessage() {
        Jwt ghost = verifiedJwt(unique("c108-unlisted"), "c108-unlisted@profile.example");
        assertThatThrownBy(() -> service.getMine(ghost, USER))
            .isInstanceOf(ResourceNotFoundException.class).hasMessage("No Vyoog account is linked to this login");
        assertThatThrownBy(() -> service.updateMine(ghost, USER, request("Gh", "Ost")))
            .isInstanceOf(ResourceNotFoundException.class).hasMessage("No Vyoog account is linked to this login");
        assertThat(customerRepository.findByEmailIgnoreCase("c108-unlisted@profile.example")).isEmpty();
    }

    @Test
    void aDisabledRegistryEntryStillGets404() {
        Jwt disabled = verifiedJwt("c108-sub-disabled", "c108-disabled@profile.example");
        assertThatThrownBy(() -> service.getMine(disabled, USER))
            .isInstanceOf(ResourceNotFoundException.class).hasMessage("No Vyoog account is linked to this login");
        assertThatThrownBy(() -> service.updateMine(disabled, USER, request("Dis", "Abled"))).isInstanceOf(ResourceNotFoundException.class);
        assertThat(customerRepository.findByKeycloakSub("c108-sub-disabled")).isEmpty();
    }

    @Test
    void aListedAdminWhoseEmailBelongsToAnotherLoginStillGets409AndNothingIsCreated() {
        Customer other = person("Other", "Login");
        Jwt listedWithTakenEmail = Jwt.withTokenValue("token").header("alg", "none").subject("c108-sub-conflict")
            .claim("email", other.getEmail()).claim("email_verified", true).build();
        assertThatThrownBy(() -> service.updateMine(listedWithTakenEmail, USER, request("Reg", "Admin")))
            .isInstanceOfSatisfying(ProfileException.class, e -> assertThat(e.getStatus().value()).isEqualTo(409));
        assertThat(customerRepository.findByKeycloakSub("c108-sub-conflict")).isEmpty();
    }

    @Test
    void aListedAdminWithAnApiKeyClaimIsForbiddenOnEveryOwnProfileCall() {
        Jwt apiKey = Jwt.withTokenValue("t").header("alg", "none").subject("c108-sub-apikey")
            .claim("email", "c108-apikey@profile.example").claim("email_verified", true).claim("api_key_id", "9").build();
        assertThatThrownBy(() -> service.getMine(apiKey, USER)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.updateMine(apiKey, USER, request("A", "B"))).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.putPhoto(apiKey, USER, file(image("png", 8, 8), "image/png"))).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.removePhoto(apiKey, USER)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.completionMine(apiKey)).isInstanceOf(ForbiddenException.class);
        assertThat(customerRepository.findByKeycloakSub("c108-sub-apikey")).isEmpty();
    }

    @Test
    void aListedAdminWhoAlreadyHasARecordIsShownAsPlatformAdminWhileAnUnlistedPersonIsNot() {
        Customer listed = person("Li", "Sted");
        listed.setKeycloakSub("c108-sub-existing");
        listed.setEmail("c108-existing@profile.example");
        customerRepository.save(listed);
        Customer plain = person("Plain", "Person");

        assertThat(service.getMine(jwtOf(listed), USER).account().role()).isEqualTo("PLATFORM_ADMIN");
        assertThat(service.getMine(jwtOf(plain), USER).account().role()).isEqualTo("INDIVIDUAL");
    }

    // ---- a non-admin without a record ----

    @Test
    void aNonAdminWithoutARecordGets404OnEveryOwnProfileCall() {
        Jwt ghost = jwt(unique("ghost"), "ghost@profile.example", "Gh", "Ost");
        assertThatThrownBy(() -> service.getMine(ghost, USER)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.updateMine(ghost, USER, request("Gh", "Ost"))).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.putPhoto(ghost, USER, file(image("png", 8, 8), "image/png"))).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.removePhoto(ghost, USER)).isInstanceOf(ResourceNotFoundException.class);
        assertThat(customerRepository.findByEmailIgnoreCase("ghost@profile.example")).isEmpty();
    }

    // ---- isolation between people ----

    @Test
    void aPersonCanNeverReadOrWriteAnotherPersonsProfile() {
        Customer alice = person("Alice", "One");
        Customer bob = person("Bob", "Two");
        service.updateMine(jwtOf(bob), USER, fullRequest());
        service.putPhoto(jwtOf(bob), USER, file(image("png", 8, 8), "image/png"));

        // Alice's token only ever reaches Alice's row: there is no id to pass.
        ProfileDto aliceView = service.getMine(jwtOf(alice), USER);
        assertThat(aliceView.account().email()).isEqualTo(alice.getEmail());
        assertThat(aliceView.personal().firstName()).isEqualTo("Alice");
        assertThat(aliceView.photo().present()).isFalse();
        assertThatThrownBy(() -> service.getPhoto(jwtOf(alice))).isInstanceOf(ResourceNotFoundException.class);

        service.updateMine(jwtOf(alice), USER, request("Alicia", "One"));
        service.removePhoto(jwtOf(alice), USER);

        Customer bobAfter = customerRepository.findById(bob.getId()).orElseThrow();
        assertThat(bobAfter.getFirstName()).isEqualTo("Asha");
        assertThat(bobAfter.getMobile()).isEqualTo("+919876543210");
        assertThat(photoRepository.existsById(bob.getId())).isTrue();
        assertThat(customerRepository.findById(alice.getId()).orElseThrow().getFirstName()).isEqualTo("Alicia");
    }

    @Test
    void anApiKeyIsNeverAPersonAndCannotUseTheProfile() {
        Customer me = person("Asha", "Rao");
        Jwt apiKey = Jwt.withTokenValue("t").header("alg", "none").subject(me.getKeycloakSub())
            .claim("email", me.getEmail()).claim("api_key_id", "7").build();
        assertThatThrownBy(() -> service.getMine(apiKey, USER)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.updateMine(apiKey, USER, request("X", "Y"))).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.completionMine(apiKey)).isInstanceOf(ForbiddenException.class);
    }

    // ---- platform admin edits another person ----

    @Test
    void anAdminEditOfAnotherPersonIsSavedAndAuditedWithTheAdminAsActor() {
        Customer target = person("Target", "Person");
        Customer adminRow = person("Adm", "In");
        Jwt adminJwt = jwtOf(adminRow);

        ProfileDto updated = service.updateForAdmin(target.getId(), adminJwt, fullRequest());

        assertThat(updated.personal().firstName()).isEqualTo("Asha");
        assertThat(updated.account().email()).isEqualTo(target.getEmail());
        assertThat(customerRepository.findById(target.getId()).orElseThrow().getCity()).isEqualTo("Bengaluru");
        List<AuditLog> entries = audit(target);
        assertThat(entries).hasSize(1);
        AuditLog entry = entries.get(0);
        assertThat(entry.getAction()).isEqualTo("PROFILE_UPDATED_BY_ADMIN");
        assertThat(entry.getActorKeycloakSub()).isEqualTo(adminRow.getKeycloakSub());
        assertThat(entry.getActorCustomerId()).isEqualTo(adminRow.getId());
        assertThat(entry.getTargetId()).isEqualTo(String.valueOf(target.getId()));
        assertThat(entry.getDetail()).contains("personal", "work", "address", "online");
        // the person's own Keycloak user gets the new name, never the admin's
        assertThat(keycloak().updateUserCalls).containsExactly(
            new FakeKeycloakAdminClient.UpdateUserCall(target.getKeycloakSub(), "Asha", "Rao"));
    }

    @Test
    void anAdminWithoutACustomerRowCanStillEditAndIsAuditedByLogin() {
        Customer target = person("Target", "Person");
        String adminSub = unique("adm");
        service.updateForAdmin(target.getId(), jwt(adminSub, "adm@profile.example", null, null), request("Target", "Changed"));
        AuditLog entry = audit(target).get(0);
        assertThat(entry.getActorKeycloakSub()).isEqualTo(adminSub);
        assertThat(entry.getActorCustomerId()).isNull();
        assertThat(entry.getActorEmail()).isEqualTo("adm@profile.example");
    }

    @Test
    void anAdminReadAndEditOfAnUnknownCustomerIs404AndInvalidInputIsRejected() {
        Jwt adminJwt = jwt(unique("adm"), "adm@profile.example", null, null);
        assertThatThrownBy(() -> service.getForAdmin(987_654_321L)).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> service.updateForAdmin(987_654_321L, adminJwt, request("A", "B"))).isInstanceOf(ResourceNotFoundException.class);
        Customer target = person("Target", "Person");
        assertThatThrownBy(() -> service.updateForAdmin(target.getId(), adminJwt, request("", "")))
            .isInstanceOf(ProfileValidationException.class);
        assertThat(service.getForAdmin(target.getId()).account().email()).isEqualTo(target.getEmail());
    }

    // ---- audit ----

    @Test
    void auditRecordsNameTheChangedSectionsAndNeverTheValues() {
        Customer me = person("Asha", "Rao");
        service.updateMine(jwtOf(me), USER, fullRequestWithNames("Asha", "Rao"));
        service.updateMine(jwtOf(me), USER, fullRequestWithNames("Asha", "Rao")); // identical: nothing changed, nothing audited

        List<AuditLog> entries = audit(me);
        assertThat(entries).hasSize(1);
        AuditLog entry = entries.get(0);
        assertThat(entry.getAction()).isEqualTo("PROFILE_UPDATED");
        assertThat(entry.getActorKeycloakSub()).isEqualTo(me.getKeycloakSub());
        assertThat(entry.getDetail()).contains("personal").doesNotContain("Ashu").doesNotContain("+9198").doesNotContain("Acme")
            .doesNotContain("Bengaluru").doesNotContain("linkedin");

        ProfileUpdateRequest base = fullRequestWithNames("Asha", "Rao");
        service.updateMine(jwtOf(me), USER, new ProfileUpdateRequest(base.personal(),
            new Work("CEO", "Engineering", "Acme", "Software"), base.address(), base.online()));
        assertThat(audit(me).get(0).getDetail()).isEqualTo("sections changed: work");
    }

    // ---- photo ----

    @Test
    void aPhotoCanBeAddedReplacedAndRemovedWithAuditRecords() {
        Customer me = person("Asha", "Rao");
        byte[] first = image("png", 16, 16);
        byte[] second = image("jpg", 24, 24);

        ProfileDto added = service.putPhoto(jwtOf(me), USER, file(first, "image/png"));
        assertThat(added.photo().present()).isTrue();
        assertThat(added.photo().updatedAt()).isNotNull();
        ProfileService.PhotoContent stored = service.getPhoto(jwtOf(me));
        assertThat(stored.contentType()).isEqualTo("image/png");
        assertThat(stored.data()).isEqualTo(first);
        assertThat(stored.etag()).startsWith("\"");

        service.putPhoto(jwtOf(me), USER, file(second, "image/jpeg"));
        assertThat(photoRepository.count()).isGreaterThanOrEqualTo(1);
        ProfileService.PhotoContent replaced = service.getPhoto(jwtOf(me));
        assertThat(replaced.contentType()).isEqualTo("image/jpeg");
        assertThat(replaced.data()).isEqualTo(second);
        assertThat(photoRepository.findMetaByCustomerId(me.getId()).orElseThrow().getSizeBytes()).isEqualTo(second.length);

        ProfileDto removed = service.removePhoto(jwtOf(me), USER);
        assertThat(removed.photo().present()).isFalse();
        assertThat(removed.photo().updatedAt()).isNull();
        assertThatThrownBy(() -> service.getPhoto(jwtOf(me))).isInstanceOf(ResourceNotFoundException.class);
        service.removePhoto(jwtOf(me), USER); // removing nothing is not an error and is not audited again

        assertThat(audit(me)).extracting(AuditLog::getAction)
            .containsExactlyInAnyOrder("PROFILE_PHOTO_UPDATED", "PROFILE_PHOTO_UPDATED", "PROFILE_PHOTO_REMOVED");
    }

    @Test
    void aWrongTypeOversizeOrDisguisedPhotoIsRefusedAndChangesNothing() {
        Customer me = person("Asha", "Rao");
        byte[] good = image("png", 16, 16);
        service.putPhoto(jwtOf(me), USER, file(good, "image/png"));

        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertThatThrownBy(() -> service.putPhoto(jwtOf(me), USER, file(svg, "image/svg+xml")))
            .isInstanceOfSatisfying(ProfileException.class, e -> assertThat(e.getStatus().value()).isEqualTo(400));
        assertThatThrownBy(() -> service.putPhoto(jwtOf(me), USER, file(svg, "image/png")))
            .isInstanceOfSatisfying(ProfileException.class, e -> assertThat(e.getStatus().value()).isEqualTo(400));
        assertThatThrownBy(() -> service.putPhoto(jwtOf(me), USER, file(new byte[ProfilePhotoInspector.MAX_BYTES + 1], "image/png")))
            .isInstanceOfSatisfying(ProfileException.class, e -> assertThat(e.getStatus().value()).isEqualTo(413));
        assertThatThrownBy(() -> service.putPhoto(jwtOf(me), USER, file(new byte[0], "image/png")))
            .isInstanceOfSatisfying(ProfileException.class, e -> assertThat(e.getStatus().value()).isEqualTo(400));
        assertThatThrownBy(() -> service.putPhoto(jwtOf(me), USER, null))
            .isInstanceOfSatisfying(ProfileException.class, e -> assertThat(e.getStatus().value()).isEqualTo(400));

        assertThat(service.getPhoto(jwtOf(me)).data()).isEqualTo(good);
        assertThat(audit(me)).hasSize(1);
    }
}
