# TC-BIL-058: Only MANAGE_BILLING can use tax endpoints; every change is audited

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-058 |
| Requirement ID (required) | [REQ-BIL-002](../../../docs/02-requirements/FRD/tax-rules/requirement.md) (REQ-BIL-002.47) |
| Acceptance Criterion | [AC-21](../../../docs/02-requirements/FRD/tax-rules/acceptance-criteria.md) |
| Business rules | [BR-6](../../../docs/02-requirements/FRD/tax-rules/business-rules.md) |
| Test Plan | [TESTPLAN-BIL-002](TESTPLAN-BIL-002.md) |
| Priority | P0 |
| Type | Security |
| Automated | Partly |

## Preconditions
A signed-out caller, a signed-in user without `MANAGE_BILLING` (for example `ROLE_ACTIVITY_USER`), and a platform administrator.

## Steps
1. Call every tax read endpoint (status, rules, classes, products, registrations, exemptions, reference, blocked invoices) as each caller.
2. Call `PUT /mode`, `POST /provider/validate`, `POST /rules/{id}/approve`, `DELETE /rules/{id}` and `POST /blocked-invoices/{id}/retry` as the non-admin user.
3. As the administrator create, approve, reject, activate, deactivate and version a rule, change a class, registration, exemption, the approval policy, the provider settings and the mode, and retry a Draft.
4. Open the audit log.

## Expected Result
Steps 1 and 2: 401 without a token, 403 without `MANAGE_BILLING`, 200 for the administrator. Step 4: one audit entry per change naming the action (for example `TAX_RULE_CREATED`, `TAX_RULE_APPROVED`, `TAX_RULE_VERSIONED`, `TAX_PROVIDER_SETTINGS_CHANGED`, `TAX_PROVIDER_VALIDATED`, `TAX_ENGINE_MODE_CHANGED`, `TAX_APPROVAL_POLICY_CHANGED`, `INVOICE_TAX_RETRIED`) and the acting administrator; a blocked calculation is audited as `INVOICE_TAX_BLOCKED` and an issued invoice as `INVOICE_GENERATED`.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/config/TaxAuthorizationTest.java` — `everyTaxEndpointNeedsSignInAndTheBillingPermission`

Not automated: No automated test asserts the audit entries; step 3 and 4 are checked by hand.

## Actual Result
Not yet executed under this test case ID. The automated tests listed above exist in the repository.

## Status
Not Run

## Linked Defect (if failed)
None.
