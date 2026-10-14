# TC-BIL-038: Other countries use the same rule structure with their own components and conditions

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-038 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.16) |
| Acceptance Criterion | [AC-7](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-14](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-16](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Approved rules: a country-wide US rule (one component, 5%); a California rule with State tax 6% and County tax 1.25%; a cross-border zero-rated rule for US business customers; a Germany reverse-charge rule limited to business customers with a tax ID.

## Steps
1. Calculate 1000.00 for a customer in California.
2. Calculate 1000.00 for a customer in New York.
3. Calculate a cross-border sale to a business customer in the US.
4. Calculate for a German business customer with a tax ID, then without a tax ID, then an individual.

## Expected Result
Step 1: two components STATE 60.00 and COUNTY 12.50 (total 72.50), place of supply "California, United States". Step 2: the country-wide component only. Step 3: an explicit zero-tax line with treatment Zero-rated and the taxable amount recorded. Step 4: reverse charge with zero tax only for a business customer with a tax ID; the other two pay the "everyone else" rule. No rate is assumed for a country without a rule (TC-BIL-043).

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InternalTaxEngineTest.java` — `theMostSpecificRuleWins`, `crossBorderSuppliesUseTheirOwnRule`, `customerTypeAndTaxIdConditionsNarrowARule`, `aCountryWithoutRulesBlocksEvenWhenAnotherCountryHasThem`

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
