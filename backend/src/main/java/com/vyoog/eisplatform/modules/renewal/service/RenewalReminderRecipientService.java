package com.vyoog.eisplatform.modules.renewal.service;

import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.authorization.service.MemberAccessService;
import com.vyoog.eisplatform.modules.registration.model.Customer;
import com.vyoog.eisplatform.modules.registration.model.MembershipStatus;
import com.vyoog.eisplatform.modules.registration.model.OrgRole;
import com.vyoog.eisplatform.modules.registration.model.OrganizationMember;
import com.vyoog.eisplatform.modules.registration.repository.CustomerRepository;
import com.vyoog.eisplatform.modules.registration.repository.OrganizationMemberRepository;
import com.vyoog.eisplatform.modules.registration.service.OrganizationSelfService;
import com.vyoog.eisplatform.modules.renewal.dto.ReminderRecipientsDto;
import com.vyoog.eisplatform.modules.renewal.dto.SaveReminderRecipientsRequest;
import com.vyoog.eisplatform.modules.renewal.model.RenewalReminderRecipient;
import com.vyoog.eisplatform.modules.renewal.repository.RenewalReminderRecipientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * C100 RR-2 B and RR-5: an organization administrator (anyone with Manage billing) chooses who, besides the organization admins and the people with
 * Manage billing, gets the renewal reminders of the organization's subscriptions, and on which channels (email, in-app, or both).
 */
@Service
@RequiredArgsConstructor
public class RenewalReminderRecipientService {

    private static final String PERMISSION = "MANAGE_ORGANIZATION_BILLING";

    private final OrganizationSelfService organizationSelfService;
    private final OrganizationMemberRepository memberRepository;
    private final CustomerRepository customerRepository;
    private final MemberAccessService memberAccessService;
    private final RenewalReminderRecipientRepository recipientRepository;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public ReminderRecipientsDto get(Long callerCustomerId) {
        return build(organizationSelfService.requireOrganizationPermission(callerCustomerId, PERMISSION));
    }

    /** The active members who could be added: everyone who is not already a default recipient. Lets Manage billing holders pick people without Manage users. */
    @Transactional(readOnly = true)
    public List<ReminderRecipientsDto.Candidate> candidates(Long callerCustomerId) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(callerCustomerId, PERMISSION);
        Set<Long> defaults = new HashSet<>();
        build(organizationId).defaults().forEach(d -> defaults.add(d.customerId()));
        List<ReminderRecipientsDto.Candidate> result = new ArrayList<>();
        for (OrganizationMember m : memberRepository.findByOrganizationId(organizationId)) {
            if (m.getStatus() == MembershipStatus.ACTIVE && !defaults.contains(m.getCustomerId())) {
                customerRepository.findById(m.getCustomerId())
                    .ifPresent(c -> result.add(new ReminderRecipientsDto.Candidate(c.getId(), c.getFirstName() + " " + c.getLastName(), c.getEmail())));
            }
        }
        result.sort(java.util.Comparator.comparing(ReminderRecipientsDto.Candidate::name, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    @Transactional
    public ReminderRecipientsDto save(Long callerCustomerId, SaveReminderRecipientsRequest request) {
        Long organizationId = organizationSelfService.requireOrganizationPermission(callerCustomerId, PERMISSION);
        Set<Long> seen = new HashSet<>();
        List<RenewalReminderRecipient> rows = new ArrayList<>();
        for (SaveReminderRecipientsRequest.Entry entry : request.recipients()) {
            if (!seen.add(entry.customerId())) {
                throw new IllegalArgumentException("A person is listed twice.");
            }
            if (!entry.email() && !entry.inApp()) {
                throw new IllegalArgumentException("Choose email, in-app or both for every person you add.");
            }
            OrganizationMember member = memberRepository.findByOrganizationIdAndCustomerId(organizationId, entry.customerId())
                .filter(m -> m.getStatus() == MembershipStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Only active members of your organization can be added."));
            RenewalReminderRecipient row = new RenewalReminderRecipient();
            row.setOrganizationId(organizationId);
            row.setCustomerId(member.getCustomerId());
            row.setEmailEnabled(entry.email());
            row.setInAppEnabled(entry.inApp());
            row.setCreatedByCustomerId(callerCustomerId);
            rows.add(row);
        }
        recipientRepository.deleteByOrganizationId(organizationId);
        recipientRepository.flush();
        recipientRepository.saveAll(rows);
        auditService.recordSuccess("RENEWAL_REMINDER_RECIPIENTS_CHANGED", null, callerCustomerId, null, "Organization", organizationId.toString(),
            organizationId, rows.size() + " additional renewal reminder recipient(s) set");
        return build(organizationId);
    }

    private ReminderRecipientsDto build(Long organizationId) {
        List<ReminderRecipientsDto.Person> defaults = new ArrayList<>();
        for (OrganizationMember m : memberRepository.findByOrganizationId(organizationId)) {
            if (m.getStatus() != MembershipStatus.ACTIVE) {
                continue;
            }
            String reason = m.getOrgRole() == OrgRole.ORG_ADMIN ? "ORG_ADMIN"
                : memberAccessService.hasPermission(m, PERMISSION) ? "MANAGE_BILLING" : null;
            if (reason != null) {
                customerRepository.findById(m.getCustomerId()).ifPresent(c -> defaults.add(person(c, reason, true, true)));
            }
        }
        Set<Long> defaultIds = new HashSet<>();
        defaults.forEach(d -> defaultIds.add(d.customerId()));
        List<ReminderRecipientsDto.Person> added = new ArrayList<>();
        for (RenewalReminderRecipient row : recipientRepository.findByOrganizationId(organizationId)) {
            if (defaultIds.contains(row.getCustomerId())) {
                continue;
            }
            customerRepository.findById(row.getCustomerId()).ifPresent(c -> added.add(person(c, "ADDED", row.isEmailEnabled(), row.isInAppEnabled())));
        }
        return new ReminderRecipientsDto(defaults, added);
    }

    private static ReminderRecipientsDto.Person person(Customer c, String reason, boolean email, boolean inApp) {
        return new ReminderRecipientsDto.Person(c.getId(), c.getFirstName() + " " + c.getLastName(), c.getEmail(), reason, email, inApp);
    }
}
