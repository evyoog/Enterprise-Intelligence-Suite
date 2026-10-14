# TC-BIL-036: Only Approved, active rules that are in force on the invoice date are used

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-036 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.12) |
| Acceptance Criterion | [AC-5](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-11](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
One rule for India with a taxable component, in each of these states in turn: Draft, Pending approval, Rejected, Approved but deactivated, Approved starting tomorrow, Approved and ended yesterday, Approved and active.

## Steps
1. For each state calculate tax for an Indian customer (for example with the preview calculator or by generating an invoice).
2. Check the day boundaries: a rule that starts today and a rule that ends today.

## Expected Result
For the first six states no rule is used and the calculation is blocked with `NO_MATCHING_RULE`. The Approved, active rule is used. A rule starting today and a rule ending today are both in force. Deactivating and activating a rule controls whether it is used.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InternalTaxEngineTest.java` — `onlyApprovedActiveRulesInForceAreUsed`, `effectiveDatesAreRespected`
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxRuleServiceTest.java` — `deactivatingAndActivatingARuleControlsWhetherItIsUsed`

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
