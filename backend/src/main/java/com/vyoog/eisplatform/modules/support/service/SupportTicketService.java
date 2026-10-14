package com.vyoog.eisplatform.modules.support.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.support.dto.CreateTicketRequest;
import com.vyoog.eisplatform.modules.support.dto.ResolveTicketRequest;
import com.vyoog.eisplatform.modules.support.dto.SupportTicketDto;
import com.vyoog.eisplatform.modules.support.dto.UpdateTicketRequest;
import com.vyoog.eisplatform.modules.support.model.SupportTicket;
import com.vyoog.eisplatform.modules.support.model.TicketPriority;
import com.vyoog.eisplatform.modules.support.model.TicketStatus;
import com.vyoog.eisplatform.modules.support.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * 12.01.01 Ticket Management (sprint 2027.1.2). Any authenticated customer
 * may create and track their own tickets; every other action requires
 * {@code MANAGE_SUPPORT_TICKETS} (platform ADMIN) — see {@link SupportTicket}'s
 * own javadoc on why nothing here is AI-driven.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupportTicketService {

    private static final Set<TicketStatus> TERMINAL = Set.of(TicketStatus.RESOLVED, TicketStatus.CLOSED);

    private final SupportTicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;
    /** C101 / REQ-SUP-002: deadlines on create and on a priority change. */
    private final SlaService slaService;

    @Transactional
    public SupportTicketDto createTicket(Long customerId, CreateTicketRequest request) {
        SupportTicket ticket = new SupportTicket();
        ticket.setRequestedByCustomerId(customerId);
        ticket.setSubject(request.subject());
        ticket.setDescription(request.description());
        ticket = ticketRepository.save(ticket);
        slaService.applyDeadlines(ticket);   // the clock starts now (createdAt is set by the save above)
        ticket = ticketRepository.save(ticket);

        auditService.recordSuccess("TICKET_CREATED", null, customerId, null,
            "SupportTicket", ticket.getId().toString(), null, "Ticket created: " + request.subject());
        return toDto(ticket, false);
    }

    public List<SupportTicketDto> listMyTickets(Long customerId) {
        return ticketRepository.findByRequestedByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(t -> toDto(t, false)).toList();
    }

    /** The caller's own ticket, or a generic 404 for anyone else's — same
     * pattern as {@code SubscriptionService#resolveOwnSubscription}. */
    public SupportTicketDto getMyTicket(Long customerId, Long ticketId) {
        SupportTicket ticket = findById(ticketId);
        if (!customerId.equals(ticket.getRequestedByCustomerId())) {
            throw new ResourceNotFoundException("Ticket not found");
        }
        return toDto(ticket, false);
    }

    public List<SupportTicketDto> listAllTickets() {
        return ticketRepository.findAllByOrderByCreatedAtDesc().stream().map(t -> toDto(t, true)).toList();
    }

    public SupportTicketDto getTicket(Long ticketId) {
        return toDto(findById(ticketId), true);
    }

    /** 12.01.01.02/.03/.04 Categorize/Prioritize/Assign, folded into one
     * call. Assigning an OPEN ticket also moves it to IN_PROGRESS — there is
     * no separate "start work" action. */
    @Transactional
    public SupportTicketDto updateTicket(Long ticketId, UpdateTicketRequest request) {
        SupportTicket ticket = findById(ticketId);
        refuseIfTerminal(ticket);
        if (request.category() != null) ticket.setCategory(request.category());
        if (request.priority() != null && request.priority() != ticket.getPriority()) {
            ticket.setPriority(request.priority());
            if (ticket.getResolutionDueAt() != null) {
                recalculateDeadlines(ticket);
            }
        }
        if (request.assignedToCustomerId() != null) {
            ticket.setAssignedToCustomerId(request.assignedToCustomerId());
            if (ticket.getStatus() == TicketStatus.OPEN) {
                ticket.setStatus(TicketStatus.IN_PROGRESS);
            }
            markFirstResponse(ticket);
        }
        ticket = ticketRepository.save(ticket);
        auditService.recordSuccess("TICKET_UPDATED", null, null, null,
            "SupportTicket", ticketId.toString(), null, "Ticket categorized/prioritized/assigned");
        return toDto(ticket, true);
    }

    /** 12.01.01.05 Escalate ticket — also raises priority to URGENT, the
     * concrete effect an escalation has (Not specified beyond that in any
     * source; not inventing a routing/on-call chain here). */
    @Transactional
    public SupportTicketDto escalateTicket(Long ticketId) {
        SupportTicket ticket = findById(ticketId);
        refuseIfTerminal(ticket);
        ticket.setStatus(TicketStatus.ESCALATED);
        if (ticket.getPriority() != TicketPriority.URGENT) {
            ticket.setPriority(TicketPriority.URGENT);
            if (ticket.getResolutionDueAt() != null) {
                recalculateDeadlines(ticket);
            }
        }
        markFirstResponse(ticket);
        ticket = ticketRepository.save(ticket);
        auditService.recordSuccess("TICKET_ESCALATED", null, null, null,
            "SupportTicket", ticketId.toString(), null, "Ticket escalated");
        notifyRequester(ticket, "Your ticket was escalated", "Your support ticket \"" + ticket.getSubject() + "\" has been escalated.");
        return toDto(ticket, true);
    }

    /** 12.01.01.06 Resolve ticket. */
    @Transactional
    public SupportTicketDto resolveTicket(Long ticketId, ResolveTicketRequest request) {
        SupportTicket ticket = findById(ticketId);
        refuseIfTerminal(ticket);
        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.setResolutionNote(request.resolutionNote());
        ticket.setResolvedAt(Instant.now());
        markFirstResponse(ticket);
        ticket = ticketRepository.save(ticket);
        auditService.recordSuccess("TICKET_RESOLVED", null, null, null,
            "SupportTicket", ticketId.toString(), null, "Ticket resolved");
        notifyRequester(ticket, "Your ticket was resolved", "Your support ticket \"" + ticket.getSubject() + "\" has been resolved.");
        return toDto(ticket, true);
    }

    /** 12.01.01.07 Close ticket — only from RESOLVED (Not specified whether a
     * ticket can be closed directly; requiring resolution first is the safer
     * reading and matches "Resolve" then "Close" being two distinct steps). */
    @Transactional
    public SupportTicketDto closeTicket(Long ticketId) {
        SupportTicket ticket = findById(ticketId);
        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new IllegalArgumentException("Only a resolved ticket can be closed.");
        }
        ticket.setStatus(TicketStatus.CLOSED);
        ticket.setClosedAt(Instant.now());
        ticket = ticketRepository.save(ticket);
        auditService.recordSuccess("TICKET_CLOSED", null, null, null,
            "SupportTicket", ticketId.toString(), null, "Ticket closed");
        return toDto(ticket, true);
    }

    /** C101: the first human action on the ticket stops the first-response clock. Nothing to do for a ticket without deadlines. */
    private void markFirstResponse(SupportTicket ticket) {
        if (ticket.getFirstRespondedAt() == null && ticket.getFirstResponseDueAt() != null) {
            ticket.setFirstRespondedAt(Instant.now());
        }
    }

    /** C101: a new priority moves both deadlines, counted from the creation time; marks that no longer apply are cleared (a breach already recorded stays). */
    private void recalculateDeadlines(SupportTicket ticket) {
        slaService.applyDeadlines(ticket);
        ticket.setFirstResponseWarnedAt(null);
        ticket.setResolutionWarnedAt(null);
    }

    private void refuseIfTerminal(SupportTicket ticket) {
        if (TERMINAL.contains(ticket.getStatus())) {
            throw new IllegalArgumentException("This ticket is already resolved or closed.");
        }
    }

    private SupportTicket findById(Long ticketId) {
        return ticketRepository.findById(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
    }

    private void notifyRequester(SupportTicket ticket, String title, String message) {
        customerRepository.findById(ticket.getRequestedByCustomerId()).ifPresent(customer ->
            notificationService.notify(customer.getId(), customer.getEmail(), NotificationCategory.SYSTEM, NotificationSeverity.INFO, title, message));
    }

    private SupportTicketDto toDto(SupportTicket ticket, boolean admin) {
        Instant now = Instant.now();
        String requestedByName = customerRepository.findById(ticket.getRequestedByCustomerId())
            .map(this::fullName).orElse("Unknown");
        String assignedToName = ticket.getAssignedToCustomerId() == null ? null
            : customerRepository.findById(ticket.getAssignedToCustomerId()).map(this::fullName).orElse(null);
        return new SupportTicketDto(
            ticket.getId(), ticket.getRequestedByCustomerId(), requestedByName,
            ticket.getSubject(), ticket.getDescription(), ticket.getCategory(),
            ticket.getPriority(), ticket.getStatus(),
            ticket.getAssignedToCustomerId(), assignedToName,
            ticket.getResolutionNote(), ticket.getResolvedAt(), ticket.getClosedAt(), ticket.getCreatedAt(),
            ticket.getFirstRespondedAt(), ticket.getFirstResponseDueAt(), ticket.getResolutionDueAt(),
            admin ? SlaService.firstResponseState(ticket, now) : null, admin ? SlaService.resolutionState(ticket, now) : null
        );
    }

    private String fullName(Customer customer) {
        return customer.getFirstName() + " " + customer.getLastName();
    }
}
