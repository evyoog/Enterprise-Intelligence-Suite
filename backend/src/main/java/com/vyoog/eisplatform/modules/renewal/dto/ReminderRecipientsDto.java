package com.vyoog.eisplatform.modules.renewal.dto;

import java.util.List;

/** C100 RR-2 B and RR-5: who gets the renewal reminders of an organization's subscriptions. {@code defaults} always get them (organization admins and people
 * with Manage billing); {@code added} are the people an administrator chose, each with the channels they get them on. */
public record ReminderRecipientsDto(List<Person> defaults, List<Person> added) {

    /** An active member of the organization who can be added to the reminders. */
    public record Candidate(Long customerId, String name, String email) {
    }

    /** {@code reason}: ORG_ADMIN, MANAGE_BILLING or ADDED. */
    public record Person(Long customerId, String name, String email, String reason, boolean emailEnabled, boolean inAppEnabled) {
    }
}
