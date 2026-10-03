package com.vyoog.eisplatform.modules.renewal.repository;

import com.vyoog.eisplatform.modules.renewal.model.RenewalReminderLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RenewalReminderLogRepository extends JpaRepository<RenewalReminderLog, Long> {

    boolean existsBySubscriptionIdAndRecipientCustomerIdAndLocalDate(Long subscriptionId, Long recipientCustomerId, LocalDate localDate);

    List<RenewalReminderLog> findBySubscriptionIdOrderBySentAtAsc(Long subscriptionId);
}
