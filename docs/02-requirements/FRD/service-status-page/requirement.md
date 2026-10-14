# REQ-PRT-001 — Interim Service Status Page

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-25
**Changed:** 2026-10-14 ([C101](../../../01-business/roadmap/open-decisions.md#c101)): per-product status is now derived from service instances ([REQ-SRM-001](../service-instances/requirement.md)) and incidents are the support incidents of [REQ-SUP-003](../incident-problem-management/requirement.md). See REQ-PRT-001.5 and .6.

| Field | Value |
|---|---|
| Sprint | [2026.3.3](../../../01-business/roadmap/sprints/SPRINT-2026.3.3.md) |
| Requirement ID | REQ-PRT-001 |
| Application | [01 Enterprise Intelligence Suite](../../../01-business/roadmap/applications/01-enterprise-intelligence-suite.md) |
| Application code | `APP-PRT` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0: the application is MVP scope ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No. This feature makes no use of AI (proposed; confirmed when the FRD is approved, C12) |

## Source functions
Workbook functions from the sprint and application pages that this FRD covers:

| Function ID | Function | Application page |
|---|---|---|
| 01.02.02.01 | View service status (per-product, interim) | [01 Enterprise Intelligence Suite](../../../01-business/roadmap/applications/01-enterprise-intelligence-suite.md#feature-010202-service-health) |
| 01.02.02.03 | View incidents (interim) | [01 Enterprise Intelligence Suite](../../../01-business/roadmap/applications/01-enterprise-intelligence-suite.md#feature-010202-service-health) |

## Summary
A simple interim status page. Platform administrators post per-product status and incidents manually, and customers view them. It is replaced by real sources later: per-product status by Health Monitoring (10.04, sprint 2027.1.1) and incidents by Incident & Problem Management (12.04, sprint 2027.1.3). The page is removed after sprint 2027.1.3 ([C20](../../../01-business/roadmap/open-decisions.md#c20)).

**Update, 2026-10-14 ([C101](../../../01-business/roadmap/open-decisions.md#c101)):** both replacements are built. A product's status is now **derived from its service instances' health reports** (a product is Down only if more than half of its reporting instances are Down); the status a platform administrator posts is kept when no instance reported in the last 5 minutes, and a manual **Maintenance wins** ([REQ-SRM-001](../service-instances/requirement.md)). An incident is now the **support incident** of [REQ-SUP-003](../incident-problem-management/requirement.md): the same record, with internal fields customers never see. The administrator's **Manage tab no longer posts incidents**; they moved to an **Incidents** tab.

## Actors
- Platform administrator (posts status and incidents)
- Customer (views status and incidents)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-PRT-001.1 | A platform administrator can post and update the status of each product. | P0 |
| REQ-PRT-001.2 | A platform administrator can post incidents with a title, message, start time and end time. | P0 |
| REQ-PRT-001.3 | Customers can view per-product status and incidents. | P0 |
| REQ-PRT-001.4 | The interim page is controlled by a single on/off configuration setting. | P0 |
| REQ-PRT-001.5 | **(C101)** A product's status is **derived from its service instances** when at least one reported in the last 5 minutes (more than half Down = Major outage; some Down = Partial outage; some Degraded = Degraded; else Operational); otherwise the status a platform administrator posted is kept (Operational if none); a posted **Maintenance wins**. Each product says whether its status comes from instances or from an administrator, and how many instances it is based on. Rules: [BR-SRM-010 to BR-SRM-012](../service-instances/business-rules.md). | P0 |
| REQ-PRT-001.6 | **(C101)** An incident is the support incident of [REQ-SUP-003](../incident-problem-management/requirement.md) (same record). Customers who purchased the product still see only title, message, product, start and end; severity, phase, internal notes, assignee, resolution summary, linked tickets and problem are never shown to customers. Platform administrators post and manage incidents on the **Incidents** tab; the Manage tab posts product status only. | P0 |

## Out of scope
- ~~Automatic health monitoring (Health Monitoring 10.04, sprint 2027.1.1)~~ — built in sprint 2027.1.2 as [REQ-SRM-001](../service-instances/requirement.md) (C101). Anomaly detection and alerts (10.04.01.02/.03) stay **Not specified**.
- ~~Incident and problem management (12.04, sprint 2027.1.3)~~ — built in sprint 2027.1.2 as [REQ-SUP-003](../incident-problem-management/requirement.md) (C101).

## Dependencies
- Existing product catalog (products the status refers to).
- Service instances ([REQ-SRM-001](../service-instances/requirement.md)) and incidents ([REQ-SUP-003](../incident-problem-management/requirement.md)), 2026-10-14.
- Retirement tasks in sprints [2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md) and [2027.1.3](../../../01-business/roadmap/sprints/SPRINT-2027.1.3.md) ([C20](../../../01-business/roadmap/open-decisions.md#c20)).

## Decisions ([C26](../../../01-business/roadmap/open-decisions.md#c26), product owner, 2026-09-26)
The open questions are resolved:
- Status values: Operational, Degraded, Partial outage, Major outage, Maintenance.
- Visibility: every signed-in customer sees every product's status; incident details only for purchased products.
- Posting permission: new platform permission `MANAGE_SERVICE_STATUS`, granted to `ADMIN`.
- Setting: `app.status-page.enabled`, on by default.
- Placement: `/status` in the signed-in sidebar, linked from the dashboard Service Health card; admin screen `/admin/service-status`.
