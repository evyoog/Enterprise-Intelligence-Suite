package com.vyoog.eisplatform.modules.support.repository;

import com.vyoog.eisplatform.modules.support.model.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByRequestedByCustomerIdOrderByCreatedAtDesc(Long requestedByCustomerId);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();
}
