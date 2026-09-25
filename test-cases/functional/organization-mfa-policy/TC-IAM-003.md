# TC-IAM-003: Organization MFA Policy — AC-3

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-003 |
| Requirement ID (required) | [REQ-IAM-001](../../../docs/02-requirements/FRD/organization-mfa-policy/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/organization-mfa-policy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-001](TESTPLAN-IAM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An organization member without `MANAGE_ORGANIZATION`.

## Steps
1. Arrange: an organization member without `MANAGE_ORGANIZATION`.
2. Act: they call `PATCH /organization/me/mfa-policy`.
3. Observe the response and the UI.

## Expected Result
The response is 403 "You do not have permission to do this" and the UI shows that message.

## Automated coverage
- `frontend/src/components/organization/OrganizationMfaPolicyCard.test.tsx` — "shows the backend message when the change is refused"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
