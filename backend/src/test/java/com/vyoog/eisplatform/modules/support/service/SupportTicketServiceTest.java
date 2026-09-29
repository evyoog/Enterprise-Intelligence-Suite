package com.vyoog.eisplatform.modules.support.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.support.dto.CreateTicketRequest;
import com.vyoog.eisplatform.modules.support.dto.ResolveTicketRequest;
import com.vyoog.eisplatform.modules.support.dto.SupportTicketDto;
import com.vyoog.eisplatform.modules.support.dto.UpdateTicketRequest;
import com.vyoog.eisplatform.modules.support.model.TicketPriority;
import com.vyoog.eisplatform.modules.support.model.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 12.01.01 Ticket Management (sprint 2027.1.2). */
@SpringBootTest
@ActiveProfiles("test")
class SupportTicketServiceTest {

    @Autowired
    private SupportTicketService ticketService;
    @Autowired
    private CustomerRepository customerRepository;

    private Long newCustomer() {
        Customer customer = new Customer();
        customer.setEmail("ticket-test-" + System.nanoTime() + "@test.example");
        customer.setFirstName("Test");
        customer.setLastName("User");
        return customerRepository.save(customer).getId();
    }

    @Test
    void customerCreatesAndTracksTheirOwnTicket() {
        Long customerId = newCustomer();
        SupportTicketDto created = ticketService.createTicket(customerId, new CreateTicketRequest("Can't log in", "MFA code never arrives."));
        assertThat(created.status()).isEqualTo(TicketStatus.OPEN);
        assertThat(created.priority()).isEqualTo(TicketPriority.MEDIUM);

        assertThat(ticketService.listMyTickets(customerId)).extracting(SupportTicketDto::id).contains(created.id());
        assertThat(ticketService.getMyTicket(customerId, created.id()).subject()).isEqualTo("Can't log in");
    }

    @Test
    void anotherCustomersTicketIsInvisible() {
        Long owner = newCustomer();
        Long stranger = newCustomer();
        SupportTicketDto ticket = ticketService.createTicket(owner, new CreateTicketRequest("Subject", "Description"));

        assertThatThrownBy(() -> ticketService.getMyTicket(stranger, ticket.id()))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void assigningAnOpenTicketMovesItToInProgress() {
        Long customerId = newCustomer();
        Long agentId = newCustomer();
        SupportTicketDto ticket = ticketService.createTicket(customerId, new CreateTicketRequest("Subject", "Description"));

        SupportTicketDto updated = ticketService.updateTicket(ticket.id(), new UpdateTicketRequest("Billing", TicketPriority.HIGH, agentId));
        assertThat(updated.status()).isEqualTo(TicketStatus.IN_PROGRESS);
        assertThat(updated.category()).isEqualTo("Billing");
        assertThat(updated.priority()).isEqualTo(TicketPriority.HIGH);
        assertThat(updated.assignedToCustomerId()).isEqualTo(agentId);
    }

    @Test
    void escalatingRaisesPriorityToUrgent() {
        Long customerId = newCustomer();
        SupportTicketDto ticket = ticketService.createTicket(customerId, new CreateTicketRequest("Subject", "Description"));

        SupportTicketDto escalated = ticketService.escalateTicket(ticket.id());
        assertThat(escalated.status()).isEqualTo(TicketStatus.ESCALATED);
        assertThat(escalated.priority()).isEqualTo(TicketPriority.URGENT);
    }

    @Test
    void resolveThenCloseFollowsTheOneWayLifecycle() {
        Long customerId = newCustomer();
        SupportTicketDto ticket = ticketService.createTicket(customerId, new CreateTicketRequest("Subject", "Description"));

        assertThatThrownBy(() -> ticketService.closeTicket(ticket.id()))
            .isInstanceOf(IllegalArgumentException.class);

        SupportTicketDto resolved = ticketService.resolveTicket(ticket.id(), new ResolveTicketRequest("Reset the MFA device."));
        assertThat(resolved.status()).isEqualTo(TicketStatus.RESOLVED);
        assertThat(resolved.resolvedAt()).isNotNull();

        SupportTicketDto closed = ticketService.closeTicket(ticket.id());
        assertThat(closed.status()).isEqualTo(TicketStatus.CLOSED);
        assertThat(closed.closedAt()).isNotNull();

        assertThatThrownBy(() -> ticketService.updateTicket(ticket.id(), new UpdateTicketRequest("Billing", null, null)))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ticketService.escalateTicket(ticket.id()))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ticketService.resolveTicket(ticket.id(), new ResolveTicketRequest(null)))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
