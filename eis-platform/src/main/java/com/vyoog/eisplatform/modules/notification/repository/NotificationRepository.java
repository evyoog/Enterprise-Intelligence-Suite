package com.vyoog.eisplatform.modules.notification.repository;

import com.vyoog.eisplatform.modules.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    Optional<Notification> findByIdAndCustomerId(Long id, Long customerId);

    long countByCustomerIdAndReadFalse(Long customerId);

    List<Notification> findByCustomerIdAndReadFalse(Long customerId);
}
