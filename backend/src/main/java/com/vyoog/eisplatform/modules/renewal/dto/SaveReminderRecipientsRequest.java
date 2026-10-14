package com.vyoog.eisplatform.modules.renewal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** C100 RR-5: replaces the people an administrator added to the renewal reminders. At least one channel per person. */
public record SaveReminderRecipientsRequest(@NotNull @Size(max = 200) List<@Valid Entry> recipients) {

    public record Entry(@NotNull Long customerId, boolean email, boolean inApp) {
    }
}
