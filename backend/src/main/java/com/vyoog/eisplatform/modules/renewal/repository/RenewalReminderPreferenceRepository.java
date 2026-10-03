package com.vyoog.eisplatform.modules.renewal.repository;

import com.vyoog.eisplatform.modules.renewal.model.RenewalReminderPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RenewalReminderPreferenceRepository extends JpaRepository<RenewalReminderPreference, Long> {

    Optional<RenewalReminderPreference> findByCustomerId(Long customerId);
}
