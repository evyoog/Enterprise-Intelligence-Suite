# TC-IAM-001: Organization MFA Policy — AC-1

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-001 |
| Requirement ID (required) | [REQ-IAM-001](../../../docs/02-requirements/FRD/organization-mfa-policy/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/organization-mfa-policy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-001](TESTPLAN-IAM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A caller with `MANAGE_ORGANIZATION`.

## Steps
1. Arrange: a caller with `MANAGE_ORGANIZATION`.
2. Act: they turn the MFA requirement on.
3. Observe the response and the UI.

## Expected Result
`GET /organization/me` returns `mfaRequired: true` and an `MFA_POLICY_CHANGED` audit record exists.

## Automated coverage
- `frontend/src/components/organization/OrganizationMfaPolicyCard.test.tsx` — "shows the current policy and turns it on"

**Manual check:** Check that an `MFA_POLICY_CHANGED` audit record exists.

## Actual Result
The automated tests below passed on 2026-09-25; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
