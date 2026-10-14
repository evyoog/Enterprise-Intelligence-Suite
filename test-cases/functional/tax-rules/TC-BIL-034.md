# TC-BIL-034: A new version of an approved rule closes the previous version the day before

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-034 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.13) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-21](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An approved, active rule (version 1) that started on 1 Jan 2026 and has no end date.

## Steps
1. Press New version on the rule (only the latest version shows this action).
2. Check the copy: a Draft, version 2, same rule family.
3. Set its effective-from to 1 Nov 2026, submit it and approve it as another administrator.
4. Read the rule list.
5. Try to create a new version that starts on or before the date version 1 started.

## Expected Result
Step 2: version 2 is a Draft. Step 4: version 1 now ends on 31 Oct 2026 and version 2 starts 1 Nov 2026; version 1 stays readable in the list. Step 5 is refused. An approved rule is never edited.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxRuleServiceTest.java` — `aNewVersionIsADraftAndApprovingItClosesThePreviousVersionTheDayBefore`, `aNewVersionMustStartAfterThePreviousVersionStarted`
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "lists the rules with their components, dates, status and life cycle actions"

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
