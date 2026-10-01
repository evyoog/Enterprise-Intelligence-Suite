# Screen: Tax rules

| Field | Value |
|---|---|
| Requirement | [REQ-BIL-002](../../02-requirements/FRD/tax-rules/requirement.md) |
| Route | `/admin/billing/tax` |
| Sidebar | Admin → Billing → **Tax** (new item alongside Invoices & payments, Payment gateway) |
| Permissions | `MANAGE_BILLING` (platform ADMIN). Others: route hidden; API returns 403 |

Shared presentation rules: [billing-ui-standards.md](billing-ui-standards.md).

## List
`<DataTable>` of tax rules with `<FilterBar>` (region, status). Columns: region, tax name, rate, method, effective from, status.

## Create / edit (side drawer)
| Field | Type | Required | Validation | i18n key |
|---|---|---|---|---|
| Region | Select, from existing platform regions | Yes | Must be an existing region | `admin.taxRules.region` |
| Tax name | Text | Yes | Non-blank, for example "GST" | `admin.taxRules.taxName` |
| Rate | Number input with a `%` suffix | Yes | 0–100, up to 2 decimals | `admin.taxRules.rate` |
| Method | Segmented control: Admin rate / Tax service | Yes | One selected; **Tax service** shows "Not configured — falls back to admin rate" until a tax-service provider is configured ([REQ-BIL-002.6](../../02-requirements/FRD/tax-rules/requirement.md)) | `admin.taxRules.method` |
| Effective from | Date | Yes | Not before today for a new rule | `admin.taxRules.effectiveFrom` |

Buttons: **Save**, **Cancel**. Saving a second enabled rule for the same region and effective-from date is refused inline ([BR-2](../../02-requirements/FRD/tax-rules/business-rules.md)).

## Calculation preview panel
Enter an amount and a region → shows the tax name, rate, tax amount, total, and which method would actually be used (Admin rate, or Tax service with its own fallback already applied). Calls `POST /admin/billing/tax-rules/preview`.

## Enable / disable
A switch per row, with a confirmation dialog naming the rule ("Disable the GST rule for India effective 1 Oct 2026?").

## States
Loading skeleton matching the table; empty ("No tax rules yet."); backend errors shown as-is with **Retry**.
