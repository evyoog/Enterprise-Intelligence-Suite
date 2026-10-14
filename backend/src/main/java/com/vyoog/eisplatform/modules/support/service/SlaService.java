package com.vyoog.eisplatform.modules.support.service;

import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.notification.model.NotificationCategory;
import com.vyoog.eisplatform.modules.notification.model.NotificationSeverity;
import com.vyoog.eisplatform.modules.notification.service.NotificationService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.service.EmailService;
import com.vyoog.eisplatform.modules.support.dto.SlaDtos;
import com.vyoog.eisplatform.modules.support.model.SlaPolicy;
import com.vyoog.eisplatform.modules.support.model.SlaSettings;
import com.vyoog.eisplatform.modules.support.model.SupportTicket;
import com.vyoog.eisplatform.modules.support.model.TicketPriority;
import com.vyoog.eisplatform.modules.support.model.TicketStatus;
import com.vyoog.eisplatform.modules.support.repository.SlaPolicyRepository;
import com.vyoog.eisplatform.modules.support.repository.SlaSettingsRepository;
import com.vyoog.eisplatform.modules.support.repository.SupportTicketRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * C101 / REQ-SUP-002 (SL-1 B, SL-2 B): support-ticket response and resolution targets. The clock starts at ticket creation and never pauses; the first
 * response is the first human action that takes the ticket out of Open (assign, escalate or resolve); a priority change recalculates both deadlines from
 * the creation time. At the warning point (80% by default) the assignee and the alert list are told; at the deadline the ticket is escalated (priority
 * unchanged) and flagged. Tickets created before this feature have no deadlines.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SlaService {

    private static final Logger log = LoggerFactory.getLogger(SlaService.class);
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s,;]+@[^@\\s,;]+\\.[^@\\s,;]+$");
    private static final int MAX_MINUTES = 60 * 24 * 365;
    private static final Set<TicketStatus> DONE = Set.of(TicketStatus.RESOLVED, TicketStatus.CLOSED);

    private final SlaPolicyRepository policyRepository;
    private final SlaSettingsRepository settingsRepository;
    private final SupportTicketRepository ticketRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final AuditService auditService;

    // ------------------------------------------------------------------ deadlines

    /** Sets both deadlines of a ticket from its creation time and its current priority; clears warning and breach marks that no longer apply. */
    public void applyDeadlines(SupportTicket ticket) {
        Instant from = ticket.getCreatedAt() == null ? Instant.now() : ticket.getCreatedAt();
        SlaSettings settings = settings();
        SlaPolicy policy = policy(ticket.getPriority());
        SlaCalculator calculator = calculator(settings);
        ticket.setFirstResponseDueAt(calculator.add(from, policy.getFirstResponseMinutes(), policy.isAroundTheClock()));
        ticket.setResolutionDueAt(calculator.add(from, policy.getResolutionMinutes(), policy.isAroundTheClock()));
    }

    /** MET / MET_LATE once done, otherwise BREACHED / WARNING / ON_TRACK; null when the ticket has no deadline. */
    public static String firstResponseState(SupportTicket t, Instant now) {
        return state(t.getFirstResponseDueAt(), t.getFirstRespondedAt(), t.getFirstResponseBreachedAt(), t.getFirstResponseWarnedAt(), now);
    }

    public static String resolutionState(SupportTicket t, Instant now) {
        return state(t.getResolutionDueAt(), t.getResolvedAt(), t.getResolutionBreachedAt(), t.getResolutionWarnedAt(), now);
    }

    private static String state(Instant due, Instant doneAt, Instant breachedAt, Instant warnedAt, Instant now) {
        if (due == null) {
            return null;
        }
        if (doneAt != null) {
            return doneAt.isAfter(due) || breachedAt != null ? "MET_LATE" : "MET";
        }
        if (breachedAt != null || now.isAfter(due)) {
            return "BREACHED";
        }
        return warnedAt != null ? "WARNING" : "ON_TRACK";
    }

    // ------------------------------------------------------------------ the scheduled check

    /** Warns at the warning point and escalates at the deadline, for every ticket still open. Returns {warnings, breaches}. */
    @Transactional
    public int[] check(Instant now) {
        SlaSettings settings = settings();
        SlaCalculator calculator = calculator(settings);
        int warnings = 0;
        int breaches = 0;
        for (SupportTicket ticket : ticketRepository.findByStatusNotInAndResolutionDueAtIsNotNull(DONE)) {
            SlaPolicy policy = policy(ticket.getPriority());
            Instant from = ticket.getCreatedAt() == null ? now : ticket.getCreatedAt();
            boolean changed = false;
            if (ticket.getFirstRespondedAt() == null && ticket.getFirstResponseDueAt() != null) {
                Instant warnAt = calculator.addPercent(from, policy.getFirstResponseMinutes(), settings.getWarnAtPercent(), policy.isAroundTheClock());
                if (ticket.getFirstResponseBreachedAt() == null && !now.isBefore(ticket.getFirstResponseDueAt())) {
                    ticket.setFirstResponseBreachedAt(now);
                    breaches++;
                    changed = true;
                    breach(ticket, "first response", ticket.getFirstResponseDueAt(), settings);
                } else if (ticket.getFirstResponseWarnedAt() == null && ticket.getFirstResponseBreachedAt() == null && !now.isBefore(warnAt)) {
                    ticket.setFirstResponseWarnedAt(now);
                    warnings++;
                    changed = true;
                    warn(ticket, "first response", ticket.getFirstResponseDueAt(), settings);
                }
            }
            Instant warnResolution = calculator.addPercent(from, policy.getResolutionMinutes(), settings.getWarnAtPercent(), policy.isAroundTheClock());
            if (ticket.getResolutionBreachedAt() == null && !now.isBefore(ticket.getResolutionDueAt())) {
                ticket.setResolutionBreachedAt(now);
                breaches++;
                changed = true;
                breach(ticket, "resolution", ticket.getResolutionDueAt(), settings);
            } else if (ticket.getResolutionWarnedAt() == null && ticket.getResolutionBreachedAt() == null && !now.isBefore(warnResolution)) {
                ticket.setResolutionWarnedAt(now);
                warnings++;
                changed = true;
                warn(ticket, "resolution", ticket.getResolutionDueAt(), settings);
            }
            if (changed) {
                ticketRepository.save(ticket);
            }
        }
        return new int[]{warnings, breaches};
    }

    private void warn(SupportTicket ticket, String target, Instant due, SlaSettings settings) {
        alert(ticket, settings, NotificationSeverity.WARNING, "Support ticket nearing its " + target + " target",
            "Ticket #" + ticket.getId() + " \"" + ticket.getSubject() + "\" (" + ticket.getPriority() + ") must have its " + target + " by " + due + ".");
        auditService.recordSuccess("TICKET_SLA_WARNING", null, null, null, "SupportTicket", ticket.getId().toString(), null, "SLA warning: " + target);
    }

    private void breach(SupportTicket ticket, String target, Instant due, SlaSettings settings) {
        if (ticket.getStatus() != TicketStatus.ESCALATED) {
            ticket.setStatus(TicketStatus.ESCALATED);   // priority unchanged (SL-1 default)
        }
        alert(ticket, settings, NotificationSeverity.CRITICAL, "Support ticket breached its " + target + " target",
            "Ticket #" + ticket.getId() + " \"" + ticket.getSubject() + "\" (" + ticket.getPriority() + ") missed its " + target + " target (" + due + ") and was escalated.");
        auditService.recordSuccess("TICKET_SLA_BREACHED", null, null, null, "SupportTicket", ticket.getId().toString(), null, "SLA breach: " + target + "; ticket escalated");
    }

    /** The assignee and the alert list, each told once: in the app when they have an account, and by email. */
    private void alert(SupportTicket ticket, SlaSettings settings, NotificationSeverity severity, String title, String message) {
        Set<String> told = new LinkedHashSet<>();
        if (ticket.getAssignedToCustomerId() != null) {
            customerRepository.findById(ticket.getAssignedToCustomerId()).ifPresent(c -> {
                told.add(c.getEmail().toLowerCase(Locale.ROOT));
                notificationService.notify(c.getId(), c.getEmail(), NotificationCategory.SYSTEM, severity, title, message);
            });
        }
        for (String address : emails(settings.getAlertEmails())) {
            if (!told.add(address)) {
                continue;
            }
            java.util.Optional<Customer> customer = customerRepository.findByEmailIgnoreCase(address);
            if (customer.isPresent()) {
                notificationService.notify(customer.get().getId(), address, NotificationCategory.SYSTEM, severity, title, message);
            } else {
                try {
                    emailService.sendNotificationEmail(address, title, message);
                } catch (RuntimeException e) {
                    log.warn("Could not email SLA alert to {}: {}", address, e.getMessage());
                }
            }
        }
    }

    // ------------------------------------------------------------------ configuration

    public SlaDtos.Config config() {
        return new SlaDtos.Config(policies(), toDto(settings()));
    }

    public List<SlaDtos.Policy> policies() {
        List<SlaDtos.Policy> list = new ArrayList<>();
        for (TicketPriority p : TicketPriority.values()) {
            SlaPolicy policy = policy(p);
            list.add(new SlaDtos.Policy(p.name(), policy.getFirstResponseMinutes(), policy.getResolutionMinutes(), policy.isAroundTheClock()));
        }
        return list;
    }

    /** New targets apply to tickets created or re-prioritised from now on; deadlines already set are not moved. */
    @Transactional
    public SlaDtos.Config updatePolicies(String actorSub, SlaDtos.PoliciesRequest request) {
        if (request == null || request.policies() == null || request.policies().isEmpty()) {
            throw new IllegalArgumentException("Send at least one priority.");
        }
        for (SlaDtos.Policy in : request.policies()) {
            TicketPriority priority;
            try {
                priority = TicketPriority.valueOf(in.priority() == null ? "" : in.priority().trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown priority " + in.priority() + ".");
            }
            if (in.firstResponseMinutes() < 1 || in.resolutionMinutes() < 1 || in.firstResponseMinutes() > MAX_MINUTES || in.resolutionMinutes() > MAX_MINUTES) {
                throw new IllegalArgumentException(priority + ": the targets must be between 1 minute and one year.");
            }
            if (in.firstResponseMinutes() > in.resolutionMinutes()) {
                throw new IllegalArgumentException(priority + ": the first response target cannot be longer than the resolution target.");
            }
            SlaPolicy policy = policy(priority);
            policy.setFirstResponseMinutes(in.firstResponseMinutes());
            policy.setResolutionMinutes(in.resolutionMinutes());
            policy.setAroundTheClock(in.aroundTheClock());
            policy.setUpdatedAt(Instant.now());
            policyRepository.save(policy);
        }
        auditService.recordSuccess("SLA_POLICY_UPDATED", actorSub, null, null, "SlaPolicy", "all", null, "SLA targets updated");
        return config();
    }

    @Transactional
    public SlaDtos.Config updateSettings(String actorSub, SlaDtos.Settings in) {
        if (in == null) {
            throw new IllegalArgumentException("Send the settings.");
        }
        Set<DayOfWeek> days = parseDays(in.businessDays());
        LocalTime start = parseTime(in.businessStart(), "Business start");
        LocalTime end = parseTime(in.businessEnd(), "Business end");
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Business hours must start before they end.");
        }
        try {
            ZoneId.of(in.timeZone() == null ? "" : in.timeZone().trim());
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Unknown time zone.");
        }
        if (in.warnAtPercent() < 1 || in.warnAtPercent() > 99) {
            throw new IllegalArgumentException("The warning point must be between 1% and 99%.");
        }
        List<String> emails = in.alertEmails() == null ? List.of() : in.alertEmails().stream().filter(e -> e != null && !e.isBlank()).map(e -> e.trim().toLowerCase(Locale.ROOT)).distinct().toList();
        if (emails.size() > 20) {
            throw new IllegalArgumentException("At most 20 alert addresses.");
        }
        for (String e : emails) {
            if (!EMAIL.matcher(e).matches()) {
                throw new IllegalArgumentException("\"" + e + "\" is not an email address.");
            }
        }
        SlaSettings settings = settings();
        settings.setBusinessDays(String.join(",", days.stream().sorted().map(d -> d.name().substring(0, 3)).toList()));
        settings.setBusinessStart(start);
        settings.setBusinessEnd(end);
        settings.setTimeZone(in.timeZone().trim());
        settings.setWarnAtPercent(in.warnAtPercent());
        settings.setAlertEmails(emails.isEmpty() ? null : String.join(",", emails));
        settings.setUpdatedAt(Instant.now());
        settingsRepository.save(settings);
        auditService.recordSuccess("SLA_SETTINGS_UPDATED", actorSub, null, null, "SlaSettings", "1", null, "SLA business hours / alerts updated");
        return config();
    }

    // ------------------------------------------------------------------ helpers

    SlaSettings settings() {
        return settingsRepository.findById((short) 1).orElseGet(() -> settingsRepository.save(new SlaSettings()));
    }

    SlaPolicy policy(TicketPriority priority) {
        return policyRepository.findById(priority).orElseGet(() -> {
            SlaPolicy p = new SlaPolicy();
            p.setPriority(priority);
            switch (priority) {
                case URGENT -> { p.setFirstResponseMinutes(60); p.setResolutionMinutes(480); p.setAroundTheClock(true); }
                case HIGH -> { p.setFirstResponseMinutes(240); p.setResolutionMinutes(480); }
                case MEDIUM -> { p.setFirstResponseMinutes(480); p.setResolutionMinutes(1440); }
                default -> { p.setFirstResponseMinutes(480); p.setResolutionMinutes(2400); }
            }
            return policyRepository.save(p);
        });
    }

    SlaCalculator calculator(SlaSettings s) {
        return new SlaCalculator(parseDays(List.of(s.getBusinessDays().split(","))), s.getBusinessStart(), s.getBusinessEnd(), ZoneId.of(s.getTimeZone()));
    }

    private static Set<DayOfWeek> parseDays(List<String> in) {
        if (in == null || in.isEmpty()) {
            throw new IllegalArgumentException("Choose at least one business day.");
        }
        Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
        for (String d : in) {
            String key = d == null ? "" : d.trim().toUpperCase(Locale.ROOT);
            DayOfWeek match = null;
            for (DayOfWeek candidate : DayOfWeek.values()) {
                if (key.length() >= 3 && candidate.name().startsWith(key.substring(0, 3)) && candidate.name().startsWith(key)) {
                    match = candidate;
                }
            }
            if (match == null) {
                throw new IllegalArgumentException("Unknown day " + d + ".");
            }
            days.add(match);
        }
        return days;
    }

    private static LocalTime parseTime(String value, String label) {
        try {
            return LocalTime.parse(value == null ? "" : value.trim());
        } catch (DateTimeException e) {
            throw new IllegalArgumentException(label + " must be a time like 09:00.");
        }
    }

    private static List<String> emails(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(csv.split("[,;\\s]+")).map(s -> s.trim().toLowerCase(Locale.ROOT)).filter(s -> !s.isEmpty()).toList();
    }

    private SlaDtos.Settings toDto(SlaSettings s) {
        return new SlaDtos.Settings(List.of(s.getBusinessDays().split(",")), s.getBusinessStart().toString(), s.getBusinessEnd().toString(), s.getTimeZone(),
            s.getWarnAtPercent(), emails(s.getAlertEmails()));
    }
}
