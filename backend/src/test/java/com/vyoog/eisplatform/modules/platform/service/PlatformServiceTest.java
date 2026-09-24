package com.vyoog.eisplatform.modules.platform.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.platform.dto.PlatformCreateRequest;
import com.vyoog.eisplatform.modules.platform.dto.PlatformDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class PlatformServiceTest {

    @Autowired
    private PlatformService platformService;

    @Test
    void createPlatformSavesAllFields() {
        PlatformDto created = platformService.createPlatform(
            new PlatformCreateRequest("Thittam", "Suite of Vyoog apps", "https://example.com/thittam.png"));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Thittam");
        assertThat(created.getDescription()).isEqualTo("Suite of Vyoog apps");
        assertThat(created.getImageUrl()).isEqualTo("https://example.com/thittam.png");
    }

    @Test
    void listPlatformsIncludesEveryCreatedPlatform() {
        PlatformDto created = platformService.createPlatform(new PlatformCreateRequest("Listed platform", null, null));

        assertThat(platformService.listPlatforms())
            .extracting(PlatformDto::getId)
            .contains(created.getId());
    }

    @Test
    void updatePlatformChangesAllFields() {
        PlatformDto created = platformService.createPlatform(new PlatformCreateRequest("Original", "Original desc", null));

        PlatformDto updated = platformService.updatePlatform(created.getId(),
            new PlatformCreateRequest("Updated", "Updated desc", "https://example.com/new.png"));

        assertThat(updated.getName()).isEqualTo("Updated");
        assertThat(updated.getDescription()).isEqualTo("Updated desc");
        assertThat(updated.getImageUrl()).isEqualTo("https://example.com/new.png");
    }

    @Test
    void updatePlatformThrowsWhenNotFound() {
        assertThatThrownBy(() -> platformService.updatePlatform(-1L, new PlatformCreateRequest("Doesn't matter", null, null)))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getPlatformThrowsWhenNotFound() {
        assertThatThrownBy(() -> platformService.getPlatform(-1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deletePlatformRemovesItFromTheListing() {
        PlatformDto created = platformService.createPlatform(new PlatformCreateRequest("To delete", null, null));

        platformService.deletePlatform(created.getId());

        assertThatThrownBy(() -> platformService.getPlatform(created.getId()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deletePlatformThrowsWhenNotFound() {
        assertThatThrownBy(() -> platformService.deletePlatform(-1L))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
