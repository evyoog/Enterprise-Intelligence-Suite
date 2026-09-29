# TC-PTR-002: A provider moves through every stage in order

| Field | Value |
|---|---|
| Test Case ID (required) | TC-PTR-002 |
| Requirement ID (required) | [REQ-PTR-001](../../../docs/02-requirements/FRD/provider-onboarding/requirement.md) |
| Acceptance Criterion | [AC-2](../../../docs/02-requirements/FRD/provider-onboarding/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-PTR-001](TESTPLAN-PTR-001.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A REGISTERED provider exists.

## Steps
1. Act: an admin verifies the provider.
2. Act: an admin approves the provider.
3. Act: an admin activates the provider.

## Expected Result
Step 1: status VERIFIED. Step 2: status APPROVED. Step 3: status ACTIVE.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/partner/service/PartnerServiceTest.java` — `aRegisteredProviderMovesThroughEveryStageInOrder`
- `frontend/src/pages/admin/AdminPartnerDetailPage.test.tsx` — `verifies a registered provider`

## Actual Result
The automated tests below passed on 2026-09-28.

## Status
Passed (automated run 2026-09-28)

## Linked Defect (if failed)
None.
