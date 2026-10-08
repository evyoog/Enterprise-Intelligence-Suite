package com.vyoog.eisplatform.modules.orghierarchy.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/** Organization hierarchy payloads (REQ-TEN-006). */
public final class OrgHierarchyDtos {

    private OrgHierarchyDtos() {
    }

    public record LevelDto(String type, String label, int rank) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record NodeDto(Long id, Long parentId, String name, String type, String code, String description,
                          int sortOrder, boolean active, int childCount, int memberCount,
                          Instant createdAt, Instant updatedAt) {
    }

    public record TreeDto(List<LevelDto> levels, List<NodeDto> nodes) {
    }

    public record NodeMemberDto(Long memberId, Long customerId, String firstName, String lastName, String email,
                                String status) {
    }

    public record NodeDetailDto(NodeDto node, List<String> path, List<NodeMemberDto> members) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record HistoryDto(Long id, Long previousParentId, String previousParentName, Long newParentId,
                             String newParentName, Long changedByCustomerId, Instant effectiveAt) {
    }

    public record CreateNodeRequest(Long parentId, String name, String type, String code, String description,
                                    Integer sortOrder) {
    }

    public record UpdateNodeRequest(String name, String type, String code, String description, Integer sortOrder,
                                    Boolean active, Boolean force) {
    }

    public record MoveNodeRequest(Long newParentId) {
    }

    public record LevelInput(String type, String label) {
    }

    public record UpdateLevelsRequest(List<LevelInput> levels) {
    }

    public record ImportError(int row, String message) {
    }

    public record ImportResultDto(int created, int failed, List<ImportError> errors) {
    }
}
