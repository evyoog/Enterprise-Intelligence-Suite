package com.vyoog.eisplatform.modules.servicestatus.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.servicestatus.dto.AdminIncidentDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.IncidentDto;
import com.vyoog.eisplatform.modules.servicestatus.dto.IncidentRequest;
import com.vyoog.eisplatform.modules.servicestatus.dto.ProblemDtos;
import com.vyoog.eisplatform.modules.servicestatus.model.IncidentPhase;
import com.vyoog.eisplatform.modules.servicestatus.model.IncidentSeverity;
import com.vyoog.eisplatform.modules.support.dto.CreateTicketRequest;
import com.vyoog.eisplatform.modules.support.service.SupportTicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** C101 / REQ-SUP-003 (IC-1 A, IC-2 A): the support incident is the status-page incident; problems link incidents to a root cause. */
@SpringBootTest
@ActiveProfiles("test")
class IncidentAndProblemTest {

    @Autowired private IncidentManagementService incidents;
    @Autowired private ProblemService problems;
    @Autowired private ServiceStatusService statusService;
    @Autowired private ProductRepository productRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private SupportTicketService ticketService;

    private Product product() {
        Product p = new Product();
        p.setName("Incident-" + System.nanoTime());
        p.setPrice(BigDecimal.TEN);
        return productRepository.save(p);
    }

    private Customer customer() {
        Customer c = new Customer();
        c.setEmail("inc-" + System.nanoTime() + "@incident.example");
        c.setFirstName("Inc");
        c.setLastName("Ident");
        return customerRepository.save(c);
    }

    private IncidentRequest request(Product p, IncidentPhase phase, Instant endedAt, String summary) {
        return new IncidentRequest(p.getId(), "Exports are slow", "Exports take up to 10 minutes.", Instant.now().minus(1, ChronoUnit.HOURS), endedAt,
            IncidentSeverity.MAJOR, phase, "Queue backed up on node 3", null, summary);
    }

    private AdminIncidentDto incident(Product p) {
        return incidents.createIncident(request(p, null, null, null), "admin-sub");
    }

    @Test
    void aNewIncidentIsLoggedAndTheInternalFieldsStayOffTheStatusPage() {
        Product p = product();
        AdminIncidentDto created = incident(p);
        assertThat(created.phase()).isEqualTo("LOGGED");
        assertThat(created.severity()).isEqualTo("MAJOR");
        assertThat(created.internalNotes()).isEqualTo("Queue backed up on node 3");
        assertThat(created.open()).isTrue();

        // The public record has no internal fields at all.
        assertThat(IncidentDto.class.getRecordComponents()).extracting(c -> c.getName())
            .doesNotContain("severity", "phase", "internalNotes", "assignedToCustomerId", "resolutionSummary", "problemId", "tickets");
        assertThat(statusService.adminView().incidents()).anyMatch(i -> i.id().equals(created.id()));
    }

