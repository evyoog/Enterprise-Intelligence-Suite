package com.vyoog.eisplatform.modules.registration.repository;

import com.vyoog.eisplatform.modules.registration.model.Order;
import com.vyoog.eisplatform.modules.registration.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);

    List<Order> findByRequestedByCustomerIdOrderByCreatedAtDesc(Long requestedByCustomerId);

    List<Order> findByOrganizationIdAndStatusOrderByCreatedAtAsc(Long organizationId, OrderStatus status);
}
