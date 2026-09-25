# TC-IAM-012: Member Role Assignment — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-012 |
| Requirement ID (required) | [REQ-IAM-002](../../../docs/02-requirements/FRD/member-role-assignment/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/member-role-assignment/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-002](TESTPLAN-IAM-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The member list is shown.

## Steps
1. Arrange: the member list is shown.
2. Observe the response and the UI.

## Expected Result
Its text exists in `en.json` and `es.json`, the role selector is keyboard operable, and a jest-axe check reports no violations.

## Automated coverage
- `frontend/src/components/organization/OrganizationMembersCard.test.tsx` — "has no detectable a11y violations"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
