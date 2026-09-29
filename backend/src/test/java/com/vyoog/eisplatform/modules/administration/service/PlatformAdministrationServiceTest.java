package com.vyoog.eisplatform.modules.administration.service;

import com.vyoog.eisplatform.common.exception.DuplicateResourceException;
import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.administration.dto.CreateFeatureFlagRequest;
import com.vyoog.eisplatform.modules.administration.dto.CreateRegionRequest;
import com.vyoog.eisplatform.modules.administration.dto.UpdateFeatureFlagRequest;
import com.vyoog.eisplatform.modules.administration.dto.UpdateRegionRequest;
import com.vyoog.eisplatform.modules.registration.model.Organization;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 15.01 Platform Administration (sprint 2026.4.2). */
@SpringBootTest
@ActiveProfiles("test")
class PlatformAdministrationServiceTest {

    @Autowired
    private PlatformAdministrationService service;

    @Autowired
    private PlatformFeatureFlagService featureFlagService;

    @Autowired
    private OrganizationRepository organizationRepository;

    private Organization newOrganization() {
        Organization organization = new Organization();
        organization.setName("Test Org");
        organization.setCode("TEST-" + System.nanoTime());
        organization.setBusinessEmail("biz@test-org.example");
        organization.setCountry("India");
        organization.setLicensedSeats(10);
        return organizationRepository.save(organization);
    }

    @Test
    void currenciesAreSeededFromTheFixedCurrencyEnum() {
        var currencies = service.listCurrencies();
        assertThat(currencies).extracting(com.vyoog.eisplatform.modules.administration.dto.PlatformCurrencyDto::code)
            .containsExactlyInAnyOrder("USD", "EUR", "GBP", "INR");
        assertThat(currencies).allSatisfy(c -> assertThat(c.enabled()).isTrue());
    }

    @Test
    void anAdminCanDisableAndReEnableACurrency() {
        var disabled = service.updateCurrency("EUR", false, "test-admin");
        assertThat(disabled.enabled()).isFalse();

        var reenabled = service.updateCurrency("EUR", true, "test-admin");
        assertThat(reenabled.enabled()).isTrue();
    }

    @Test
    void updatingAnUnknownCurrencyIsRefused() {
        assertThatThrownBy(() -> service.updateCurrency("XYZ", true, "test-admin"))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void anAdminCanCreateAndUpdateARegion() {
        String tag = "region-" + System.nanoTime();
        var created = service.createRegion(new CreateRegionRequest(tag, "Test Region"), "test-admin");
        assertThat(created.code()).isEqualTo(tag);
        assertThat(created.enabled()).isTrue();

        var updated = service.updateRegion(created.id(), new UpdateRegionRequest("Renamed Region", false), "test-admin");
        assertThat(updated.name()).isEqualTo("Renamed Region");
        assertThat(updated.enabled()).isFalse();
    }

    @Test
    void creatingARegionWithADuplicateCodeIsRefused() {
        String tag = "dup-" + System.nanoTime();
        service.createRegion(new CreateRegionRequest(tag, "First"), "test-admin");

        assertThatThrownBy(() -> service.createRegion(new CreateRegionRequest(tag, "Second"), "test-admin"))
            .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void anAdminCanCreateUpdateAndDeleteAFeatureFlag() {
        String tag = "flag_" + System.nanoTime();
        var created = service.createFeatureFlag(new CreateFeatureFlagRequest(tag, true, "A test flag"), "test-admin");
        assertThat(created.enabled()).isTrue();
        assertThat(featureFlagService.isEnabled(tag)).isTrue();

        var updated = service.updateFeatureFlag(tag, new UpdateFeatureFlagRequest(false, "Now disabled"), "test-admin");
        assertThat(updated.enabled()).isFalse();
        assertThat(featureFlagService.isEnabled(tag)).isFalse();

        service.deleteFeatureFlag(tag, "test-admin");
        // An unknown/deleted flag fails open (see PlatformFeatureFlagService's own javadoc).
        assertThat(featureFlagService.isEnabled(tag)).isTrue();
    }

    @Test
    void anUnknownFeatureFlagIsTreatedAsEnabled() {
        assertThat(featureFlagService.isEnabled("no_such_flag_" + System.nanoTime())).isTrue();
    }

    @Test
    void supportedLanguagesAreReadOnlyAndFixed() {
        assertThat(service.listSupportedLanguages()).extracting("code").containsExactly("en", "es");
    }

    @Test
    void aRegionAssignedToAnOrganizationCannotBeDeleted() {
        String tag = "assigned-" + System.nanoTime();
        var region = service.createRegion(new CreateRegionRequest(tag, "Assigned Region"), "test-admin");
        Organization org = newOrganization();
        org.setRegionId(region.id());
        organizationRepository.save(org);

        assertThatThrownBy(() -> service.deleteRegion(region.id(), "test-admin"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void anUnassignedRegionCanBeDeleted() {
        String tag = "unassigned-" + System.nanoTime();
        var region = service.createRegion(new CreateRegionRequest(tag, "Unassigned Region"), "test-admin");

        service.deleteRegion(region.id(), "test-admin");

        assertThat(service.listRegions()).extracting("id").doesNotContain(region.id());
    }
}
