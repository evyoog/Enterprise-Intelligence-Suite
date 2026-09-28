# TC-PTR-003: Skipping a lifecycle stage is refused

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PTR-003 |
| Requirement ID (required) | [REQ-PTR-001](../../../docs/02-requirements/FRD/provider-onboarding/requirement.md) |
| Acceptance Criterion | [AC-3](../../../docs/02-requirements/FRD/provider-onboarding/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PTR-001](TESTPLAN-PTR-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A REGISTERED provider exists (not yet verified).

## Steps
1. Act: an admin tries to approve the provider directly.
2. Act: an admin tries to activate the provider directly.

## Expected Result
Both steps are refused (400) — the provider remains REGISTERED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/partner/service/PartnerServiceTest.java` — `aStageCannotBeSkipped`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
