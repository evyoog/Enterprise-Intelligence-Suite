package com.vyoog.eisplatform.modules.preference.repository;

import com.vyoog.eisplatform.modules.preference.model.CustomerPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerPreferenceRepository extends JpaRepository<CustomerPreference, Long> {
}
