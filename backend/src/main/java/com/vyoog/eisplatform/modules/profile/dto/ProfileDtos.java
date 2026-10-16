package com.vyoog.eisplatform.modules.profile.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * "My profile" (C107). Every record is {@code ALWAYS} included, unlike the platform default of omitting
 * nulls, so the screen receives an explicit {@code null} for an empty field.
 */
public final class ProfileDtos {

    private ProfileDtos() {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Account(String email, String role, String organizationName, String organizationCode, String status,
                          Instant memberSince, boolean nameEditable) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Personal(String firstName, String lastName, String preferredName, String mobile,
                           String alternatePhone, String bio) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Work(String jobTitle, String department, String companyName, String industry) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Address(String addressLine1, String addressLine2, String city, String state, String postalCode,
                          String country) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Online(String linkedinUrl, String websiteUrl) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Photo(boolean present, Instant updatedAt) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record Completion(int percent, int filled, int total, List<String> missing) {
    }

    /** {@code exists} is false only for a signed-in person without a customer row (a platform admin). */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ProfileDto(boolean exists, Account account, Personal personal, Work work, Address address,
                             Online online, Photo photo, Completion completion) {
    }

    /** GET /me/profile/completion: the cheap read behind the global profile bar. */
    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record CompletionResponse(boolean exists, int percent, int filled, int total, List<String> missing) {
    }

    /** Full replace of the four sections. Email, role and photo are never part of it. A missing section is
     * treated as all-empty. */
    public record ProfileUpdateRequest(Personal personal, Work work, Address address, Online online) {
    }
}
