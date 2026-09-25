# TC-IAM-002: Organization MFA Policy — AC-2

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-002 |
| Requirement ID (required) | [REQ-IAM-001](../../../docs/02-requirements/FRD/organization-mfa-policy/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/organization-mfa-policy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-001](TESTPLAN-IAM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | No |

## Preconditions
A caller with `MANAGE_ORGANIZATION`.

## Steps
1. Arrange: a caller with `MANAGE_ORGANIZATION`.
2. Act: they turn the MFA requirement off.
3. Observe the response and the UI.

## Expected Result
`GET /organization/me` returns `mfaRequired: false`.

## Automated coverage
- None

**Manual check:** Turn the requirement off and check `GET /organization/me` returns `mfaRequired: false`.

## Actual Result
Not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
