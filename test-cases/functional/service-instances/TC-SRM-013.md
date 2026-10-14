# TC-SRM-013: Hand changes and reporter keys are audited without the secret

| Field | Value |
|---|---|
| Test Case ID (required) | TC-SRM-013 |
| Requirement ID (required) | [REQ-SRM-001](../../../docs/02-requirements/FRD/service-instances/requirement.md) (C101) |
| Acceptance Criterion | [AC-13](../../../docs/02-requirements/FRD/service-instances/acceptance-criteria.md) |
| Business rule(s) | [BR-SRM-004, BR-SRM-009](../../../docs/02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../../docs/01-business/roadmap/open-decisions.md#c101) |
| Test Plan | [TESTPLAN-SRM-001](TESTPLAN-SRM-001.md) |
| Priority | P1 |
| Type | Functional (manual) |
| Automated | No |

## Preconditions
A platform administrator; the audit log screen or table `audit_log`.

## Steps
1. Change an instance by hand.
2. Create a reporter key, then revoke it.
3. Read the three audit entries.

## Expected Result
`SERVICE_INSTANCE_CHANGED` (before and after, with the note), `SERVICE_REPORTER_KEY_CREATED` and `SERVICE_REPORTER_KEY_REVOKED` exist, naming the 12-character prefix; none contains the secret.

## Automated coverage
None. This case is manual only.

Manual only: no automated test asserts these audit entries. **Not Run**.

## Actual Result
Not Run. The case has not been run by hand yet.

## Status
Not Run

## Linked Defect (if failed)
None.
