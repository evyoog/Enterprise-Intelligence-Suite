package com.vyoog.eisplatform.modules.support.service;

import com.vyoog.eisplatform.modules.notification.model.Notification;
import com.vyoog.eisplatform.modules.notification.repository.NotificationRepository;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.support.dto.CreateTicketRequest;
import com.vyoog.eisplatform.modules.support.dto.ResolveTicketRequest;
import com.vyoog.eisplatform.modules.support.dto.SlaDtos;
import com.vyoog.eisplatform.modules.support.dto.SupportTicketDto;
import com.vyoog.eisplatform.modules.support.dto.UpdateTicketRequest;
import com.vyoog.eisplatform.modules.support.model.SupportTicket;
import com.vyoog.eisplatform.modules.support.model.TicketPriority;
import com.vyoog.eisplatform.modules.support.model.TicketStatus;
import com.vyoog.eisplatform.modules.support.repository.SupportTicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** C101 / REQ-SUP-002 (SL-1 B, SL-2 B): deadlines on tickets, first response, warnings, breaches, and the settings. The scheduled job is off in tests; the check is called with a later "now". */
@SpringBootTest
@ActiveProfiles("test")
class SlaServiceTest {

    @Autowired private SlaService slaService;
    @Autowired private SupportTicketService ticketService;
    @Autowired private SupportTicketRepository ticketRepository;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private NotificationRepository notificationRepository;

    private Customer customer(String name) {
        Customer c = new Customer();
        c.setEmail(name + "-" + System.nanoTime() + "@sla.example");
        c.setFirstName("Sla");
        c.setLastName(name);
        return customerRepository.save(c);
    }

    /** An urgent ticket (24x7: first response 60 min, resolution 480 min), so the timings do not depend on the time of day the test runs. */
    private SupportTicketDto urgentTicket(Customer requester) {
        SupportTicketDto created = ticketService.createTicket(requester.getId(), new CreateTicketRequest("Down", "Everything is down"));
        return ticketService.updateTicket(created.id(), new UpdateTicketRequest(null, TicketPriority.URGENT, null));
    }

    private SupportTicket reload(Long id) {
        return ticketRepository.findById(id).orElseThrow();
    }

    @Test
    void aNewTicketGetsBothDeadlinesFromItsCreationTime() {
        SupportTicketDto ticket = ticketService.createTicket(customer("a").getId(), new CreateTicketRequest("Subject", "Description"));
        SupportTicket stored = reload(ticket.id());
        assertThat(stored.getFirstResponseDueAt()).isAfter(stored.getCreatedAt());
        assertThat(stored.getResolutionDueAt()).isAfter(stored.getFirstResponseDueAt());
        assertThat(stored.getFirstRespondedAt()).isNull();
    }

    @Test
    void theRequesterSeesTheDeadlinesButNotTheSlaState() {
        Customer requester = customer("b");
        SupportTicketDto ticket = ticketService.createTicket(requester.getId(), new CreateTicketRequest("Subject", "Description"));
        SupportTicketDto mine = ticketService.getMyTicket(requester.getId(), ticket.id());
        assertThat(mine.firstResponseDueAt()).isNotNull();
        assertThat(mine.firstResponseState()).isNull();
        assertThat(ticketService.getTicket(ticket.id()).firstResponseState()).isEqualTo("ON_TRACK");
        assertThat(ticketService.getTicket(ticket.id()).resolutionState()).isEqualTo("ON_TRACK");
    }

    @Test
    void changingThePriorityRecalculatesTheDeadlinesFromTheCreationTime() {
        SupportTicketDto ticket = ticketService.createTicket(customer("c").getId(), new CreateTicketRequest("Subject", "Description"));
        SupportTicket before = reload(ticket.id());
        ticketService.updateTicket(ticket.id(), new UpdateTicketRequest(null, TicketPriority.URGENT, null));
        SupportTicket after = reload(ticket.id());
        assertThat(Duration.between(after.getCreatedAt(), after.getFirstResponseDueAt())).isEqualTo(Duration.ofMinutes(60));
        assertThat(Duration.between(after.getCreatedAt(), after.getResolutionDueAt())).isEqualTo(Duration.ofMinutes(480));
        assertThat(after.getFirstResponseDueAt()).isNotEqualTo(before.getFirstResponseDueAt());
    }

