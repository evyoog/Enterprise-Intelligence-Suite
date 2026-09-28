package com.vyoog.eisplatform.modules.support.controller;

import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.support.dto.CreateTicketRequest;
import com.vyoog.eisplatform.modules.support.dto.SupportTicketDto;
import com.vyoog.eisplatform.modules.support.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 12.01.01 Ticket Management — any authenticated customer's own tickets.
 * Covered by the existing "/me/**" authenticated() rule in SecurityConfig. */
@RestController
@RequestMapping("/me/tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final CurrentCustomerResolver currentCustomerResolver;
    private final SupportTicketService ticketService;

    @PostMapping
    public SupportTicketDto create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateTicketRequest request) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return ticketService.createTicket(customer.getId(), request);
    }

    @GetMapping
    public List<SupportTicketDto> myTickets(@AuthenticationPrincipal Jwt jwt) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return ticketService.listMyTickets(customer.getId());
    }

    @GetMapping("/{id}")
    public SupportTicketDto get(@AuthenticationPrincipal Jwt jwt, @PathVariable("id") Long ticketId) {
        Customer customer = currentCustomerResolver.resolve(jwt);
        return ticketService.getMyTicket(customer.getId(), ticketId);
    }
}
