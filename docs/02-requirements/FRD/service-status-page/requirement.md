# REQ-PRT-001 — Interim Service Status Page

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-25

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

## Out of scope
- Automatic health monitoring (Health Monitoring 10.04, sprint 2027.1.1)
- Incident and problem management (12.04, sprint 2027.1.3)

## Dependencies
- Existing product catalog (products the status refers to).
- Retirement tasks in sprints [2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md) and [2027.1.3](../../../01-business/roadmap/sprints/SPRINT-2027.1.3.md) ([C20](../../../01-business/roadmap/open-decisions.md#c20)).

## Decisions ([C26](../../../01-business/roadmap/open-decisions.md#c26), product owner, 2026-09-26)
The open questions are resolved:
- Status values: Operational, Degraded, Partial outage, Major outage, Maintenance.
- Visibility: every signed-in customer sees every product's status; incident details only for purchased products.
- Posting permission: new platform permission `MANAGE_SERVICE_STATUS`, granted to `ADMIN`.
- Setting: `app.status-page.enabled`, on by default.
- Placement: `/status` in the signed-in sidebar, linked from the dashboard Service Health card; admin screen `/admin/service-status`.
