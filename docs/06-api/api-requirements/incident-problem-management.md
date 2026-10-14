# API requirements — Incident and problem management

Requirement: [REQ-SUP-003](../../02-requirements/FRD/incident-problem-management/requirement.md). Business rules: [business-rules.md](../../02-requirements/FRD/incident-problem-management/business-rules.md). Decision: [C101](../../01-business/roadmap/open-decisions.md#c101) (IC-1 A, IC-2 A). Paths are relative to `/api`. Error bodies are `{"message": "…"}` from `GlobalExceptionHandler`. Data model: [service-instances-sla-incidents.md](../../07-database/data-model/service-instances-sla-incidents.md#incidents-and-problems-req-sup-003).

**Permission.** Every endpoint below is under `/admin/service-status/**`, gated by `SecurityConfig` with the platform permission **`MANAGE_SERVICE_STATUS`** (seeded for `ADMIN`): 401 without a token, 403 without the permission. Validation failures are **400** with the message given in [business-rules.md](../../02-requirements/FRD/incident-problem-management/business-rules.md).

## Incidents
An incident here is the status-page incident ([REQ-PRT-001](../../02-requirements/FRD/service-status-page/requirement.md)); the paths `POST` and `PUT /admin/service-status/incidents…` are the ones the status page already had, now returning the administrator record `AdminIncidentDto`.

| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/admin/service-status/incidents` | The latest 200 incidents by start time, with the internal fields | 200 `AdminIncidentDto[]` | 401, 403 |
| GET | `/admin/service-status/incidents/{incidentId}` | One incident | 200 `AdminIncidentDto` | 401, 403, 404 "Incident not found" |
| POST | `/admin/service-status/incidents` | Log (post) an incident; body `IncidentRequest`; a new incident is `LOGGED` unless a phase is sent; audited `INCIDENT_POSTED` | 200 `AdminIncidentDto` | 400, 401, 403, 404 "Product not found" |
| PUT | `/admin/service-status/incidents/{incidentId}` | Update, investigate, resolve, close or reopen; body `IncidentRequest`; audited `INCIDENT_UPDATED` | 200 `AdminIncidentDto` | 400, 401, 403, 404 |
| POST | `/admin/service-status/incidents/{incidentId}/tickets/{ticketId}` | Link a support ticket (no body); audited `INCIDENT_TICKET_LINKED` | 200 `AdminIncidentDto` | 400 (closed incident), 401, 403, 404 "Incident not found" / "Ticket not found" |
| DELETE | `/admin/service-status/incidents/{incidentId}/tickets/{ticketId}` | Unlink a ticket; audited `INCIDENT_TICKET_UNLINKED` | 200 `AdminIncidentDto` | 400 (closed incident), 401, 403, 404 |

```json
// POST /admin/service-status/incidents
{ "productId": 3, "title": "Exports are slow", "message": "Exports take up to 10 minutes.",
  "startedAt": "2027-02-10T08:15:00Z", "endedAt": null,
  "severity": "MAJOR", "phase": null, "internalNotes": "Queue backed up on node 3",
  "assignedToCustomerId": 41, "resolutionSummary": null }
```
`IncidentRequest`: **public** `productId` (required), `title` (required, up to 200), `message` (required, up to 4000), `startedAt` (required), `endedAt` (null = open); **internal and optional** `severity` (`MINOR`, `MAJOR`, `CRITICAL`), `phase` (`LOGGED`, `INVESTIGATING`, `RESOLVED`, `CLOSED`), `internalNotes` (up to 4000), `assignedToCustomerId`, `resolutionSummary` (up to 2000). A field left out keeps its value; with `phase` left out it is worked out from `endedAt` ([BR-INC-005](../../02-requirements/FRD/incident-problem-management/business-rules.md)).

```json
// 200 AdminIncidentDto
{ "id": 9, "productId": 3, "productName": "Valam.ai", "title": "Exports are slow",
  "message": "Exports take up to 10 minutes.", "startedAt": "2027-02-10T08:15:00Z", "endedAt": null, "open": true,
  "severity": "MAJOR", "phase": "LOGGED", "internalNotes": "Queue backed up on node 3",
  "assignedToCustomerId": 41, "assignedToName": "Support Agent", "resolutionSummary": null, "closedAt": null,
  "problemId": null, "problemTitle": null,
  "tickets": [ { "id": 12, "subject": "Can't export", "status": "IN_PROGRESS", "priority": "HIGH" } ] }
```

### What customers get
`GET /me/service-status` returns for each incident only `IncidentDto` = `id, productId, productName, title, message, startedAt, endedAt, open`, and only for products the customer purchased. It never contains severity, phase, internal notes, assignee, resolution summary, tickets or problem.

## Problems
| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/admin/service-status/problems` | Every problem, newest first, with counts | 200 `Problem[]` | 401, 403 |
| POST | `/admin/service-status/problems` | Create a problem, optionally linking incidents; body `{ "title": "…", "description": "…", "incidentIds": [9, 10] }`; audited `PROBLEM_CREATED` | 200 `Detail` | 400 (title; an incident already in a problem), 401, 403, 404 "Incident not found" |
| GET | `/admin/service-status/problems/{id}` | One problem with its incidents and actions | 200 `Detail` | 401, 403, 404 "Problem not found" |
| PUT | `/admin/service-status/problems/{id}` | Change title, description, root cause and/or status; body `UpdateRequest` (fields left null keep their value); audited `PROBLEM_UPDATED` | 200 `Detail` | 400 (rules of BR-INC-012, 013), 401, 403, 404 |
| POST | `/admin/service-status/problems/{id}/incidents/{incidentId}` | Link an incident; audited `PROBLEM_INCIDENT_LINKED` | 200 `Detail` | 400 (already in another problem; closed problem), 401, 403, 404 |
| DELETE | `/admin/service-status/problems/{id}/incidents/{incidentId}` | Unlink an incident; audited `PROBLEM_INCIDENT_UNLINKED` | 200 `Detail` | 400 (closed problem), 401, 403, 404 |
| POST | `/admin/service-status/problems/{id}/actions` | Add a corrective action; body `ActionRequest`; audited `PROBLEM_ACTION_ADDED` | 200 `Detail` | 400, 401, 403, 404 |
| PUT | `/admin/service-status/problems/{id}/actions/{actionId}` | Change an action (fields left null keep their value; `done` marks it done or open); audited `PROBLEM_ACTION_UPDATED` | 200 `Detail` | 400, 401, 403, 404 "Action not found" |
| DELETE | `/admin/service-status/problems/{id}/actions/{actionId}` | Delete an action; audited `PROBLEM_ACTION_DELETED` | 200 `Detail` | 400 (closed problem), 401, 403, 404 |

```json
// PUT /admin/service-status/problems/4  (one call: root cause and the move)
{ "rootCause": "Log rotation was off", "status": "ROOT_CAUSE_IDENTIFIED" }
// POST /admin/service-status/problems/4/actions
{ "description": "Turn log rotation on", "owner": "ops", "dueOn": "2027-02-20", "done": false }
```
`UpdateRequest`: `title`, `description`, `rootCause`, `status` (`OPEN`, `ROOT_CAUSE_IDENTIFIED`, `RESOLVED`, `CLOSED`; case-insensitive; else 400 "Status must be OPEN, ROOT_CAUSE_IDENTIFIED, RESOLVED or CLOSED."). `ActionRequest`: `description` (up to 500), `owner` (up to 100), `dueOn` (date), `done`.

```json
// 200 Detail
{ "problem": { "id": 4, "title": "Queue worker leaks memory", "description": "Seen on three nights",
               "status": "ROOT_CAUSE_IDENTIFIED", "rootCause": "Log rotation was off",
               "incidentCount": 2, "actionCount": 1, "openActionCount": 1,
               "createdAt": "2027-02-11T09:00:00Z", "resolvedAt": null, "closedAt": null },
  "incidents": [ { "id": 9, "title": "Exports are slow", "productName": "Valam.ai",
                   "phase": "RESOLVED", "severity": "MAJOR", "startedAt": "2027-02-10T08:15:00Z" } ],
  "actions": [ { "id": 7, "description": "Turn log rotation on", "owner": "ops", "dueOn": "2027-02-20",
                 "done": false, "doneAt": null } ] }
```
`Problem` (list item) has the same fields as `problem` above. `Detail` = `problem`, `incidents` (`IncidentRef`: `id, title, productName, phase, severity, startedAt`) and `actions` (`Action`: `id, description, owner, dueOn, done, doneAt`).

Frontend clients: `frontend/src/api/serviceStatusApi.ts` (incidents), `frontend/src/api/problemsApi.ts` (problems). OpenAPI contract: not maintained separately; `AdminServiceStatusController`, `AdminProblemController`, the DTOs and this file are the source of truth ([C14](../../01-business/roadmap/open-decisions.md#c14)).
