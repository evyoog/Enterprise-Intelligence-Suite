# TC-SUB-010: Subscription Lifecycle — AC-10

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-010 |
| Requirement ID (required) | [REQ-SUB-001](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md) |
| Acceptance Criterion | [AC-10](../../../docs/02-requirements/FRD/subscription-lifecycle/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-SUB-001](TESTPLAN-SUB-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A subscription owned by customer A; customer B (a stranger) and an organization-owned subscription.

## Steps
1. Arrange: subscribe customer A to a product.
2. Act: customer B calls suspendSubscription on customer A's subscription id.
3. Repeat against an organization-owned subscription.
4. Observe each response.

## Expected Result
Every attempt is refused with a generic 404 (`ResourceNotFoundException`), never confirming the subscription exists.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `anotherCustomersSubscriptionIsInvisible`
- `backend/src/test/java/com/vyoog/eisplatform/modules/registration/service/SubscriptionServiceTest.java` — `organizationOwnedSubscriptionCannotBeActedOnViaMeEndpoints`

## Actual Result
The automated tests below passed on 2026-09-27.

## Status
Passed (automated run 2026-09-27)

## Linked Defect (if failed)
None.
