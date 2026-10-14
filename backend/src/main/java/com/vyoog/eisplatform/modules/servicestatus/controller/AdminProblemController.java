package com.vyoog.eisplatform.modules.servicestatus.controller;

import com.vyoog.eisplatform.modules.servicestatus.dto.ProblemDtos;
import com.vyoog.eisplatform.modules.servicestatus.service.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** C101 / REQ-SUP-003 (IC-2 A): problems. Gated by {@code MANAGE_SERVICE_STATUS} (the {@code /admin/service-status/**} rule in SecurityConfig). */
@RestController
@RequestMapping("/admin/service-status/problems")
@RequiredArgsConstructor
public class AdminProblemController {

    private final ProblemService problemService;

    @GetMapping
    public List<ProblemDtos.Problem> list() {
        return problemService.list();
    }

    @PostMapping
    public ProblemDtos.Detail create(@RequestBody ProblemDtos.CreateRequest request, @AuthenticationPrincipal Jwt jwt) {
        return problemService.create(request, jwt.getSubject());
    }

    @GetMapping("/{id}")
    public ProblemDtos.Detail get(@PathVariable Long id) {
        return problemService.get(id);
    }

    @PutMapping("/{id}")
    public ProblemDtos.Detail update(@PathVariable Long id, @RequestBody ProblemDtos.UpdateRequest request, @AuthenticationPrincipal Jwt jwt) {
        return problemService.update(id, request, jwt.getSubject());
    }

    @PostMapping("/{id}/incidents/{incidentId}")
    public ProblemDtos.Detail linkIncident(@PathVariable Long id, @PathVariable Long incidentId, @AuthenticationPrincipal Jwt jwt) {
        return problemService.linkIncident(id, incidentId, jwt.getSubject());
    }

    @DeleteMapping("/{id}/incidents/{incidentId}")
    public ProblemDtos.Detail unlinkIncident(@PathVariable Long id, @PathVariable Long incidentId, @AuthenticationPrincipal Jwt jwt) {
        return problemService.unlinkIncident(id, incidentId, jwt.getSubject());
    }

    @PostMapping("/{id}/actions")
    public ProblemDtos.Detail addAction(@PathVariable Long id, @RequestBody ProblemDtos.ActionRequest request, @AuthenticationPrincipal Jwt jwt) {
        return problemService.addAction(id, request, jwt.getSubject());
    }

    @PutMapping("/{id}/actions/{actionId}")
    public ProblemDtos.Detail updateAction(@PathVariable Long id, @PathVariable Long actionId, @RequestBody ProblemDtos.ActionRequest request,
                                           @AuthenticationPrincipal Jwt jwt) {
        return problemService.updateAction(id, actionId, request, jwt.getSubject());
    }

    @DeleteMapping("/{id}/actions/{actionId}")
    public ProblemDtos.Detail deleteAction(@PathVariable Long id, @PathVariable Long actionId, @AuthenticationPrincipal Jwt jwt) {
        return problemService.deleteAction(id, actionId, jwt.getSubject());
    }
}
