# TC-BIL-116: AC-46 The Add, Edit and Remove dialogs guide the administrator and show the server's refusal

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-116 |
| Requirement ID (required) | [REQ-BIL-003](../../../docs/02-requirements/FRD/payment-gateways/requirement.md) (REQ-BIL-003.16, .17) |
| Acceptance Criterion | [AC-46](../../../docs/02-requirements/FRD/payment-gateways/acceptance-criteria.md) |
| Business rule(s) | [BR-20 to BR-25](../../../docs/02-requirements/FRD/payment-gateways/business-rules.md) |
| Product-owner acceptance point(s) | None (added by C99) |
| Decision | [C99](../../../docs/01-business/roadmap/open-decisions.md#c99) |
| Test Plan | [TESTPLAN-BIL-003](TESTPLAN-BIL-003.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
The Payment Gateways page with Razorpay and one added gateway (Acme Pay); the API mocked in the frontend test.

## Steps
1. Open **Add payment gateway**: read the info text and the warning; **Add gateway** is disabled.
2. Type a name, check the derived code; type a credential label, check the derived name; add and remove a credential.
3. Submit a gateway whose code is taken (the server answers 409).
4. Open **Edit** on Acme Pay; try to change the code and a credential name; change the name; Save.
5. Open **Remove** on Acme Pay and confirm.
6. Look for a "Planned integrations" section.

## Expected Result
The dialog says that values are stored encrypted and never shown again and that a gateway added here has no payment integration; Add stays disabled until the form is valid and the first problem is named. The code follows the name (MY_LOCAL_BANK) and the credential name follows the label (apiSecretKey). A refusal appears inside the dialog ("A payment gateway with the code ACME already exists.") and the dialog stays open. Edit has the code and the existing credential names locked and no Value inputs; Save sends the definition and the toast says "Acme Payments was updated.". Remove asks "Remove Acme Pay?" and, once confirmed, deletes and says "Acme Pay was removed.". There is no "Planned integrations" section anywhere.

## Automated coverage
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "adds a gateway from the form: pre-fills from a template, hides secrets, sends the definition and values once"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "adds a gateway from scratch: derives the code and credential names and keeps the Add button off until the form is valid"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "shows the server message when adding is refused"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "edits the definition of an added gateway without asking for credential values, and removes it after confirmation"
- `frontend/src/pages/admin/AdminPaymentGatewayPage.test.tsx` — "labels a gateway added by an administrator as having no payment integration and offers only what works" (also asserts no "Planned integrations" text)

Not covered by an automated test: Cancel and Escape on each dialog, keyboard-only use, the phone layout, an accessibility scan of the open dialog, and the Spanish text (`es.json` carries the same keys). Check these manually.

## Actual Result
The automated tests listed above passed (backend: Surefire reports of 2026-10-14 (`backend/target/surefire-reports`, `mvn -B verify`): GatewayManagementServiceTest 18 tests, RazorpayGatewayTest 5, CredentialCipherTest 3, PaymentGatewayAuthorizationTest 4, BillingServiceTest 12, no failures; frontend: `npx vitest run` of AdminPaymentGatewayPage, AdminBillingSettingsPage and CheckoutPage tests on 2026-10-14, 48 passed).

## Status
Passed (automated run 2026-10-14)

## Linked Defect (if failed)
None.
