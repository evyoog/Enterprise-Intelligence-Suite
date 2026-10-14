# TC-SRM-002: The instance follows the subscription: suspend, resume, cancel

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-002 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-002](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An ACTIVE organization subscription with its instance (never reported).

## Steps
1. Suspend the subscription.
2. Make it ACTIVE again.
3. Cancel it.

## Expected Result
The instance is SUSPENDED, then PROVISIONING (it has never reported; an instance that has reported goes back to ACTIVE), then DEPROVISIONED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/serviceinstance/service/ServiceInstanceServiceTest.java` — `theInstanceFollowsTheSubscriptionSuspendResumeAndCancel`

Not covered by an automated test: the "ACTIVE again after a report" branch (back to ACTIVE, not PROVISIONING).

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
