# TC-SUP-034: The Manage tab no longer posts incidents

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SUP-034 |
| Requirement ID (required) | [REQ-SUP-003](../../../docs/02-requirements/FRD/incident-problem-management/requirement.md) (C101) |
| Acceptance Criterion | [AC-14](../../../docs/02-requirements/FRD/incident-problem-management/acceptance-criteria.md) |
| Business rule(s) | [BR-INC-001](../../../docs/02-requirements/FRD/incident-problem-management/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SUP-003](TESTPLAN-SUP-003.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform administrator on Platform → Service status → Manage.

## Steps
1. Post a status with a note for one product.
2. Look for an incident form.

## Expected Result
The status is saved with its note; the tab has no incident form (incidents are posted on the Incidents tab); no axe violations.

## Automated coverage
- `frontend/src/pages/ServiceStatusPage.test.tsx` — "posts a new status with a note for one product", "no longer has the incident form (incidents moved to the Incidents tab)", "has no detectable a11y violations"

## Actual Result
The automated tests above passed on 2026-10-14.

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
