# TC-PTR-005: Saving a contract again edits the same one

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PTR-005 |
| Requirement ID (required) | [REQ-PTR-001](../../../docs/02-requirements/FRD/provider-onboarding/requirement.md) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/provider-onboarding/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PTR-001](TESTPLAN-PTR-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A provider exists with no contract yet.

## Steps
1. Act: an admin creates a contract (terms, start date, end date).
2. Observe the contract's status.
3. Act: an admin saves the contract again with different terms.
4. Observe the returned contract's id and terms.

## Expected Result
Step 2: ACTIVE. Step 4: same id as step 1, terms updated to the new value.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/partner/service/PartnerServiceTest.java` — `savingAContractAgainEditsTheSameOneAndReactivatesIt`, `aContractsEndDateMustBeAfterItsStartDate`, `creatingAContractRequiresAnExistingProvider`
- `frontend/src/pages/admin/AdminPartnerDetailPage.test.tsx` — `saves a contract for the provider`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
