package com.vyoog.eisplatform.modules.support.controller;

import com.vyoog.eisplatform.modules.support.dto.ResolveTicketRequest;
import com.vyoog.eisplatform.modules.support.dto.SupportTicketDto;
import com.vyoog.eisplatform.modules.support.dto.UpdateTicketRequest;
import com.vyoog.eisplatform.modules.support.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 12.01.01 Ticket Management admin actions — MANAGE_SUPPORT_TICKETS-gated in SecurityConfig. */
@RestController
@RequestMapping("/admin/support/tickets")
@RequiredArgsConstructor
public class AdminSupportTicketController {

    private final SupportTicketService ticketService;

    @GetMapping
    public List<SupportTicketDto> listAll() {
        return ticketService.listAllTickets();
    }

    @GetMapping("/{id}")
    public SupportTicketDto get(@PathVariable("id") Long id) {
        return ticketService.getTicket(id);
    }

    @PatchMapping("/{id}")
    public SupportTicketDto update(@PathVariable("id") Long id, @RequestBody UpdateTicketRequest request) {
        return ticketService.updateTicket(id, request);
    }

    @PostMapping("/{id}/escalate")
    public SupportTicketDto escalate(@PathVariable("id") Long id) {
        return ticketService.escalateTicket(id);
    }

    @PostMapping("/{id}/resolve")
    public SupportTicketDto resolve(@PathVariable("id") Long id, @Valid @RequestBody(required = false) ResolveTicketRequest request) {
        return ticketService.resolveTicket(id, request != null ? request : new ResolveTicketRequest(null));
    }

    @PostMapping("/{id}/close")
    public SupportTicketDto close(@PathVariable("id") Long id) {
        return ticketService.closeTicket(id);
    }
}
