# TC-BIL-025: Full offline payment details and accepted offline methods

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-025 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.21, .24; C60) |
| Decision | [C60](../../../docs/01-business/roadmap/open-decisions.md#c60) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
As TC-BIL-024.

## Steps
1. Open Offline payments; fill bank, branch, account type, IFSC, UPI ID, cheque payee.
2. Switch all three accepted methods off and save.
3. Turn Bank transfer back on and save.
4. Record a Cheque payment against an offline invoice.

## Expected Result
The preview shows the customer-facing bank block. Step 2 is refused ("Accept at least one offline payment method."). Step 3 saves. Step 4: the dialog offers only Bank transfer; the API refuses Cheque (400). The Pay-by-invoice email and offline invoice include branch, IBAN/UPI ID, cheque payee and instructions when set.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/BillingSettingsServiceTest.java` — `fullOfflineDetailsAreSavedAndAnOfflineMethodThatIsOffIsRefused`
- `frontend/src/pages/admin/AdminBillingSettingsPage.test.tsx` — "saves the full offline details and needs at least one accepted method"

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
