# TC-IAM-047: Organization Mfa Policy — AC-9

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-047 |
| Requirement ID (required) | [REQ-IAM-001](../../../docs/02-requirements/FRD/organization-mfa-policy/requirement.md) |
| Acceptance Criterion | [AC-9](../../../docs/02-requirements/FRD/organization-mfa-policy/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-001](TESTPLAN-IAM-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A held sign-in.

## Steps
1. Arrange: a held sign-in.
2. Act: a wrong code is entered, or an ENROLL challenge is used as a VERIFY challenge.
3. Observe the response and the UI.

## Expected Result
It is refused, the attempt is counted, and no session is issued.

## Automated coverage
- `backend/…/federation/service/SamlSignInMfaPolicyTest.java` — "anEnrollChallengeCannotBeUsedAsAVerifyChallengeOrTheOtherWayRound"
- `frontend/src/components/home/MfaSignInEnrollment.test.tsx` — "shows the backend message for a wrong code and stays on set-up"

## Actual Result
The automated tests below passed on 2026-09-26.

## Status
Passed (automated run 2026-09-26)

## Linked Defect (if failed)
None.
