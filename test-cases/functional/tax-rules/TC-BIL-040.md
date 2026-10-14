# TC-BIL-040: The most specific rule wins and equally specific rules block the invoice as ambiguous

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-040 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.17) |
| Acceptance Criterion | [AC-8](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-15](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Approved rules: a country-wide rule and a state rule for the same country; two different rules with the same scope; a rule for class BOOKS and an "any class" rule; a Business-only rule and an "everyone else" rule.

## Steps
1. Calculate for a customer in the state with the state rule.
2. Calculate for a customer elsewhere in the country.
3. Calculate with two different rules of the same specificity.
4. Calculate a BOOKS product, a SAAS product and an unclassified product.

## Expected Result
Step 1: the state rule is used. Step 2: the country-wide rule. Step 3: blocked with `AMBIGUOUS_RULES` and a message naming both rules. Step 4: the class rule applies only to its class; an unclassified product matches only rules whose class is Any, and a class-specific rule does not apply to an unclassified product.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/InternalTaxEngineTest.java` — `theMostSpecificRuleWins`, `twoDifferentRulesOfTheSameSpecificityAreAmbiguous`, `theTaxClassSelectsTheRuleAndAnUnclassifiedProductOnlyMatchesAnyClassRules`, `aClassSpecificRuleDoesNotApplyToAnUnclassifiedProduct`, `customerTypeAndTaxIdConditionsNarrowARule`, `theSameRuleFamilyIsUsedAtMostOncePerVersion`

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
