# TC-IAM-049: Organization Mfa Policy — AC-11

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-049 |
| Requirement ID (required) | [REQ-IAM-001](../../../docs/02-requirements/FRD/organization-mfa-policy/requirement.md) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/organization-mfa-policy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-001](TESTPLAN-IAM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Partly |

## Preconditions
The set-up step in the sign-in dialog.

## Steps
1. Arrange: the set-up step in the sign-in dialog.
2. Act: it is shown.
3. Observe the response and the UI.

## Expected Result
Its text exists in `en.json` and `es.json`, it is keyboard operable, and a jest-axe check reports no violations.

## Automated coverage
- `frontend/src/components/home/MfaSignInEnrollment.test.tsx` — "has no detectable a11y violations on the set-up step"

**Manual check:** Switch to Spanish and sign in to an MFA-required organization with a real authenticator app.

## Actual Result
The automated tests below passed on 2026-09-26; the manual check is not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
