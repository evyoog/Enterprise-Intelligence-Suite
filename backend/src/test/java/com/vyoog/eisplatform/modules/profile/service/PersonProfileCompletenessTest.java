package com.vyoog.eisplatform.modules.profile.service;

import com.vyoog.eisplatform.modules.profile.dto.ProfileDtos.Completion;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** My profile (C107): the eleven-item completion, percent = round(filled * 100 / 11). */
class PersonProfileCompletenessTest {

    private static Customer full() {
        Customer c = new Customer();
        c.setMobile("+919876543210");
        c.setJobTitle("CTO");
        c.setDepartment("Engineering");
        c.setCompanyName("Acme");
        c.setIndustry("Software");
        c.setAddressLine1("1 Main Road");
        c.setCity("Bengaluru");
        c.setState("KA");
        c.setPostalCode("560001");
        c.setCountry("India");
        return c;
    }

    @Test
    void anEmptyProfileIsZeroOfEleven() {
        Completion c = PersonProfileCompleteness.of(new Customer(), false);
        assertThat(c.percent()).isZero();
        assertThat(c.filled()).isZero();
        assertThat(c.total()).isEqualTo(11);
        assertThat(c.missing()).containsExactlyElementsOf(PersonProfileCompleteness.KEYS);
        assertThat(PersonProfileCompleteness.empty()).isEqualTo(c);
    }

    @Test
    void aFullProfileWithAPhotoIsOneHundredPercent() {
        Completion c = PersonProfileCompleteness.of(full(), true);
        assertThat(c.percent()).isEqualTo(100);
        assertThat(c.filled()).isEqualTo(11);
        assertThat(c.missing()).isEmpty();
    }

    @Test
    void thePhotoIsOneOfTheEleven() {
        Completion c = PersonProfileCompleteness.of(full(), false);
        assertThat(c.filled()).isEqualTo(10);
        assertThat(c.percent()).isEqualTo(91);
        assertThat(c.missing()).containsExactly("photo");
    }

    @Test
    void blankValuesDoNotCountAndNamesAndEmailAreNotPartOfIt() {
        Customer c = new Customer();
        c.setFirstName("Asha");
        c.setLastName("Rao");
        c.setEmail("a@b.example");
        c.setBio("something");
        c.setMobile("   ");
        c.setCity("");
        c.setCountry("India");
        Completion result = PersonProfileCompleteness.of(c, false);
        assertThat(result.filled()).isEqualTo(1);
        assertThat(result.missing()).doesNotContain("country").contains("mobile", "city");
    }

    @Test
    void percentIsRoundedFromElevenths() {
        int[] expected = {0, 9, 18, 27, 36, 45, 55, 64, 73, 82, 91, 100};
        for (int filled = 0; filled <= 11; filled++) {
            Customer c = new Customer();
            String[] order = {"mobile", "jobTitle", "department", "companyName", "industry", "addressLine1", "city", "state", "postalCode", "country"};
            for (int i = 0; i < Math.min(filled, 10); i++) {
                switch (order[i]) {
                    case "mobile" -> c.setMobile("+911234567");
                    case "jobTitle" -> c.setJobTitle("x");
                    case "department" -> c.setDepartment("x");
                    case "companyName" -> c.setCompanyName("x");
                    case "industry" -> c.setIndustry("x");
                    case "addressLine1" -> c.setAddressLine1("x");
                    case "city" -> c.setCity("x");
                    case "state" -> c.setState("x");
                    case "postalCode" -> c.setPostalCode("12345");
                    default -> c.setCountry("x");
                }
            }
            Completion result = PersonProfileCompleteness.of(c, filled == 11);
            assertThat(result.filled()).as("filled " + filled).isEqualTo(filled);
            assertThat(result.percent()).as("percent for " + filled).isEqualTo(expected[filled]);
        }
    }
}
