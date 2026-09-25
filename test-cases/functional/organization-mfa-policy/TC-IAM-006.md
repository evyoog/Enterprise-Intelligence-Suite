# TC-IAM-006: Organization MFA Policy — AC-6

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-006 |
| Requirement ID (required) | [REQ-IAM-001](../../../docs/02-requirements/FRD/organization-mfa-policy/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/organization-mfa-policy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-001](TESTPLAN-IAM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The MFA setting is shown.

## Steps
1. Arrange: the MFA setting is shown.
2. Observe the response and the UI.

## Expected Result
Its text exists in `en.json` and `es.json`, it is keyboard operable, and a jest-axe check reports no violations.

## Automated coverage
- `frontend/src/components/organization/OrganizationMfaPolicyCard.test.tsx` — "has no detectable a11y violations"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