    @Test
    void theFirstResponseIsTheFirstTimeAPersonTakesTheTicketOutOfOpen() {
        SupportTicketDto categorized = ticketService.createTicket(customer("d").getId(), new CreateTicketRequest("Subject", "Description"));
        ticketService.updateTicket(categorized.id(), new UpdateTicketRequest("Billing", null, null));   // categorizing is not a response
        assertThat(reload(categorized.id()).getFirstRespondedAt()).isNull();
        ticketService.updateTicket(categorized.id(), new UpdateTicketRequest(null, null, customer("agent").getId()));
        Instant first = reload(categorized.id()).getFirstRespondedAt();
        assertThat(first).isNotNull();
        ticketService.escalateTicket(categorized.id());
        assertThat(reload(categorized.id()).getFirstRespondedAt()).isEqualTo(first);   // the first one stays

        SupportTicketDto resolvedStraightAway = ticketService.createTicket(customer("e").getId(), new CreateTicketRequest("Subject", "Description"));
        ticketService.resolveTicket(resolvedStraightAway.id(), new ResolveTicketRequest("Fixed"));
        assertThat(reload(resolvedStraightAway.id()).getFirstRespondedAt()).isNotNull();
        assertThat(ticketService.getTicket(resolvedStraightAway.id()).resolutionState()).isEqualTo("MET");
    }

    @Test
    void aWarningGoesOutOnceAtTheWarningPointToTheAssigneeAndTheAlertList() {
        Customer assignee = customer("assignee");
        Customer listed = customer("listed");
        SupportTicketDto ticket = urgentTicket(customer("f"));
        ticketService.updateTicket(ticket.id(), new UpdateTicketRequest(null, null, assignee.getId()));
        // assigned = first response met, so only the resolution clock is left: warn at 80% of 480 min = 384 min.
        slaService.updateSettings("admin-sub", settings(listed.getEmail()));

        Instant now = Instant.now();
        slaService.check(now.plus(Duration.ofMinutes(100)));
        assertThat(reload(ticket.id()).getResolutionWarnedAt()).isNull();

        slaService.check(now.plus(Duration.ofMinutes(400)));
        SupportTicket warned = reload(ticket.id());
        assertThat(warned.getResolutionWarnedAt()).isNotNull();
        assertThat(warned.getResolutionBreachedAt()).isNull();
        assertThat(warned.getStatus()).isNotEqualTo(TicketStatus.ESCALATED);   // a warning does not escalate
        assertThat(titles(assignee)).anyMatch(t -> t.contains("nearing"));
        assertThat(titles(listed)).anyMatch(t -> t.contains("nearing"));
        assertThat(ticketService.getTicket(ticket.id()).resolutionState()).isEqualTo("WARNING");

        long before = titles(assignee).stream().filter(t -> t.contains("nearing")).count();
        slaService.check(now.plus(Duration.ofMinutes(410)));
        assertThat(titles(assignee).stream().filter(t -> t.contains("nearing")).count()).isEqualTo(before);   // not again
    }