    @Test
    void theIncidentMovesThroughItsPhasesAndResolvedNeedsASummary() {
        Product p = product();
        AdminIncidentDto created = incident(p);
        AdminIncidentDto investigating = incidents.updateIncident(created.id(), request(p, IncidentPhase.INVESTIGATING, null, null), "admin-sub");
        assertThat(investigating.phase()).isEqualTo("INVESTIGATING");

        assertThatThrownBy(() -> incidents.updateIncident(created.id(), request(p, IncidentPhase.RESOLVED, null, " "), "admin-sub"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("resolution summary");
        assertThatThrownBy(() -> incidents.updateIncident(created.id(), request(p, IncidentPhase.CLOSED, null, "x"), "admin-sub"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("resolved");

        AdminIncidentDto resolved = incidents.updateIncident(created.id(), request(p, IncidentPhase.RESOLVED, null, "Restarted the queue worker"), "admin-sub");
        assertThat(resolved.phase()).isEqualTo("RESOLVED");
        assertThat(resolved.endedAt()).isNotNull();
        assertThat(resolved.open()).isFalse();
        assertThat(resolved.resolutionSummary()).isEqualTo("Restarted the queue worker");

        AdminIncidentDto closed = incidents.updateIncident(created.id(), request(p, IncidentPhase.CLOSED, null, null), "admin-sub");
        assertThat(closed.phase()).isEqualTo("CLOSED");
        assertThat(closed.closedAt()).isNotNull();
        assertThatThrownBy(() -> incidents.updateIncident(created.id(), request(p, IncidentPhase.INVESTIGATING, null, null), "admin-sub"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("closed");
    }

    @Test
    void anIncidentThatIsStillBeingInvestigatedCannotHaveAnEndTime() {
        Product p = product();
        AdminIncidentDto created = incident(p);
        assertThatThrownBy(() -> incidents.updateIncident(created.id(), request(p, IncidentPhase.INVESTIGATING, Instant.now(), null), "admin-sub"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aResolvedIncidentCanBeReopenedByRemovingTheEndTime() {
        Product p = product();
        AdminIncidentDto created = incident(p);
        incidents.updateIncident(created.id(), request(p, null, Instant.now(), "Fixed"), "admin-sub");   // phase worked out from the end time
        AdminIncidentDto reopened = incidents.updateIncident(created.id(), request(p, null, null, null), "admin-sub");
        assertThat(reopened.phase()).isEqualTo("INVESTIGATING");
        assertThat(reopened.open()).isTrue();
    }

    @Test
    void assigneeAndTicketsAreLinkedAndAMissingOneIsRejected() {
        Product p = product();
        Customer agent = customer();
        Customer requester = customer();
        AdminIncidentDto created = incidents.createIncident(new IncidentRequest(p.getId(), "Login failing", "Users cannot log in.", Instant.now(), null,
            IncidentSeverity.CRITICAL, null, null, agent.getId(), null), "admin-sub");
        assertThat(created.assignedToName()).contains("Inc");

        var ticket = ticketService.createTicket(requester.getId(), new CreateTicketRequest("Cannot log in", "It fails"));
        AdminIncidentDto linked = incidents.linkTicket(created.id(), ticket.id(), "admin-sub");
        assertThat(linked.tickets()).extracting(AdminIncidentDto.LinkedTicket::id).containsExactly(ticket.id());
        assertThat(incidents.linkTicket(created.id(), ticket.id(), "admin-sub").tickets()).hasSize(1);   // linking twice changes nothing
        assertThat(incidents.unlinkTicket(created.id(), ticket.id(), "admin-sub").tickets()).isEmpty();

        assertThatThrownBy(() -> incidents.linkTicket(created.id(), -5L, "admin-sub")).isInstanceOf(ResourceNotFoundException.class);
        assertThatThrownBy(() -> incidents.createIncident(new IncidentRequest(p.getId(), "T", "M", Instant.now(), null, null, null, null, -9L, null), "admin-sub"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ------------------------------------------------------------------ problems

    @Test
    void aProblemLinksSeveralIncidentsAndAnIncidentBelongsToOnlyOne() {
        Product p = product();
        AdminIncidentDto a = incident(p);
        AdminIncidentDto b = incident(p);
        ProblemDtos.Detail problem = problems.create(new ProblemDtos.CreateRequest("Queue worker leaks memory", "Seen on three nights", List.of(a.id(), b.id())), "admin-sub");
        assertThat(problem.problem().status()).isEqualTo("OPEN");
        assertThat(problem.incidents()).extracting(ProblemDtos.IncidentRef::id).containsExactlyInAnyOrder(a.id(), b.id());
        assertThat(incidents.get(a.id()).problemId()).isEqualTo(problem.problem().id());
        assertThat(incidents.get(a.id()).problemTitle()).isEqualTo("Queue worker leaks memory");

        ProblemDtos.Detail other = problems.create(new ProblemDtos.CreateRequest("Other", null, null), "admin-sub");
        assertThatThrownBy(() -> problems.linkIncident(other.problem().id(), a.id(), "admin-sub"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already belongs");

        ProblemDtos.Detail unlinked = problems.unlinkIncident(problem.problem().id(), b.id(), "admin-sub");
        assertThat(unlinked.incidents()).extracting(ProblemDtos.IncidentRef::id).containsExactly(a.id());
        assertThat(problems.linkIncident(other.problem().id(), b.id(), "admin-sub").incidents()).hasSize(1);   // free again
    }

    @Test
    void aProblemNeedsARootCauseToBeResolvedAndEveryActionDoneToBeClosed() {
        ProblemDtos.Detail created = problems.create(new ProblemDtos.CreateRequest("Disk fills up", null, null), "admin-sub");
        Long id = created.problem().id();
        assertThatThrownBy(() -> problems.update(id, new ProblemDtos.UpdateRequest(null, null, null, "RESOLVED"), "admin-sub"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("root cause");
        assertThatThrownBy(() -> problems.update(id, new ProblemDtos.UpdateRequest(null, null, null, "CLOSED"), "admin-sub"))
            .isInstanceOf(IllegalArgumentException.class);

        ProblemDtos.Detail identified = problems.update(id, new ProblemDtos.UpdateRequest(null, null, "Log rotation was off", "ROOT_CAUSE_IDENTIFIED"), "admin-sub");
        assertThat(identified.problem().status()).isEqualTo("ROOT_CAUSE_IDENTIFIED");

        ProblemDtos.Detail withAction = problems.addAction(id, new ProblemDtos.ActionRequest("Turn log rotation on", "ops", null, false), "admin-sub");
        Long actionId = withAction.actions().get(0).id();
        ProblemDtos.Detail resolved = problems.update(id, new ProblemDtos.UpdateRequest(null, null, null, "RESOLVED"), "admin-sub");
        assertThat(resolved.problem().status()).isEqualTo("RESOLVED");
        assertThat(resolved.problem().resolvedAt()).isNotNull();
        assertThat(resolved.problem().openActionCount()).isEqualTo(1);

        assertThatThrownBy(() -> problems.update(id, new ProblemDtos.UpdateRequest(null, null, null, "CLOSED"), "admin-sub"))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("corrective action");

        ProblemDtos.Detail done = problems.updateAction(id, actionId, new ProblemDtos.ActionRequest(null, null, null, true), "admin-sub");
        assertThat(done.actions().get(0).done()).isTrue();
        assertThat(done.actions().get(0).doneAt()).isNotNull();
        ProblemDtos.Detail closed = problems.update(id, new ProblemDtos.UpdateRequest(null, null, null, "CLOSED"), "admin-sub");
        assertThat(closed.problem().status()).isEqualTo("CLOSED");
        assertThat(closed.problem().closedAt()).isNotNull();

        assertThatThrownBy(() -> problems.update(id, new ProblemDtos.UpdateRequest("New title", null, null, null), "admin-sub")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> problems.addAction(id, new ProblemDtos.ActionRequest("More", null, null, null), "admin-sub")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void aClosedProblemTakesNoMoreIncidentsAndAProblemMustHaveATitle() {
        Product p = product();
        ProblemDtos.Detail created = problems.create(new ProblemDtos.CreateRequest("Done quickly", null, null), "admin-sub");
        Long id = created.problem().id();
        problems.update(id, new ProblemDtos.UpdateRequest(null, null, "Typo in config", "RESOLVED"), "admin-sub");
        problems.update(id, new ProblemDtos.UpdateRequest(null, null, null, "CLOSED"), "admin-sub");
        AdminIncidentDto i = incident(p);
        assertThatThrownBy(() -> problems.linkIncident(id, i.id(), "admin-sub")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> problems.create(new ProblemDtos.CreateRequest("  ", null, null), "admin-sub")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> problems.get(-1L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void theProblemListShowsCountsOfIncidentsAndOpenActions() {
        Product p = product();
        AdminIncidentDto a = incident(p);
        ProblemDtos.Detail created = problems.create(new ProblemDtos.CreateRequest("Counted", null, List.of(a.id())), "admin-sub");
        problems.addAction(created.problem().id(), new ProblemDtos.ActionRequest("One", null, null, null), "admin-sub");
        problems.addAction(created.problem().id(), new ProblemDtos.ActionRequest("Two", null, null, true), "admin-sub");
        ProblemDtos.Problem row = problems.list().stream().filter(x -> x.id().equals(created.problem().id())).findFirst().orElseThrow();
        assertThat(row.incidentCount()).isEqualTo(1);
        assertThat(row.actionCount()).isEqualTo(2);
        assertThat(row.openActionCount()).isEqualTo(1);
    }
}
