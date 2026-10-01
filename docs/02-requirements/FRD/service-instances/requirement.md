# REQ-SRM-001 — Service instances

**Status:** Draft (stub — documents only; built in its own sprint)
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved.
**Decision:** [C57](../../../01-business/roadmap/open-decisions.md#c57) (answer to D7, option A). Related: [C56](../../../01-business/roadmap/open-decisions.md#c56) (provisioning contract), [C20](../../../01-business/roadmap/open-decisions.md#c20) (interim status page).

| Field | Value |
|---|---|
| Sprint | [2027.1.2](../../../01-business/roadmap/sprints/SPRINT-2027.1.2.md) |
| Requirement ID | REQ-SRM-001 |
| Application | [10 Service & Resource Management](../../../01-business/roadmap/applications/10-service-resource-management.md) |
| Application code | `APP-SRM` |
| Priority | Not specified for this FRD (application 10 is P1/stretch under [C4](../../../01-business/roadmap/open-decisions.md#c4)/[C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 10.01.01.01 | Create service instance | Yes — created when provisioning is requested (REQ-ORD-002) |
| 10.01.01.02–.07 | Configure, start, stop, restart, scale, delete service | No — EIS does not run the products' infrastructure; Not specified |
| 10.04.01.01 | Collect health status | Yes — health reported by the product |
| 10.04.01.04 | View health | Yes — and feeds the status page (C20) |
| 10.04.01.02/.03 | Detect anomaly; Create alert | No — Not specified |

## Summary
A **service instance** is one subscription's provisioned tenant in a hosted product. EIS keeps one service instance per subscription. The status and health come from the product, through the provisioning contract ([REQ-ORD-002](../provisioning-contract/requirement.md)).

## Proposed record
| Field | Description |
|---|---|
| subscription | The subscription this instance serves (one instance per subscription) |
| product tenant reference, access URL | From the product's "provisioned" reply |
| status | `PROVISIONING`, `ACTIVE`, `SUSPENDED`, `FAILED`, `DEPROVISIONED` |
| health | As reported by the product; the value set is Not specified |
| last reported at | Time of the product's latest report |

## Status page (C20)
When this FRD is built, the interim status page ([REQ-PRT-001](../service-status-page/requirement.md)) switches its **per-product status** to this source, as [C20](../../../01-business/roadmap/open-decisions.md#c20)'s retirement task says. How many instance health values combine into one per-product status is Not specified.

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | Health value set (for example healthy / degraded / down) and reporting interval. | Yes |
| 2 | How instance health combines into one per-product status for the status page. | Yes |
| 3 | Who sees service instances: the customer, organization admins, platform admins? | Yes |
| 4 | Status transitions and who causes each (all from REQ-ORD-002 replies?). | Yes — depends on REQ-ORD-002 |

## Not yet written
`business-rules.md`, `workflow.md`, `acceptance-criteria.md`, `ui-requirements.md` and `api-requirements.md` are written when this FRD is scoped for its sprint.
