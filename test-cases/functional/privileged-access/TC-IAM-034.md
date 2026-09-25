# TC-IAM-034: Privileged Access (User and Organization Administrator) — AC-12

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-034 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-12](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The request and approval views are shown.

## Steps
1. Arrange: the request and approval views are shown.
2. Observe the response and the UI.

## Expected Result
Their text exists in `en.json` and `es.json`, they are keyboard operable, and a jest-axe check reports no violations.

## Automated coverage
- `frontend/src/components/security/PrivilegedAccessRequestsCard.test.tsx` — "has no detectable a11y violations"
- `frontend/src/components/organization/OrganizationPrivilegedAccessCard.test.tsx` — "has no detectable a11y violations"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
