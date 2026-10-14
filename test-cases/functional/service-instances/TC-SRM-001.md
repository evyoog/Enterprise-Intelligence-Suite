# TC-SRM-001: An active subscription gets one service instance; a pending one does not

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-001 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-001, BR-SRM-002, BR-SRM-015](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A product, an organization, and a subscription that is waiting for payment (`PENDING_SUBSCRIPTION`).

## Steps
1. Sync the pending subscription to its instance (the subscription flow does this on every change).
2. Make the subscription ACTIVE and sync again.
3. Create another ACTIVE subscription without syncing, then run the reconcile job.

## Expected Result
Step 1: no instance exists. Step 2: one instance exists with status PROVISIONING and health UNKNOWN. Step 3: the reconcile job creates the missing instance.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/serviceinstance/service/ServiceInstanceServiceTest.java` — `anActiveSubscriptionGetsAnInstanceAndAPendingOneDoesNot`, `reconcileCreatesMissingInstancesForActiveSubscriptions`

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
