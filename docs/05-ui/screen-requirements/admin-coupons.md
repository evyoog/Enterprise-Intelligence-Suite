# Screen: Coupons (platform administrator)

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-001.34, .35](../../02-requirements/FRD/billing-payments/requirement.md); business rules [BR-24 to BR-29](../../02-requirements/FRD/billing-payments/business-rules.md) |
| Decision | [C100](../../01-business/roadmap/open-decisions.md#c100) (PB-1 note: a coupon code field, configured by the platform administrator) |
| Route | `/admin/billing/coupons`, inside the signed-in tool frame ([application-layout.md](application-layout.md)) |
| Sidebar | Admin → Billing → **Coupons** (ticket icon, `nav` key `coupons`), under Operations |
| Permissions | Platform permission `MANAGE_BILLING`. Others: the link is hidden and the API returns 403 |
| Built | 2026-10-14 — `frontend/src/pages/admin/AdminCouponsPage.tsx`, `frontend/src/api/couponApi.ts` |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md). Colours from the unified MUI theme ([C45](../../01-business/roadmap/open-decisions.md#c45)), light and dark mode.

## Layout
- **Page header:** ticket icon (amber), eyebrow "Billing", title **"Coupons"**, subtitle "Codes customers enter in the cart for a discount. One code per cart; tax is calculated on the discounted amount.", and an **Add coupon** button.
- **Table** (`aria-label` "Coupons"), newest first, one row per coupon:

| Column | Content |
|---|---|
| Code | The code in monospace and bold, with the description below it in small muted text |
| Discount | "{n}% off" for a percent coupon, or "{amount} off" in the coupon's currency (the platform money format) |
| Expires | Date and time in the browser's locale |
| Status | Chip: **Active** (green), **Disabled** (neutral), **Expired** (warning) |
| Redeemed | The number of invoices the coupon has been used on |
| Actions | **Edit**; **Disable** or **Enable** (hidden for an Expired coupon); **Delete**. Each has an accessible name with the code ("Edit SAVE10") |

## Add and edit dialog
Title "Add coupon" or "Edit coupon". Errors appear after the first **Save** and clear as a field is edited; the server's refusal is shown in an alert at the top of the dialog.

| Field | Type | Required | Validation | Message (i18n key) |
|---|---|---|---|---|
| Code | Text, capitals, max 40; **locked when editing** | Yes | 3 to 40 letters, digits, `-` or `_` | "Use 3 to 40 letters, digits, dashes or underscores." (`admin.coupons.form.codeInvalid`); help "…Stored in capitals; cannot be changed later." |
| Description | Text, max 200 | No | Up to 200 characters | `admin.coupons.form.descriptionTooLong` |
| Discount type | Select: **Percent off** / **Fixed amount** | Yes | — | `admin.coupons.form.type*` |
| Percent off | Number 1 to 99, shown for Percent | Yes (percent) | 1 to 99 | "Enter a percentage from 1 to 99." |
| Amount off + Currency | Number (main unit, two decimals) and select INR, USD, EUR, GBP, shown for Fixed | Yes (fixed) | Above zero | "Enter an amount above zero." The amount is sent in minor units with the currency |
| Expires on | Date and time (`datetime-local`) | Yes | **In the future** (also when editing) | "Set the expiry date and time." / "The expiry must be in the future."; help "Date and time in your time zone ({zone})." The value is sent as an instant |

Buttons **Cancel**, **Save** (spinner while saving). The server rules, which the page only anticipates, are in BR-24: a duplicate code is refused (409), a percent outside 1 to 99 is refused, a fixed coupon needs an amount and a three-letter currency. *As built the page also refuses a past expiry when editing, which the API accepts; see the FRD open points.*

## Behaviour
| Action | Behaviour |
|---|---|
| Add | Creates the coupon; toast "Coupon {code} created"; the list reloads |
| Edit | Changes everything but the code; toast "Coupon {code} saved". Past redemptions are unaffected |
| Disable / Enable | Toggles `enabled` without a confirmation; the row updates; toast "Coupon {code} disabled" / "enabled". A disabled coupon is refused in carts and at invoicing with "This coupon is not active." |
| Delete | Confirmation dialog "Delete {code}?" — "This permanently removes the coupon. A coupon that has been redeemed cannot be deleted; disable it instead." Buttons **Delete** / cancel. A redeemed coupon: the server's 409 message is shown in an alert and the row stays. Toast "Coupon {code} deleted" on success |

## States
| State | Behaviour |
|---|---|
| Loading | A skeleton block |
| Empty | Ticket icon, **"No coupons yet"**, "Add a coupon so customers can enter its code in the cart." and an **Add coupon** button |
| Load error | The backend message (or "Coupons could not be loaded.") in an alert with **Retry** |
| Action error | The backend message (or "That action could not be completed.") in a dismissible alert; the list stays |

## Accessibility and i18n
The table has an accessible name; every row action names its coupon; dialogs are labelled by their title; field errors are helper text; statuses are text chips, not colour alone; axe test on the list. All strings are in `frontend/src/i18n/locales/{en,es}.json` under `admin.coupons.*` and `appShell.nav.coupons`.

## API used
[billing-payments.md](../../06-api/api-requirements/billing-payments.md): `GET`, `POST /admin/billing/coupons`, `PUT`, `DELETE /admin/billing/coupons/{id}`, `PATCH /admin/billing/coupons/{id}/enabled`.
