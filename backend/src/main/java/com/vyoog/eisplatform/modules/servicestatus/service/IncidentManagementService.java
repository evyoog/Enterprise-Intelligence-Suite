package com.vyoog.eisplatform.modules.servicestatus.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.model.ProductStatus;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.servicestatus.dto.AdminIncidentDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.IncidentRequest;
import com.vyoog.eisplatform.modules.servicestatus.model.IncidentPhase;
import com.vyoog.eisplatform.modules.servicestatus.model.ServiceIncident;
import com.vyoog.eisplatform.modules.servicestatus.repository.ProblemRepository;
import com.vyoog.eisplatform.modules.servicestatus.repository.ServiceIncidentRepository;
import com.vyoog.eisplatform.modules.support.model.SupportTicket;
import com.vyoog.eisplatform.modules.support.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * C101 / REQ-SUP-003 (IC-1 A): a support incident is the status-page incident. Platform administrators ({@code MANAGE_SERVICE_STATUS}) post and update it:
 * the public fields appear on the status page for customers who bought the product; severity, phase, internal notes, assignee, resolution summary, linked
 * tickets and problem stay internal. Phases: Logged, Investigating, Resolved (sets the end time, needs a resolution summary), Closed (only after Resolved,
 * then final).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IncidentManagementService {

    private final ServiceIncidentRepository incidentRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SupportTicketRepository ticketRepository;
    private final ProblemRepository problemRepository;
    private final AuditService auditService;

    public List<AdminIncidentDto> list() {
        Maps maps = maps();
        return incidentRepository.findTop200ByOrderByStartedAtDesc().stream().map(i -> toDto(i, maps)).toList();
    }

    public AdminIncidentDto get(Long id) {
        return toDto(find(id), maps());
    }

    @Transactional
    public AdminIncidentDto createIncident(IncidentRequest request, String actorKeycloakSub) {
        Product product = requireActiveProduct(request.productId());
        ServiceIncident incident = new ServiceIncident();
        incident.setCreatedAt(Instant.now());
        incident.setCreatedByKeycloakSub(actorKeycloakSub);
        apply(incident, request);
        incident = incidentRepository.save(incident);
        auditService.recordSuccess("INCIDENT_POSTED", actorKeycloakSub, null, null, "ServiceIncident", incident.getId().toString(), null,
            product.getName() + ": " + incident.getTitle());
        return toDto(incident, maps());
    }

    @Transactional
    public AdminIncidentDto updateIncident(Long incidentId, IncidentRequest request, String actorKeycloakSub) {
        ServiceIncident incident = find(incidentId);
        Product product = requireActiveProduct(request.productId());
        IncidentPhase before = incident.getPhase();
        apply(incident, request);
        incident = incidentRepository.save(incident);
        auditService.recordSuccess("INCIDENT_UPDATED", actorKeycloakSub, null, null, "ServiceIncident", incidentId.toString(), null,
            product.getName() + ": " + incident.getTitle() + " (" + before + (before == incident.getPhase() ? "" : " -> " + incident.getPhase()) + ")");
        return toDto(incident, maps());
    }

    /** Links a support ticket to the incident (the customers who reported the same trouble). */
    @Transactional
    public AdminIncidentDto linkTicket(Long incidentId, Long ticketId, String actorKeycloakSub) {
        ServiceIncident incident = find(incidentId);
        ticketRepository.findById(ticketId).orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        if (incident.getPhase() == IncidentPhase.CLOSED) {
            throw new IllegalArgumentException("A closed incident cannot be changed.");
        }
        if (incident.getTicketIds().add(ticketId)) {
            incidentRepository.save(incident);
            auditService.recordSuccess("INCIDENT_TICKET_LINKED", actorKeycloakSub, null, null, "ServiceIncident", incidentId.toString(), null, "Ticket " + ticketId + " linked");
        }
        return toDto(incident, maps());
    }

    @Transactional
    public AdminIncidentDto unlinkTicket(Long incidentId, Long ticketId, String actorKeycloakSub) {
        ServiceIncident incident = find(incidentId);
        if (incident.getPhase() == IncidentPhase.CLOSED) {
            throw new IllegalArgumentException("A closed incident cannot be changed.");
        }
        if (incident.getTicketIds().remove(ticketId)) {
            incidentRepository.save(incident);
            auditService.recordSuccess("INCIDENT_TICKET_UNLINKED", actorKeycloakSub, null, null, "ServiceIncident", incidentId.toString(), null, "Ticket " + ticketId + " unlinked");
        }
        return toDto(incident, maps());
    }

    private void apply(ServiceIncident incident, IncidentRequest request) {
        if (incident.getPhase() == IncidentPhase.CLOSED) {
            throw new IllegalArgumentException("A closed incident cannot be changed.");
        }
        if (request.endedAt() != null && request.endedAt().isBefore(request.startedAt())) {
            throw new IllegalArgumentException("The end time must be after the start time.");
        }
        IncidentPhase current = incident.getPhase();
        IncidentPhase target = request.phase() != null ? request.phase()
            : request.endedAt() != null ? IncidentPhase.RESOLVED
            : current == IncidentPhase.RESOLVED ? IncidentPhase.INVESTIGATING : current;   // the end time removed = reopened

        String summary = request.resolutionSummary() != null ? blankToNull(request.resolutionSummary()) : incident.getResolutionSummary();
        Instant endedAt = request.endedAt();
        Instant closedAt = null;
        switch (target) {
            case LOGGED, INVESTIGATING -> {
                if (request.endedAt() != null) {
                    throw new IllegalArgumentException("An incident that is " + target.name().toLowerCase() + " has no end time. Mark it resolved instead.");
                }
                endedAt = null;
            }
            case RESOLVED -> {
                if (summary == null) {
                    throw new IllegalArgumentException("Write a resolution summary before marking the incident resolved.");
                }
                if (endedAt == null) {
                    endedAt = incident.getEndedAt() != null ? incident.getEndedAt() : Instant.now();
                }
            }
            case CLOSED -> {
                if (current != IncidentPhase.RESOLVED) {
                    throw new IllegalArgumentException("Only a resolved incident can be closed.");
                }
                endedAt = request.endedAt() != null ? request.endedAt() : incident.getEndedAt();
                closedAt = Instant.now();
            }
        }
        if (endedAt != null && endedAt.isBefore(request.startedAt())) {
            throw new IllegalArgumentException("The end time must be after the start time.");
        }
        if (request.assignedToCustomerId() != null) {
            customerRepository.findById(request.assignedToCustomerId()).orElseThrow(() -> new IllegalArgumentException("The assignee does not exist."));
            incident.setAssignedToCustomerId(request.assignedToCustomerId());
        }
        incident.setProductId(request.productId());
        incident.setTitle(request.title().trim());
        incident.setMessage(request.message().trim());
        incident.setStartedAt(request.startedAt());
        incident.setEndedAt(endedAt);
        incident.setPhase(target);
        incident.setClosedAt(closedAt);
        if (request.severity() != null) {
            incident.setSeverity(request.severity());
        }
        if (request.internalNotes() != null) {
            incident.setInternalNotes(blankToNull(request.internalNotes()));
        }
        incident.setResolutionSummary(summary);
        incident.setUpdatedAt(Instant.now());
    }

    private ServiceIncident find(Long id) {
        return incidentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
    }

    private Product requireActiveProduct(Long productId) {
        return productRepository.findById(productId).filter(p -> p.getStatus() == ProductStatus.ACTIVE)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    private record Maps(Map<Long, String> products, Map<Long, String> customers, Map<Long, String> problems) {
    }

    private Maps maps() {
        return new Maps(
            productRepository.findAll().stream().collect(Collectors.toMap(Product::getId, Product::getName, (a, b) -> a)),
            customerRepository.findAll().stream().collect(Collectors.toMap(Customer::getId, c -> (c.getFirstName() + " " + c.getLastName()).trim(), (a, b) -> a)),
            problemRepository.findAll().stream().collect(Collectors.toMap(p -> p.getId(), p -> p.getTitle(), (a, b) -> a)));
    }

    private AdminIncidentDto toDto(ServiceIncident i, Maps maps) {
        List<AdminIncidentDto.LinkedTicket> tickets = i.getTicketIds().isEmpty() ? List.of()
            : ticketRepository.findAllById(i.getTicketIds()).stream().map(this::ticket).toList();
        return new AdminIncidentDto(i.getId(), i.getProductId(), maps.products().get(i.getProductId()), i.getTitle(), i.getMessage(), i.getStartedAt(), i.getEndedAt(),
            i.getEndedAt() == null, i.getSeverity().name(), i.getPhase().name(), i.getInternalNotes(), i.getAssignedToCustomerId(),
            i.getAssignedToCustomerId() == null ? null : maps.customers().get(i.getAssignedToCustomerId()), i.getResolutionSummary(), i.getClosedAt(),
            i.getProblemId(), i.getProblemId() == null ? null : maps.problems().get(i.getProblemId()), tickets);
    }

    private AdminIncidentDto.LinkedTicket ticket(SupportTicket t) {
        return new AdminIncidentDto.LinkedTicket(t.getId(), t.getSubject(), t.getStatus().name(), t.getPriority().name());
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
