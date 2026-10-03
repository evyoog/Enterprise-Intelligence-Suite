package com.vyoog.eisplatform.modules.integration.repository;

import com.vyoog.eisplatform.modules.integration.model.EventHandlerReceipt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventHandlerReceiptRepository extends JpaRepository<EventHandlerReceipt, Long> {

    boolean existsByHandlerNameAndEventId(String handlerName, String eventId);

    List<EventHandlerReceipt> findByEventIdOrderByProcessedAtAsc(String eventId);
}
