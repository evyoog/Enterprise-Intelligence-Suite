package com.vyoog.eisplatform.modules.servicestatus.service;

import com.vyoog.eisplatform.common.exception.ResourceNotFoundException;
import com.vyoog.eisplatform.modules.audit.service.AuditService;
import com.vyoog.eisplatform.modules.product.model.Product;
import com.vyoog.eisplatform.modules.product.repository.ProductRepository;
import com.vyoog.eisplatform.modules.servicestatus.dto.ProblemDtos;
import com.vyoog.eisplatform.modules.servicestatus.model.IncidentPhase;
import com.vyoog.eisplatform.modules.servicestatus.model.Problem;
import com.vyoog.eisplatform.modules.servicestatus.model.ProblemAction;
import com.vyoog.eisplatform.modules.servicestatus.model.ProblemStatus;
import com.vyoog.eisplatform.modules.servicestatus.model.ServiceIncident;
import com.vyoog.eisplatform.modules.servicestatus.repository.ProblemActionRepository;
import com.vyoog.eisplatform.modules.servicestatus.repository.ProblemRepository;
import com.vyoog.eisplatform.modules.servicestatus.repository.ServiceIncidentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * C101 / REQ-SUP-003 (IC-2 A): a problem is a separate record that links several incidents to one root cause, with corrective actions. Open, Root cause
 * identified, Resolved (needs a root cause), Closed (only after Resolved, and only when every corrective action is done; then final). An incident belongs to at
 * most one problem. Managed with {@code MANAGE_SERVICE_STATUS}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final ProblemActionRepository actionRepository;
    private final ServiceIncidentRepository incidentRepository;
    private final ProductRepository productRepository;
    private final AuditService auditService;

    public List<ProblemDtos.Problem> list() {
        return problemRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toDto).toList();
    }

    public ProblemDtos.Detail get(Long id) {
        return detail(find(id));
    }

    @Transactional
    public ProblemDtos.Detail create(ProblemDtos.CreateRequest request, String actorSub) {
        String title = request.title() == null ? "" : request.title().trim();
        if (title.isEmpty() || title.length() > 200) {
            throw new IllegalArgumentException("Give the problem a title of up to 200 characters.");
        }
        Problem problem = new Problem();
        problem.setTitle(title);
        problem.setDescription(blankToNull(request.description(), 4000));
        problem.setCreatedByKeycloakSub(actorSub);
        problem = problemRepository.save(problem);
        if (request.incidentIds() != null) {
            for (Long incidentId : request.incidentIds()) {
                link(problem, incidentId);
            }
        }
        auditService.recordSuccess("PROBLEM_CREATED", actorSub, null, null, "Problem", problem.getId().toString(), null, "Problem opened: " + title);
        return detail(problem);
    }

    @Transactional
    public ProblemDtos.Detail update(Long id, ProblemDtos.UpdateRequest request, String actorSub) {
        Problem problem = find(id);
        refuseIfClosed(problem);
        if (request.title() != null) {
            String title = request.title().trim();
            if (title.isEmpty() || title.length() > 200) {
                throw new IllegalArgumentException("Give the problem a title of up to 200 characters.");
            }
            problem.setTitle(title);
        }
        if (request.description() != null) {
            problem.setDescription(blankToNull(request.description(), 4000));
        }
        if (request.rootCause() != null) {
            problem.setRootCause(blankToNull(request.rootCause(), 4000));
        }
        ProblemStatus before = problem.getStatus();
        if (request.status() != null && !request.status().isBlank()) {
            moveTo(problem, parseStatus(request.status()));
        }
        problem.setUpdatedAt(Instant.now());
        problem = problemRepository.save(problem);
        auditService.recordSuccess("PROBLEM_UPDATED", actorSub, null, null, "Problem", id.toString(), null,
            problem.getTitle() + " (" + before + (before == problem.getStatus() ? "" : " -> " + problem.getStatus()) + ")");
        return detail(problem);
    }

    @Transactional
    public ProblemDtos.Detail linkIncident(Long problemId, Long incidentId, String actorSub) {
        Problem problem = find(problemId);
        link(problem, incidentId);
        auditService.recordSuccess("PROBLEM_INCIDENT_LINKED", actorSub, null, null, "Problem", problemId.toString(), null, "Incident " + incidentId + " linked");
        return detail(problem);
    }

    @Transactional
    public ProblemDtos.Detail unlinkIncident(Long problemId, Long incidentId, String actorSub) {
        Problem problem = find(problemId);
        refuseIfClosed(problem);
        ServiceIncident incident = incidentRepository.findById(incidentId).orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        if (problemId.equals(incident.getProblemId())) {
            incident.setProblemId(null);
            incidentRepository.save(incident);
            auditService.recordSuccess("PROBLEM_INCIDENT_UNLINKED", actorSub, null, null, "Problem", problemId.toString(), null, "Incident " + incidentId + " unlinked");
        }
        return detail(problem);
    }

    @Transactional
    public ProblemDtos.Detail addAction(Long problemId, ProblemDtos.ActionRequest request, String actorSub) {
        Problem problem = find(problemId);
        refuseIfClosed(problem);
        String description = request.description() == null ? "" : request.description().trim();
        if (description.isEmpty() || description.length() > 500) {
            throw new IllegalArgumentException("Describe the action in up to 500 characters.");
        }
        ProblemAction action = new ProblemAction();
        action.setProblemId(problemId);
        action.setDescription(description);
        action.setOwner(blankToNull(request.owner(), 100));
        action.setDueOn(request.dueOn());
        action.setDone(Boolean.TRUE.equals(request.done()));
        action.setDoneAt(action.isDone() ? Instant.now() : null);
        actionRepository.save(action);
        auditService.recordSuccess("PROBLEM_ACTION_ADDED", actorSub, null, null, "Problem", problemId.toString(), null, "Action: " + description);
        return detail(problem);
    }

    @Transactional
    public ProblemDtos.Detail updateAction(Long problemId, Long actionId, ProblemDtos.ActionRequest request, String actorSub) {
        Problem problem = find(problemId);
        refuseIfClosed(problem);
        ProblemAction action = actionRepository.findById(actionId).filter(a -> a.getProblemId().equals(problemId))
            .orElseThrow(() -> new ResourceNotFoundException("Action not found"));
        if (request.description() != null) {
            String description = request.description().trim();
            if (description.isEmpty() || description.length() > 500) {
                throw new IllegalArgumentException("Describe the action in up to 500 characters.");
            }
            action.setDescription(description);
        }
        if (request.owner() != null) {
            action.setOwner(blankToNull(request.owner(), 100));
        }
        if (request.dueOn() != null) {
            action.setDueOn(request.dueOn());
        }
        if (request.done() != null && request.done() != action.isDone()) {
            action.setDone(request.done());
            action.setDoneAt(request.done() ? Instant.now() : null);
        }
        actionRepository.save(action);
        auditService.recordSuccess("PROBLEM_ACTION_UPDATED", actorSub, null, null, "Problem", problemId.toString(), null,
            "Action " + actionId + (action.isDone() ? " done" : " open"));
        return detail(problem);
    }

    @Transactional
    public ProblemDtos.Detail deleteAction(Long problemId, Long actionId, String actorSub) {
        Problem problem = find(problemId);
        refuseIfClosed(problem);
        ProblemAction action = actionRepository.findById(actionId).filter(a -> a.getProblemId().equals(problemId))
            .orElseThrow(() -> new ResourceNotFoundException("Action not found"));
        actionRepository.delete(action);
        auditService.recordSuccess("PROBLEM_ACTION_DELETED", actorSub, null, null, "Problem", problemId.toString(), null, "Action " + actionId + " removed");
        return detail(problem);
    }

    // ------------------------------------------------------------------ rules

    private void moveTo(Problem problem, ProblemStatus target) {
        ProblemStatus current = problem.getStatus();
        if (target == current) {
            return;
        }
        switch (target) {
            case OPEN, ROOT_CAUSE_IDENTIFIED -> {
                if (target == ProblemStatus.ROOT_CAUSE_IDENTIFIED && problem.getRootCause() == null) {
                    throw new IllegalArgumentException("Write the root cause first.");
                }
                problem.setResolvedAt(null);
            }
            case RESOLVED -> {
                if (problem.getRootCause() == null) {
                    throw new IllegalArgumentException("A problem needs a root cause before it can be resolved.");
                }
                problem.setResolvedAt(Instant.now());
            }
            case CLOSED -> {
                if (current != ProblemStatus.RESOLVED) {
                    throw new IllegalArgumentException("Only a resolved problem can be closed.");
                }
                long open = actionRepository.countByProblemIdAndDoneFalse(problem.getId());
                if (open > 0) {
                    throw new IllegalArgumentException("Finish every corrective action first (" + open + " still open).");
                }
                problem.setClosedAt(Instant.now());
            }
        }
        problem.setStatus(target);
    }

    private void link(Problem problem, Long incidentId) {
        refuseIfClosed(problem);
        ServiceIncident incident = incidentRepository.findById(incidentId).orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        if (incident.getProblemId() != null && !incident.getProblemId().equals(problem.getId())) {
            throw new IllegalArgumentException("Incident " + incidentId + " already belongs to problem " + incident.getProblemId() + ".");
        }
        if (!problem.getId().equals(incident.getProblemId())) {
            incident.setProblemId(problem.getId());
            incidentRepository.save(incident);
        }
    }

    private void refuseIfClosed(Problem problem) {
        if (problem.getStatus() == ProblemStatus.CLOSED) {
            throw new IllegalArgumentException("A closed problem cannot be changed.");
        }
    }

    private static ProblemStatus parseStatus(String value) {
        try {
            return ProblemStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status must be OPEN, ROOT_CAUSE_IDENTIFIED, RESOLVED or CLOSED.");
        }
    }

    private Problem find(Long id) {
        return problemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Problem not found"));
    }

    // ------------------------------------------------------------------ mapping

    private ProblemDtos.Detail detail(Problem problem) {
        Map<Long, String> products = productRepository.findAll().stream().collect(Collectors.toMap(Product::getId, Product::getName, (a, b) -> a));
        List<ProblemDtos.IncidentRef> incidents = incidentRepository.findByProblemIdOrderByStartedAtDesc(problem.getId()).stream()
            .map(i -> new ProblemDtos.IncidentRef(i.getId(), i.getTitle(), products.get(i.getProductId()), i.getPhase().name(), i.getSeverity().name(), i.getStartedAt())).toList();
        List<ProblemDtos.Action> actions = actionRepository.findByProblemIdOrderByIdAsc(problem.getId()).stream()
            .map(a -> new ProblemDtos.Action(a.getId(), a.getDescription(), a.getOwner(), a.getDueOn(), a.isDone(), a.getDoneAt())).toList();
        return new ProblemDtos.Detail(toDto(problem), incidents, actions);
    }

    private ProblemDtos.Problem toDto(Problem p) {
        int total = (int) actionRepository.countByProblemId(p.getId());
        int open = (int) actionRepository.countByProblemIdAndDoneFalse(p.getId());
        return new ProblemDtos.Problem(p.getId(), p.getTitle(), p.getDescription(), p.getStatus().name(), p.getRootCause(),
            (int) incidentRepository.countByProblemId(p.getId()), total, open, p.getCreatedAt(), p.getResolvedAt(), p.getClosedAt());
    }

    private static String blankToNull(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String t = value.trim();
        return t.length() <= max ? t : t.substring(0, max);
    }
}
