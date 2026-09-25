# TC-IAM-029: Privileged Access (User and Organization Administrator) — AC-7

| Field | Value |
|---|---|
| Test Case ID (required) | TC-IAM-029 |
| Requirement ID (required) | [REQ-IAM-004](../../../docs/02-requirements/FRD/privileged-access/requirement.md) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/privileged-access/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-IAM-004](TESTPLAN-IAM-004.md) |
| Priority | P0 |
| Type | Functional |
| Automated | No |

## Preconditions
A request already approved or rejected.

## Steps
1. Arrange: a request already approved or rejected.
2. Act: approve or reject is attempted again.
3. Observe the response and the UI.

## Expected Result
The response is 400 "This request has already been decided.".

## Automated coverage
- None

**Manual check:** Approve or reject a request that is already decided and check the response is 400 "This request has already been decided."

## Actual Result
Not yet run.

## Status
Not Run

## Linked Defect (if failed)
None.
