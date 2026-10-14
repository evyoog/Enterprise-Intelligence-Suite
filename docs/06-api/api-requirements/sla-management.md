# API requirements — SLA management

Requirement: [REQ-SUP-002](../../02-requirements/FRD/sla-management/requirement.md). Business rules: [business-rules.md](../../02-requirements/FRD/sla-management/business-rules.md). Decision: [C101](../../01-business/roadmap/open-decisions.md#c101) (SL-1 B, SL-2 B). Paths are relative to `/api`. Error bodies are `{"message": "…"}` from `GlobalExceptionHandler`. Data model: [service-instances-sla-incidents.md](../../07-database/data-model/service-instances-sla-incidents.md#sla-management-req-sup-002).

**Permission.** `/admin/support/**` is gated by `SecurityConfig` with the platform permission `MANAGE_SUPPORT_TICKETS` (the same rule as the ticket administration): 401 without a token, 403 without the permission.

## SLA settings (`MANAGE_SUPPORT_TICKETS`)
| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| GET | `/admin/support/sla` | The targets (one per priority, in the order LOW, MEDIUM, HIGH, URGENT) and the settings | 200 `Config` | 401, 403 |
| PUT | `/admin/support/sla/policies` | Save the targets of the priorities sent (others unchanged); audited `SLA_POLICY_UPDATED` | 200 `Config` | 400, 401, 403 |
| PUT | `/admin/support/sla/settings` | Save business hours, time zone, warning point and alert list; audited `SLA_SETTINGS_UPDATED` | 200 `Config` | 400, 401, 403 |

New targets apply to tickets created or re-prioritised afterwards; deadlines already set are not moved ([BR-SLA-016](../../02-requirements/FRD/sla-management/business-rules.md)). A request with any invalid value is refused as a whole.

```json
// GET /admin/support/sla
{ "policies": [
    { "priority": "LOW",    "firstResponseMinutes": 480, "resolutionMinutes": 2400, "aroundTheClock": false },
    { "priority": "MEDIUM", "firstResponseMinutes": 480, "resolutionMinutes": 1440, "aroundTheClock": false },
    { "priority": "HIGH",   "firstResponseMinutes": 240, "resolutionMinutes": 480,  "aroundTheClock": false },
    { "priority": "URGENT", "firstResponseMinutes": 60,  "resolutionMinutes": 480,  "aroundTheClock": true } ],
  "settings": { "businessDays": ["MON","TUE","WED","THU","FRI"], "businessStart": "09:00", "businessEnd": "18:00",
                "timeZone": "Asia/Kolkata", "warnAtPercent": 80, "alertEmails": ["ops@example.com"] } }

// PUT /admin/support/sla/policies
{ "policies": [ { "priority": "HIGH", "firstResponseMinutes": 120, "resolutionMinutes": 480, "aroundTheClock": false } ] }

// PUT /admin/support/sla/settings
{ "businessDays": ["MON","TUE","WED","THU","FRI"], "businessStart": "09:00", "businessEnd": "18:00",
  "timeZone": "Asia/Kolkata", "warnAtPercent": 80, "alertEmails": ["Ops@Example.com", "oncall@example.com"] }
```

| Field | Rule |
|---|---|
| `priority` | `LOW`, `MEDIUM`, `HIGH` or `URGENT` (else 400 "Unknown priority …") |
| `firstResponseMinutes`, `resolutionMinutes` | 1 to 525 600 (400 "…the targets must be between 1 minute and one year."); first response not longer than resolution (400 "…the first response target cannot be longer than the resolution target.") |
| `businessDays` | At least one of `MON`…`SUN` (three-letter or full name, any case); else 400 "Choose at least one business day." / "Unknown day …" |
| `businessStart`, `businessEnd` | `HH:mm`; start before end (400 "Business hours must start before they end.") |
| `timeZone` | A Java time-zone id (400 "Unknown time zone.") |
| `warnAtPercent` | 1 to 99 (400 "The warning point must be between 1% and 99%.") |
| `alertEmails` | At most 20, each a valid address (400 "… is not an email address."); returned in lowercase, without duplicates |

## SLA fields on the ticket
`SupportTicketDto` (every ticket endpoint of [ticket-management](../../02-requirements/FRD/ticket-management/api-requirements.md)) now also carries:

| Field | Meaning |
|---|---|
| `firstRespondedAt` | When a person first took the ticket out of Open; null until then |
| `firstResponseDueAt`, `resolutionDueAt` | The deadlines; **null for tickets created before SLA tracking** |
| `firstResponseState`, `resolutionState` | `ON_TRACK`, `WARNING`, `BREACHED`, `MET` or `MET_LATE`. **Only in the administrator endpoints** (`/admin/support/tickets…`); **null in the requester's own endpoints** (`/me/tickets…`) and for tickets without deadlines |

A breach flips the ticket's `status` to `ESCALATED` and leaves `priority` unchanged. No endpoint exposes the warning or breach timestamps; the states carry them.

```json
// GET /admin/support/tickets/12  (additional fields)
{ "firstRespondedAt": null, "firstResponseDueAt": "2027-02-05T13:00:00Z", "resolutionDueAt": "2027-02-08T09:30:00Z",
  "firstResponseState": "WARNING", "resolutionState": "ON_TRACK" }
// GET /me/tickets/12
{ "firstRespondedAt": null, "firstResponseDueAt": "2027-02-05T13:00:00Z", "resolutionDueAt": "2027-02-08T09:30:00Z",
  "firstResponseState": null, "resolutionState": null }
```

## Jobs and notifications
| Item | Detail |
|---|---|
| `SlaCheckJob` | Every minute (`app.sla.interval-ms`, default 60 000; first run after `app.sla.initial-delay-ms`, default 30 000); off with `app.sla.job-enabled=false` |
| Warning | In-app notice (category SYSTEM, severity WARNING) "Support ticket nearing its first response target" / "…resolution target" and the notification email, to the assignee and the alert list |
| Breach | Same recipients, severity CRITICAL, "Support ticket breached its … target", and the ticket is escalated |
| Audit | `SLA_POLICY_UPDATED`, `SLA_SETTINGS_UPDATED`, `TICKET_SLA_WARNING`, `TICKET_SLA_BREACHED` |

Frontend client: `frontend/src/api/slaApi.ts`, `supportApi.ts`. OpenAPI contract: not maintained separately; `AdminSlaController`, `SlaDtos`, `SupportTicketDto` and this file are the source of truth ([C14](../../01-business/roadmap/open-decisions.md#c14)).
