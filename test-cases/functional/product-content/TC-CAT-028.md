# TC-CAT-028: Only MANAGE_CATALOG can configure content

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-028 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-out visitor, a customer and an administrator.

## Steps
1. Call every admin endpoint as a visitor and as a customer.
2. Call the public reads signed out.

## Expected Result
401 for the visitor, 403 for the customer on every admin endpoint; the public reads are open (404 for an unknown application).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/ProductContentAuthorizationTest.java` (all)

## Actual Result
The automated tests above passed on 2026-10-07.

## Status
Passed (automated run 2026-10-07)

## Linked Defect (if failed)
