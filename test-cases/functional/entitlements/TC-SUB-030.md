# TC-SUB-030: The customer endpoint answers only for the signed-in caller

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-030 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (BR-4) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | No |

## Preconditions
Two customers, A and B, each with an active subscription.

## Steps
1. As A, call `GET /me/entitlements`.
2. Look for any way to ask for B (a customer id parameter).

## Expected Result
A receives only A's entitlements; the endpoint takes no customer id, so B's cannot be requested.

## Automated coverage
None. This is true by construction: `EntitlementController#mine` resolves the customer from the token only. Add a controller test that two callers get different lists.

## Actual Result
Not run. No automated test exists for this case yet.

## Status
Not Run

## Linked Defect (if failed)
None.
