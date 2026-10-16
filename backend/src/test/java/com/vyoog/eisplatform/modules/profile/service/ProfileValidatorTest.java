package com.vyoog.eisplatform.modules.profile.service;

import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Address;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Online;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Personal;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.ProfileUpdateRequest;
import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Work;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** My profile (C107): every validation and normalisation rule of the form, in isolation (no Spring, no database). */
class ProfileValidatorTest {

    private final ProfileValidator validator = new ProfileValidator();

    private static ProfileUpdateRequest request(Personal p, Work w, Address a, Online o) {
        return new ProfileUpdateRequest(p, w, a, o);
    }

    private static Personal person(String first, String last) {
        return new Personal(first, last, null, null, null, null);
    }

    private static ProfileUpdateRequest names(String first, String last) {
        return request(person(first, last), null, null, null);
    }

    private static ProfileUpdateRequest withMobile(String mobile) {
        return request(new Personal("Asha", "Rao", null, mobile, null, null), null, null, null);
    }

    private static ProfileUpdateRequest withAddress(Address address) {
        return request(person("Asha", "Rao"), null, address, null);
    }

    private static ProfileUpdateRequest withOnline(String linkedin, String website) {
        return request(person("Asha", "Rao"), null, null, new Online(linkedin, website));
    }

    private Map<String, String> errors(ProfileUpdateRequest request) {
        return ((ProfileValidationException) org.junit.jupiter.api.Assertions.assertThrows(ProfileValidationException.class,
            () -> validator.validate(request))).getFieldErrors();
    }

    @Test
    void anEmptyProfileOnlyNeedsFirstAndLastName() {
        ProfileUpdateRequest ok = validator.validate(names("Asha", "Rao"));
        assertThat(ok.personal().firstName()).isEqualTo("Asha");
        assertThat(ok.work().jobTitle()).isNull();
        assertThat(ok.address().country()).isNull();
        assertThat(ok.online().websiteUrl()).isNull();
    }

    @Test
    void firstAndLastNameAreRequiredAndBlankCountsAsMissing() {
        assertThat(errors(names(null, "  "))).containsEntry("personal.firstName", "This field is required")
            .containsEntry("personal.lastName", "This field is required");
        assertThat(errors(new ProfileUpdateRequest(null, null, null, null)))
            .containsKeys("personal.firstName", "personal.lastName");
    }

