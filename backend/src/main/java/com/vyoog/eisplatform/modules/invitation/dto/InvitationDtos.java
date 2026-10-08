package com.vyoog.eisplatform.modules.invitation.dto;

import java.time.Instant;
import java.util.List;

/** REQ-TEN-008 payloads. No token is ever part of a response. */
public final class InvitationDtos {

    private InvitationDtos() {
    }

    public record CreateInvitationRequest(String email, String orgRole, Long orgNodeId) {
    }

    public record InvitationDto(Long id, String email, String status, String orgRole, Long orgNodeId, String orgNodeName,
                                Long invitedByCustomerId, String invitedByName, Instant invitedAt, Instant expiresAt,
                                Instant acceptedAt, Instant declinedAt, Instant revokedAt, Instant lastSentAt, int sendCount,
                                String acceptedByName) {
    }

    public record CreateResult(InvitationDto invitation, boolean emailSent) {
    }

    public record NodeOption(Long id, String name, String type, String path) {
    }

    public record InvitePermissionRequest(boolean allowed) {
    }

    public record Preview(String status, String email, String organizationName, String inviterName, String orgRole,
                          String orgNodeName, Instant expiresAt, boolean accountExists, boolean organizationAvailable) {
    }

    public record AccountRequest(String firstName, String lastName, String password, String confirmPassword) {
    }

    public record AcceptResult(boolean accepted, String organizationName) {
    }

    public record InvitersDto(List<Long> memberIds) {
    }
}