    @Test
    void aBreachEscalatesTheTicketKeepsThePriorityAndFlagsIt() {
        Customer listed = customer("listed2");
        slaService.updateSettings("admin-sub", settings(listed.getEmail()));
        SupportTicketDto ticket = urgentTicket(customer("g"));   // still Open: the first response is late first
        slaService.check(Instant.now().plus(Duration.ofMinutes(70)));

        SupportTicket stored = reload(ticket.id());
        assertThat(stored.getFirstResponseBreachedAt()).isNotNull();
        assertThat(stored.getStatus()).isEqualTo(TicketStatus.ESCALATED);
        assertThat(stored.getPriority()).isEqualTo(TicketPriority.URGENT);
        assertThat(stored.getFirstRespondedAt()).isNull();   // the automatic escalation is not a response
        assertThat(titles(listed)).anyMatch(t -> t.contains("breached"));
        assertThat(ticketService.getTicket(ticket.id()).firstResponseState()).isEqualTo("BREACHED");

        // A person then answers: the response is recorded, late.
        ticketService.updateTicket(ticket.id(), new UpdateTicketRequest(null, null, customer("agent2").getId()));
        assertThat(reload(ticket.id()).getFirstRespondedAt()).isNotNull();
        assertThat(ticketService.getTicket(ticket.id()).firstResponseState()).isEqualTo("MET_LATE");   // answered after the breach was recorded
    }

    @Test
    void aResolvedTicketIsNotWatchedAnyMore() {
        SupportTicketDto ticket = urgentTicket(customer("h"));
        ticketService.resolveTicket(ticket.id(), new ResolveTicketRequest("Done"));
        slaService.check(Instant.now().plus(Duration.ofDays(3)));
        SupportTicket stored = reload(ticket.id());
        assertThat(stored.getResolutionBreachedAt()).isNull();
        assertThat(stored.getStatus()).isEqualTo(TicketStatus.RESOLVED);
    }

    @Test
    void theTargetsAndBusinessHoursCanBeEditedAndAreValidated() {
        SlaDtos.Config config = slaService.config();
        assertThat(config.policies()).extracting(SlaDtos.Policy::priority).containsExactly("LOW", "MEDIUM", "HIGH", "URGENT");
        assertThat(config.policies().stream().filter(p -> p.priority().equals("URGENT")).findFirst().orElseThrow().aroundTheClock()).isTrue();

        assertThatThrownBy(() -> slaService.updatePolicies("a", new SlaDtos.PoliciesRequest(List.of(new SlaDtos.Policy("HIGH", 600, 300, false)))))
            .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("first response");
        assertThatThrownBy(() -> slaService.updatePolicies("a", new SlaDtos.PoliciesRequest(List.of(new SlaDtos.Policy("NOPE", 10, 20, false)))))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> slaService.updateSettings("a", new SlaDtos.Settings(List.of(), "09:00", "18:00", "Asia/Kolkata", 80, List.of())))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> slaService.updateSettings("a", new SlaDtos.Settings(List.of("MON"), "18:00", "09:00", "Asia/Kolkata", 80, List.of())))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> slaService.updateSettings("a", new SlaDtos.Settings(List.of("MON"), "09:00", "18:00", "Mars/Olympus", 80, List.of())))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> slaService.updateSettings("a", new SlaDtos.Settings(List.of("MON"), "09:00", "18:00", "Asia/Kolkata", 100, List.of())))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> slaService.updateSettings("a", new SlaDtos.Settings(List.of("MON"), "09:00", "18:00", "Asia/Kolkata", 80, List.of("not-an-email"))))
            .isInstanceOf(IllegalArgumentException.class);

        SlaDtos.Config saved = slaService.updateSettings("admin-sub", new SlaDtos.Settings(List.of("MON", "TUE", "WED", "THU", "FRI"), "09:00", "18:00", "Asia/Kolkata", 80, List.of("Ops@Example.com", "ops@example.com")));
        assertThat(saved.settings().alertEmails()).containsExactly("ops@example.com");
        assertThat(saved.settings().businessDays()).containsExactly("MON", "TUE", "WED", "THU", "FRI");
    }

    private SlaDtos.Settings settings(String alertEmail) {
        return new SlaDtos.Settings(List.of("MON", "TUE", "WED", "THU", "FRI"), "09:00", "18:00", "Asia/Kolkata", 80, List.of(alertEmail));
    }

    private List<String> titles(Customer c) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(c.getId()).stream().map(Notification::getTitle).toList();
    }
}