    @Test
    void textIsTrimmedAndInnerWhitespaceCollapsed() {
        ProfileUpdateRequest ok = validator.validate(request(
            new Personal("  Asha \t Devi ", " Rao ", "  ", null, null, "Line one\n\nline   two"),
            new Work("  CTO  ", " \t ", null, null), null, null));
        assertThat(ok.personal().firstName()).isEqualTo("Asha Devi");
        assertThat(ok.personal().lastName()).isEqualTo("Rao");
        assertThat(ok.personal().preferredName()).isNull();
        assertThat(ok.personal().bio()).isEqualTo("Line one line two");
        assertThat(ok.work().jobTitle()).isEqualTo("CTO");
        assertThat(ok.work().department()).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Asha", "Mary-Jane", "O'Brien", "O’Brien", "Dr. Rao", "José", "குமார்"})
    void namesAcceptLettersMarksSpacesAndPunctuation(String name) {
        assertThat(validator.validate(names(name, "Rao")).personal().firstName()).isEqualTo(name);
    }

    @ParameterizedTest
    @ValueSource(strings = {"R2D2", "-Asha", "Asha_", "As@ha", "1Asha", ".Asha", "Asha!"})
    void namesRejectDigitsSymbolsAndABadFirstCharacter(String name) {
        assertThat(errors(names(name, "Rao"))).containsKey("personal.firstName");
    }

    @Test
    void nameLengthLimitsAreEnforced() {
        assertThat(validator.validate(names("A".repeat(100), "B".repeat(100))).personal().firstName()).hasSize(100);
        assertThat(errors(names("A".repeat(101), "Rao"))).containsEntry("personal.firstName", "Must be at most 100 characters");
        Personal preferred = new Personal("Asha", "Rao", "P".repeat(61), null, null, null);
        assertThat(errors(request(preferred, null, null, null))).containsKey("personal.preferredName");
        assertThat(validator.validate(request(new Personal("Asha", "Rao", "P".repeat(60), null, null, null), null, null, null))
            .personal().preferredName()).hasSize(60);
    }

    @Test
    void preferredNameFollowsTheSameCharacterRuleButIsOptional() {
        Personal bad = new Personal("Asha", "Rao", "Ash4", null, null, null);
        assertThat(errors(request(bad, null, null, null))).containsKey("personal.preferredName");
        Personal ok = new Personal("Asha", "Rao", "Ashu", null, null, null);
        assertThat(validator.validate(request(ok, null, null, null)).personal().preferredName()).isEqualTo("Ashu");
    }

    @Test
    void bioIsLimitedTo500Characters() {
        Personal max = new Personal("Asha", "Rao", null, null, null, "b".repeat(500));
        assertThat(validator.validate(request(max, null, null, null)).personal().bio()).hasSize(500);
        Personal over = new Personal("Asha", "Rao", null, null, null, "b".repeat(501));
        assertThat(errors(request(over, null, null, null))).containsEntry("personal.bio", "Must be at most 500 characters");
    }

    @Test
    void angleBracketsAreRejectedInEveryFreeTextField() {
        ProfileUpdateRequest all = request(
            new Personal("<b>Asha", "Rao>", "<x>", null, null, "hello <script>"),
            new Work("a<b", "d>e", "<c>", "<i>"),
            new Address("<1", "2>", "<c>", "<s>", null, "<country>"),
            null);
        assertThat(errors(all)).containsKeys("personal.firstName", "personal.lastName", "personal.preferredName",
            "personal.bio", "work.jobTitle", "work.department", "work.companyName", "work.industry",
            "address.addressLine1", "address.addressLine2", "address.city", "address.state", "address.country");
        assertThat(errors(all).get("personal.bio")).isEqualTo("The characters < and > are not allowed");
    }

    @Test
    void controlCharactersAreRejectedButTabsAndLineBreaksAreOnlyWhitespace() {
        assertThat(errors(names("Asha\u0000", "Rao"))).containsEntry("personal.firstName", "Control characters are not allowed");
        assertThat(errors(names("Asha", "R\u0007ao"))).containsKey("personal.lastName");
        Personal bio = new Personal("Asha", "Rao", null, null, null, "bad\u001bbio");
        assertThat(errors(request(bio, null, null, null))).containsKey("personal.bio");
        assertThat(errors(withOnline("https://linkedin.com/in/\u0000x", null))).containsKey("online.linkedinUrl");
    }

    @Test
    void workAndAddressLimitsAreEnforced() {
        Work tooLong = new Work("j".repeat(101), "d".repeat(101), "c".repeat(151), "i".repeat(101));
        assertThat(errors(request(person("A", "B"), tooLong, null, null))).containsKeys("work.jobTitle", "work.department",
            "work.companyName", "work.industry");
        Work atLimit = new Work("j".repeat(100), "d".repeat(100), "c".repeat(150), "i".repeat(100));
        assertThat(validator.validate(request(person("A", "B"), atLimit, null, null)).work().companyName()).hasSize(150);

        Address over = new Address("a".repeat(151), "b".repeat(151), "c".repeat(81), "s".repeat(81), null, "k".repeat(81));
        assertThat(errors(withAddress(over))).containsKeys("address.addressLine1", "address.addressLine2", "address.city",
            "address.state", "address.country");
    }

    @ParameterizedTest
    @ValueSource(strings = {"+919876543210", "+91 98765 43210", "+1 (415) 555-2671", "+44.20.7946.0958", "+91-98765-43210"})
    void mobileIsNormalisedToE164(String input) {
        String expected = input.replaceAll("[\\s\\-.()]", "");
        assertThat(validator.validate(withMobile(input)).personal().mobile()).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"9876543210", "+0123456789", "+12345", "+1234567890123456", "+91 98765 4321a", "00919876543210", "+", "++919876543210"})
    void mobileMustBeInternationalFormat(String input) {
        assertThat(errors(withMobile(input))).containsKey("personal.mobile");
    }

    @Test
    void alternatePhoneFollowsTheSameRule() {
        Personal ok = new Personal("Asha", "Rao", null, null, "+44 20 7946 0958", null);
        assertThat(validator.validate(request(ok, null, null, null)).personal().alternatePhone()).isEqualTo("+442079460958");
        Personal bad = new Personal("Asha", "Rao", null, null, "12345", null);
        assertThat(errors(request(bad, null, null, null))).containsKey("personal.alternatePhone");
    }

    @Test
    void aBlankPhoneIsSimplyEmpty() {
        assertThat(validator.validate(withMobile("   ")).personal().mobile()).isNull();
    }

    @Test
    void indiaPostalCodeMustBeExactlySixDigits() {
        for (String country : new String[] {"India", "india", "IN", "in", " INDIA "}) {
            assertThat(validator.validate(withAddress(new Address(null, null, null, null, "560001", country))).address().postalCode())
                .isEqualTo("560001");
            assertThat(errors(withAddress(new Address(null, null, null, null, "56001", country))))
                .containsEntry("address.postalCode", "A postal code in India has exactly 6 digits");
            assertThat(errors(withAddress(new Address(null, null, null, null, "5600AB", country)))).containsKey("address.postalCode");
            assertThat(errors(withAddress(new Address(null, null, null, null, "560 001", country)))).containsKey("address.postalCode");
        }
    }

