package com.vyoog.eisplatform.modules.administration.service;

import com.vyoog.eisplatform.modules.administration.model.PlatformFeatureFlag;
import com.vyoog.eisplatform.modules.administration.repository.PlatformFeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 15.01.01 Configure feature flags (sprint 2026.4.2). Kept as its own small
 * service, separate from {@link PlatformAdministrationService}'s CRUD, so
 * any module can depend on just this one read — {@link #isEnabled} — without
 * pulling in the whole admin-settings surface. An unknown key is treated as
 * enabled (fails open) rather than silently disabling a feature nobody has
 * configured a flag for yet.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlatformFeatureFlagService {

    private final PlatformFeatureFlagRepository flagRepository;

    public boolean isEnabled(String flagKey) {
        return flagRepository.findById(flagKey).map(PlatformFeatureFlag::isEnabled).orElse(true);
    }
}
