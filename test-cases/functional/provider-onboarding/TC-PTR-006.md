# TC-PTR-006: The scheduled job expires only overdue contracts

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PTR-006 |
| Requirement ID (required) | [REQ-PTR-001](../../../docs/02-requirements/FRD/provider-onboarding/requirement.md) |
| Acceptance Criterion | [AC-6](../../../docs/02-requirements/FRD/provider-onboarding/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PTR-001](TESTPLAN-PTR-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two providers each have a contract: one with an end date in the past, one with an end date in the future.

## Steps
1. Act: run the contract-expiry check.
2. Observe both contracts' status.

## Expected Result
Step 2: the overdue contract is EXPIRED; the still-current one remains ACTIVE.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/partner/service/PartnerServiceTest.java` — `expireOverdueContractsFlipsOnlyPastEndDates`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