    @Test
    void otherCountriesUseTheGeneralPostalPattern() {
        assertThat(validator.validate(withAddress(new Address(null, null, null, null, "SW1A 1AA", "United Kingdom"))).address().postalCode())
            .isEqualTo("SW1A 1AA");
        assertThat(validator.validate(withAddress(new Address(null, null, null, null, "94103-1234", "USA"))).address().postalCode())
            .isEqualTo("94103-1234");
        assertThat(validator.validate(withAddress(new Address(null, null, null, null, "1234", null))).address().postalCode()).isEqualTo("1234");
        for (String bad : new String[] {"1", "-1234", " ", "12#34", "123456789012", "ABCDEFGHIJKL"}) {
            if (bad.isBlank()) {
                assertThat(validator.validate(withAddress(new Address(null, null, null, null, bad, "France"))).address().postalCode()).isNull();
            } else {
                assertThat(errors(withAddress(new Address(null, null, null, null, bad, "France")))).as(bad).containsKey("address.postalCode");
            }
        }
        // Not India: a 5-digit code is fine, which is exactly what the India rule would refuse.
        assertThat(validator.validate(withAddress(new Address(null, null, null, null, "12345", "Indiana"))).address().postalCode()).isEqualTo("12345");
    }

    @Test
    void linkedinMustBeHttpsOnLinkedinDotCom() {
        for (String ok : new String[] {"https://linkedin.com/in/asha", "https://www.linkedin.com/in/asha-rao", "https://in.linkedin.com/in/asha", "HTTPS://WWW.LINKEDIN.COM/in/asha"}) {
            assertThat(validator.validate(withOnline(ok, null)).online().linkedinUrl()).isEqualTo(ok);
        }
        for (String bad : new String[] {"http://www.linkedin.com/in/asha", "https://notlinkedin.com/in/asha", "https://linkedin.com.evil.example/in/asha",
            "https://evil.example/linkedin.com", "www.linkedin.com/in/asha", "javascript:alert(1)", "https://user@linkedin.com/in/asha",
            "https://linked in.com/x", "ftp://linkedin.com/x", "https://linkedin/in/asha"}) {
            assertThat(errors(withOnline(bad, null))).as(bad).containsKey("online.linkedinUrl");
        }
    }

    @Test
    void websiteAllowsHttpAndHttpsWithARealHost() {
        for (String ok : new String[] {"https://example.com", "http://example.com/path?q=1", "https://sub.example.co.in/a/b"}) {
            assertThat(validator.validate(withOnline(null, ok)).online().websiteUrl()).isEqualTo(ok);
        }
        for (String bad : new String[] {"example.com", "ftp://example.com", "javascript:alert(1)", "https://", "https://localhost", "https://127.0.0.1",
            "https://exa mple.com", "https://user:pw@example.com", "//example.com", "https://-bad.example.com"}) {
            assertThat(errors(withOnline(null, bad))).as(bad).containsKey("online.websiteUrl");
        }
    }

    @Test
    void urlsHaveATwoHundredCharacterLimitAndAreNotRewritten() {
        String path = "a".repeat(200 - "https://example.com/".length());
        String exactly200 = "https://example.com/" + path;
        assertThat(exactly200).hasSize(200);
        assertThat(validator.validate(withOnline(null, "  " + exactly200 + "  ")).online().websiteUrl()).isEqualTo(exactly200);
        assertThat(errors(withOnline(null, exactly200 + "a"))).containsEntry("online.websiteUrl", "Must be at most 200 characters");
        assertThat(errors(withOnline("https://linkedin.com/in/" + "a".repeat(200), null))).containsKey("online.linkedinUrl");
        assertThat(validator.validate(withOnline("  ", "")).online().linkedinUrl()).isNull();
    }

    @Test
    void everyErrorIsReportedAtOnceUnderItsSectionDotFieldKey() {
        ProfileUpdateRequest bad = request(
            new Personal("", "R2", null, "123", null, null),
            new Work("j".repeat(101), null, null, null),
            new Address(null, null, null, null, "12", "India"),
            new Online("http://x.example", "nope"));
        Map<String, String> errors = errors(bad);
        assertThat(errors).containsOnlyKeys("personal.firstName", "personal.lastName", "personal.mobile", "work.jobTitle",
            "address.postalCode", "online.linkedinUrl", "online.websiteUrl");
    }

    @Test
    void theValidatorThrowsAValidationExceptionWithStatus400() {
        assertThatThrownBy(() -> validator.validate(names("", "")))
            .isInstanceOfSatisfying(ProfileValidationException.class, e -> {
                assertThat(e.getStatus().value()).isEqualTo(400);
                assertThat(e.getMessage()).isEqualTo("Validation failed");
            });
    }
}
