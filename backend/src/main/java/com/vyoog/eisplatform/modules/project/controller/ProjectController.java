package com.vyoog.eisplatform.modules.project.controller;

import com.vyoog.eisplatform.modules.registration.service.CurrentCustomerResolver;
import com.vyoog.eisplatform.modules.project.dto.ProjectDtos.*;
import com.vyoog.eisplatform.modules.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REQ-TEN-009 Projects. Covered by the existing {@code /organization/me/**} authenticated rule;
 * MANAGE_USERS, MANAGE_PRODUCT_ACCESS and the "own organization only" scoping are enforced in the service.
 */
@RestController
@RequestMapping("/organization/me/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService service;
    private final CurrentCustomerResolver currentCustomerResolver;

    private Long caller(Jwt jwt) {
        return currentCustomerResolver.resolve(jwt).getId();
    }

    @GetMapping
    public List<ProjectDto> list(@AuthenticationPrincipal Jwt jwt) {
        return service.list(caller(jwt));
    }

    @PostMapping
    public ResponseEntity<ProjectDto> create(@AuthenticationPrincipal Jwt jwt, @RequestBody ProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(caller(jwt), request));
    }

    @PutMapping("/{id}")
    public ProjectDto update(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @RequestBody ProjectRequest request) {
        return service.update(caller(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        service.delete(caller(jwt), id);
    }

    @PostMapping("/{id}/members/{memberId}")
    public ProjectDto addMember(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @PathVariable Long memberId) {
        return service.addMember(caller(jwt), id, memberId);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ProjectDto removeMember(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @PathVariable Long memberId) {
        return service.removeMember(caller(jwt), id, memberId);
    }

    @PutMapping("/{id}/products")
    public ProjectDto setProduct(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @RequestBody ProductRequest request) {
        return service.setProduct(caller(jwt), id, request);
    }

    @DeleteMapping("/{id}/products/{productId}")
    public ProjectDto removeProduct(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id, @PathVariable Long productId) {
        return service.removeProduct(caller(jwt), id, productId);
    }
}
