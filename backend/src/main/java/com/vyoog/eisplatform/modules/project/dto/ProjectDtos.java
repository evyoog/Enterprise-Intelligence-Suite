package com.vyoog.eisplatform.modules.project.dto;

import com.vyoog.eisplatform.modules.registration.model.OrgRole;

import java.time.Instant;
import java.util.List;

/** REQ-TEN-009 Projects. */
public final class ProjectDtos {
    private ProjectDtos() {
    }

    public record ProjectRequest(String name, String description) {
    }

    public record ProductRequest(Long productId, String productRole) {
    }

    public record ProjectMemberDto(Long memberId, Long customerId, String name, String email, OrgRole orgRole) {
    }

    public record ProjectProductDto(Long productId, String productName, String productRole) {
    }

    public record ProjectDto(Long id, String name, String description, Instant createdAt,
                             List<ProjectMemberDto> members, List<ProjectProductDto> products) {
    }
}
