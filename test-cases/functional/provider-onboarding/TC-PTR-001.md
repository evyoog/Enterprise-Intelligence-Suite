# TC-PTR-001: Applying to become a provider creates a new registered provider

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PTR-001 |
| Requirement ID (required) | [REQ-PTR-001](../../../docs/02-requirements/FRD/provider-onboarding/requirement.md) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/provider-onboarding/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PTR-001](TESTPLAN-PTR-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
None — no Vyoog account required.

## Steps
1. Act: submit a provider application (company name, contact name, contact email).
2. Observe the returned provider's status.

## Expected Result
Step 2: status is REGISTERED.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/partner/service/PartnerServiceTest.java` — `aRegisteredProviderMovesThroughEveryStageInOrder`
- `frontend/src/pages/ProviderApplicationPage.test.tsx` — `submits an application and shows a confirmation`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
