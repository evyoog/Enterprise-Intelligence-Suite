# TC-IAM-030: Privileged Access (User and Organization Administrator) — AC-8

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-030 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An approved grant past its expiry.

## Steps
1. Arrange: an approved grant past its expiry.
2. Observe the response and the UI.

## Expected Result
"My requests" shows it as EXPIRED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/authorization/service/PrivilegedAccessServiceTest.java` — "fullLifecycle_requestApproveGrantThenExpire"
- `frontend/src/components/security/PrivilegedAccessRequestsCard.test.tsx` — "shows an expired grant and lets the requester withdraw a pending one"

## Actual Result
All automated tests below passed on 2026-09-25.

## Status
Passed (automated run 2026-09-25)

## Linked Defect (if failed)
None.
