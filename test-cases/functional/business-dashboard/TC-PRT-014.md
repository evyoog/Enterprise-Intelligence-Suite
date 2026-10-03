# TC-PRT-014: Business dashboard — Audience routing and errors

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PRT-014 |
| Requirement ID (required) | [C69](../../../docs/01-business/roadmap/open-decisions.md#c69) — [business dashboard screen](../../../docs/05-ui/screen-requirements/business-dashboard.md) |
| Test Plan | [TESTPLAN-PRT-003](TESTPLAN-PRT-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An org member without MANAGE_ORGANIZATION; an individual; a server error.

## Steps
1. Open the business dashboard as each.

## Expected Result
403 and 404 redirect to /my/products; a 500 shows the message and does not redirect.

## Automated coverage
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `redirects to /my/products on 403 …`
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `redirects to /my/products on 404 …`
- `frontend/src/pages/BusinessDashboardPage.test.tsx` — `does not redirect on a genuine server error — shows it instead`

## Actual Result
The automated tests above passed on 2026-10-03.

## Status
Passed (automated run 2026-10-03)

## Linked Defect (if failed)
None.
