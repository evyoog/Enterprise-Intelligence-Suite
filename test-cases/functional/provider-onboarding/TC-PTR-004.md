# TC-PTR-004: An active or already-rejected provider cannot be rejected

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PTR-004 |
| Requirement ID (required) | [REQ-PTR-001](../../../docs/02-requirements/FRD/provider-onboarding/requirement.md) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/provider-onboarding/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PTR-001](TESTPLAN-PTR-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
One provider is ACTIVE; another is REJECTED.

## Steps
1. Act: an admin tries to reject the ACTIVE provider.
2. Act: an admin tries to reject the already-REJECTED provider again.

## Expected Result
Both steps are refused (400).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/partner/service/PartnerServiceTest.java` — `anActiveProviderCanNoLongerBeRejected`, `rejectionIsAllowedUntilActiveThenTerminal`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
