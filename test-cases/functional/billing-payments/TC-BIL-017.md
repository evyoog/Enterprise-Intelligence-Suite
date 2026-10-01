# TC-BIL-017: Offline bank details are saved by a billing admin only and shown on the offline result

| Field | Value |
|---|---|
| Test Case ID (required) | TC-BIL-017 |
| Requirement ID (required) | [REQ-BIL-001](../../../docs/02-requirements/FRD/billing-payments/requirement.md) (.18–.21, decision C55) |
| Acceptance Criterion | [AC-29](../../../docs/02-requirements/FRD/billing-payments/acceptance-criteria.md) |
| Test Plan | [TESTPLAN-BIL-001](TESTPLAN-BIL-001.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A platform admin with `MANAGE_BILLING` and a user without it.

## Steps
1. Admin → Billing → **Billing settings**: enter account name, bank, account number and IFSC; Save.
2. Generate an offline invoice as a customer.
3. As a user without `MANAGE_BILLING`, call `GET/PUT /api/admin/billing/settings/offline`.

## Expected Result
Step 1: invalid IFSC/SWIFT lengths and missing required fields are refused before saving; Save is disabled until something changes; an unsaved-changes warning shows while editing; "Bank details saved." toast. Step 2: the saved details appear on the offline result. Step 3: 403. Nothing is read from or written to `config/secrets.env`.

## Automated coverage
- `frontend/src/pages/admin/AdminBillingSettingsPage.test.tsx` — full file
- `backend/src/test/java/com/vyoog/eisplatform/modules/billing/service/CheckoutOfflinePaymentTest.java` — `bankDetailsAreSavedAndShownOnTheOfflineResult`
- `backend/src/test/java/com/vyoog/eisplatform/config/OfflineBillingAuthorizationTest.java` — `onlyABillingAdminCanRecordOfflinePaymentsOrSeeBankDetailsSettings`

## Actual Result
The automated tests passed on 2026-10-01.

## Status
Passed (automated run 2026-10-01)

## Linked Defect (if failed)
None.
