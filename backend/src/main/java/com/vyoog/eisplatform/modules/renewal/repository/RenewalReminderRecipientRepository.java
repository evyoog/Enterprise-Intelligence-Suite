package com.vyoog.eisplatform.modules.renewal.repository;

import com.vyoog.eisplatform.modules.renewal.model.RenewalReminderRecipient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RenewalReminderRecipientRepository extends JpaRepository<RenewalReminderRecipient, Long> {
    List<RenewalReminderRecipient> findByOrganizationId(Long organizationId);

    void deleteByOrganizationId(Long organizationId);
}
