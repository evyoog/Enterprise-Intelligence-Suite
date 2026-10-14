package com.vyoog.eisplatform.modules.support.repository;

import com.vyoog.eisplatform.modules.support.model.SlaPolicy;
import com.vyoog.eisplatform.modules.support.model.TicketPriority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, TicketPriority> {
}
