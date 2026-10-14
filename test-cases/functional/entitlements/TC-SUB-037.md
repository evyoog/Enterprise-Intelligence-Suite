# TC-SUB-037: The admin Entitlements lookup page

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUB-037 |
| Requirement ID (required) | [REQ-SUB-002](../../../docs/02-requirements/FRD/entitlements/requirement.md) (.7); [screen](../../../docs/05-ui/screen-requirements/entitlements.md#admin-entitlements-lookup) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/entitlements/acceptance-criteria.md) |
| Decision | [C97](../../../docs/01-business/roadmap/open-decisions.md#c97) |
| Test Plan | [TESTPLAN-SUB-002](TESTPLAN-SUB-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An administrator with `VIEW_ENTITLEMENTS` on `/admin/entitlements` (frontend test: the API is mocked).

## Steps
1. Choose Customer, enter a numeric ID, press Look up.
2. Choose Organization and look up an ID with nothing active.
3. Look up an unknown ID (404).
4. Type "abc" in the ID field.
5. Run axe.

## Expected Result
Step 1: read-only cards. Step 2: "Nothing is active for this customer or organization." Step 3: "No customer or organization has that ID." Step 4: "Enter a number" and Look up disabled. Step 5: no violations.

## Automated coverage
- `frontend/src/pages/admin/AdminEntitlementsPage.test.tsx` — "looks up a customer read-only and has no axe violations"
- `frontend/src/pages/admin/AdminEntitlementsPage.test.tsx` — "looks up an organization, says when nothing is active, and says when the ID is unknown"
- `frontend/src/pages/admin/AdminEntitlementsPage.test.tsx` — "refuses a non-numeric ID"

## Actual Result
The automated tests below passed on 2026-10-13 (`vitest run`).

## Status
Passed (automated run 2026-10-13)

## Linked Defect (if failed)
None.
