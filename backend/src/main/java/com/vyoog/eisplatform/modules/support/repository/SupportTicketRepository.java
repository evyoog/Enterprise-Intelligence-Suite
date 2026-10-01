package com.vyoog.eisplatform.modules.support.repository;

import com.vyoog.eisplatform.modules.support.model.SupportTicket;
import com.vyoog.eisplatform.modules.support.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    List<SupportTicket> findByRequestedByCustomerIdOrderByCreatedAtDesc(Long requestedByCustomerId);

    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    /** Platform admin dashboard (C53): tickets still needing attention —
     * every status except the terminal RESOLVED/CLOSED pair (TicketStatus's
     * own javadoc). */
    long countByStatusIn(Collection<TicketStatus> statuses);

    /** Demo data seeding (C54, {@code DemoDataSeeder}) idempotency check —
     * subjects aren't unique by constraint, but the seeder always uses the
     * same fixed "Demo: ..." subjects, so this is enough to never re-insert. */
    boolean existsBySubject(String subject);
}
