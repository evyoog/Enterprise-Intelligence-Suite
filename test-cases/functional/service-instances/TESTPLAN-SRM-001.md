# TESTPLAN-SRM-001: Service instances

| Field | Value |
|---|---|
| Feature ID (required) | FTR-SRM-001 |
| Requirement(s) covered | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Author | Not specified |
| Status | Draft (the FRD is Approved, 2026-10-14; built 2026-10-14; engineering defaults to confirm) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md): TC-SRM-001 to TC-SRM-014. Out of scope (Not specified): starting, stopping and scaling services, resource and configuration management, anomaly detection and alerts, usage monitoring.

## Test Strategy
| Level | Approach |
|---|---|
| Unit / integration | Backend: `ServiceInstanceServiceTest` on the H2-backed Spring context (instances follow subscriptions, reports and keys, the derived status, hand changes, the organization list), `ServiceStatusServiceTest` and `ServiceStatusAuthorizationTest` (regression, permission rule). Frontend: `AdminServiceInstancesPage.test.tsx`, `OrganizationServiceInstancesPage.test.tsx` and `ServiceStatusPage.test.tsx` with jest-axe. Manual: the HTTP behaviour of the open `POST /product-reports/health` path and of the new endpoints' permissions, and the audit entries. |
| Security (SAST/DAST) | Security cases: TC-SRM-004 (key shown once, stored hashed, revocable), TC-SRM-005 (the open report path: missing, revoked and other product's key), TC-SRM-012 (permission gating). |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment with a product posting reports |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database); migration V041 applied by hand.

## Test Cases in This Plan
| Test Case ID | Title | Acceptance criteria | Priority | Automated? | Status |
|---|---|---|---|---|---|
| [TC-SRM-001](TC-SRM-001.md) | An active subscription gets one service instance; a pending one does not | AC-1 | P0 | Yes | Passed |
| [TC-SRM-002](TC-SRM-002.md) | The instance follows the subscription: suspend, resume, cancel | AC-2 | P0 | Yes | Passed |
| [TC-SRM-003](TC-SRM-003.md) | A product reports health with its key; the first report activates the instance | AC-3 | P0 | Yes | Passed |
| [TC-SRM-004](TC-SRM-004.md) | A reporter key is shown once, stored hashed, and can be revoked | AC-4 | P0 | Yes | Passed |
| [TC-SRM-005](TC-SRM-005.md) | Security: the open report path rejects a missing, revoked or other product's key | AC-5 | P0 | Yes | Passed |
| [TC-SRM-006](TC-SRM-006.md) | Status page derivation: more than half Down is a major outage | AC-6 | P0 | Yes | Passed |
| [TC-SRM-007](TC-SRM-007.md) | A stale report is left out and the manual status is kept | AC-7 | P0 | Yes | Passed |
| [TC-SRM-008](TC-SRM-008.md) | The status page uses the instances, says how many, and Maintenance wins | AC-8 | P0 | Yes | Passed |
| [TC-SRM-009](TC-SRM-009.md) | A platform administrator changes an instance by hand, with a note | AC-9 | P0 | Yes | Passed |
| [TC-SRM-010](TC-SRM-010.md) | An organization administrator sees only their own organization's instances, read-only | AC-10 | P0 | Yes | Passed |
| [TC-SRM-011](TC-SRM-011.md) | The Instances tab shows the summary, the table and the filters | AC-11 | P1 | Partly | Partly passed |
| [TC-SRM-012](TC-SRM-012.md) | Security: permission gating of instances, reporter keys and the organization list | AC-12 | P0 | Yes | Passed |
| [TC-SRM-013](TC-SRM-013.md) | Hand changes and reporter keys are audited without the secret | AC-13 | P1 | No | Not Run |
| [TC-SRM-014](TC-SRM-014.md) | Regression: the status page behaves as before when no instance reports | AC-14 | P0 | Yes | Passed |

## Entry Criteria
FRD Approved; migration V041 applied.

## Exit Criteria
- All automated cases passing; the manual cases run and passed before UAT sign-off.

## Blocked or partial
- **Not covered by an automated test:** the HTTP mapping of `POST /product-reports/health` (open `permitAll` path, 401 for a bad key; TC-SRM-005 step 4), requests to the new instance and key endpoints and to `/organization/me/service-instances` (TC-SRM-012), the backend listing, filters and summary (TC-SRM-011), the audit entries (TC-SRM-013), that a deprovisioned instance is left out of the organization list, and the back-to-ACTIVE branch of TC-SRM-002.
- **Regression note on the existing status-page tests:** `ServiceStatusServiceTest`, `ServiceStatusAuthorizationTest` and `ServiceStatusPage.test.tsx` ([TESTPLAN-PRT-001](../service-status-page/TESTPLAN-PRT-001.md)) must keep passing (TC-SRM-014). The frontend cases "posts an incident" and "resolves an open incident and shows backend refusals" were removed from `ServiceStatusPage.test.tsx` because the Manage tab no longer posts incidents; the Incidents tab tests ([TC-SUP-032](../incident-problem-management/TC-SUP-032.md)) replace them.
- The engineering defaults of C101 are to be confirmed by the product owner; if the 5-minute window or the combination rule changes, TC-SRM-006 and TC-SRM-007 change with it.

## Risks
The engineering defaults of C101 are to be confirmed by the product owner.
