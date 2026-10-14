# TC-BIL-033: A rule goes from Draft to Approved through a different administrator (maker and checker)

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-033 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.10, .11, .14) |
| Acceptance Criterion | [AC-4](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-20](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-22](../../../docs/02-requirements/FRD/tax-rules/business-rules.md), [BR-23](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
Two platform administrators, Admin 1 and Admin 2. The setting "Allow the same administrator to approve" is Off.

## Steps
1. As Admin 1 create a rule for India with components CGST 9 and SGST 9 (within state) and Save: it is a Draft.
2. Submit it for approval.
3. As Admin 1 try to Approve it.
4. As Admin 2 Approve it.
5. Create a second rule, submit it, and as Admin 2 Reject it with a reason; edit and resubmit it.
6. Switch "Allow the same administrator to approve" On and let Admin 1 approve a rule Admin 1 edited.
7. Create another approved rule with the same scope and overlapping dates and try to approve it.

## Expected Result
Step 3 is refused with the server message (the same administrator). Step 4: the rule is Approved and active. Step 5: a Rejected rule shows its reason, can be edited and resubmitted. Step 6 is allowed only while the policy is On (and the policy change is audited as `TAX_APPROVAL_POLICY_CHANGED`). Step 7: approval is refused because of the overlap. Approved rules cannot be edited or deleted; Draft and Rejected rules can be deleted.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/tax/service/TaxRuleServiceTest.java` — `aRuleGoesFromDraftThroughApprovalToActive`, `theAdministratorWhoEditedARuleCannotApproveItUnlessThePolicyAllowsIt`, `theSelfApprovalPolicyLetsASingleAdministratorWork`, `aRejectedRuleKeepsItsReasonCanBeEditedAndResubmitted`, `anApprovedRuleCannotBeEditedOrDeletedButDraftsCanBeDeleted`, `anOverlappingRuleOfTheSameScopeCannotBeApproved`
- `frontend/src/pages/admin/AdminTaxConfigurationPage.test.tsx` — "approves, submits and deactivates through the API and refreshes the status", "shows the server message when an approval is refused (the same administrator)", "rejects a rule with a reason", "lets a single administrator approve only when the policy is switched on"

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
