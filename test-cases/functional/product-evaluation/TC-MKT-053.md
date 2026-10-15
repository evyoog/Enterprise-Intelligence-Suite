# TC-MKT-053: A trial provisions like any subscription: service instance and event with trial true

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-053 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-008](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A product with trial days; a customer; access to the `outbox_event` table (or the platform events screen).

## Steps
1. Start a trial.
2. Read the subscription's service instance.
3. Read the newest `outbox_event` rows with `event_type` = `SubscriptionCreated` for the subscription.
4. For a normal subscription, read its `SubscriptionCreated` payload.

## Expected Result
Step 2: an instance in status PROVISIONING. Step 3: a payload with `subscriptionId`, `productId`, `planId`, `ownerType`, `ownerCustomerId`, `ownerOrganizationId`, `status` ACTIVE, `expiresAt` and `"trial": true`. Step 4: `"trial": false`. No database is created by EIS.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `aTrialIsAnActiveSubscriptionOnTheHighestPlanThatEndsAfterTheTrialDaysWithoutRenewing`

Only the service instance is asserted (the same test as [TC-MKT-050](TC-MKT-050.md)). **Not covered by an automated test:** the `trial` field of the event payload (`SubscriptionService#publish`). **Not Run** by hand.

## Actual Result
The instance part passed in the automated run of 2026-10-15. The event payload was not checked.

## Status
Partly passed: instance automated run 2026-10-15; event payload Not Run

## Linked Defect (if failed)
None.
