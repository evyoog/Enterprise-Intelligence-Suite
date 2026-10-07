package com.vyoog.eisplatform.modules.orghierarchy.controller;

import com.vyoog.eisplatform.modules.orghierarchy.dto.OrgHierarchyDtos.*;
import com.vyoog.eisplatform.modules.orghierarchy.service.OrgHierarchyService;
import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * REQ-TEN-006 Organization hierarchy. Covered by the existing
 * {@code /organization/me/**} authenticated rule; MANAGE_ORGANIZATION and the
 * "own organization only" scoping are enforced in the service (BR-ORG-001/002).
 */
@RestController
@RequestMapping("/organization/me/org-hierarchy")
@RequiredArgsConstructor
public class OrgHierarchyController {

    private final OrgHierarchyService service;
    private final CurrentCustomerResolver currentCustomerResolver;

    private Long caller(Jwt jwt) {
        return currentCustomerResolver.resolve(jwt).getId();
    }

    @GetMapping
    public TreeDto tree(@AuthenticationPrincipal Jwt jwt) {
        return service.tree(caller(jwt));
    }

    @GetMapping("/nodes/{id}")
    public NodeDetailDto detail(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.detail(caller(jwt), id);
    }

    @GetMapping("/nodes/{id}/history")
    public List<HistoryDto> history(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return service.history(caller(jwt), id);
    }

    @PostMapping("/nodes")
    public ResponseEntity<NodeDto> create(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateNodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(caller(jwt), request));
    }

    @PutMapping("/nodes/{id}")
    public NodeDto update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @RequestBody UpdateNodeRequest request) {
        return service.update(caller(jwt), id, request);
    }

    @PatchMapping("/nodes/{id}/move")
    public NodeDto move(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @RequestBody MoveNodeRequest request) {
        return service.move(caller(jwt), id, request.newParentId());
    }

    @DeleteMapping("/nodes/{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        service.delete(caller(jwt), id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/nodes/{id}/members/{memberId}")
    public NodeDetailDto placeMember(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @PathVariable Long memberId) {
        return service.placeMember(caller(jwt), id, memberId);
    }

    @DeleteMapping("/nodes/{id}/members/{memberId}")
    public NodeDetailDto removeMember(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @PathVariable Long memberId) {
        return service.removeMember(caller(jwt), id, memberId);
    }

    @PostMapping("/import")
    public ImportResultDto importCsv(@AuthenticationPrincipal Jwt jwt, @RequestParam("file") MultipartFile file) {
        try {
            return service.importCsv(caller(jwt), file.getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("The file could not be read");
        }
    }

    @GetMapping("/levels")
    public List<LevelDto> levels(@AuthenticationPrincipal Jwt jwt) {
        return service.levels(caller(jwt));
    }

    @PutMapping("/levels")
    public List<LevelDto> updateLevels(@AuthenticationPrincipal Jwt jwt, @RequestBody UpdateLevelsRequest request) {
        return service.updateLevels(caller(jwt), request);
    }
}
