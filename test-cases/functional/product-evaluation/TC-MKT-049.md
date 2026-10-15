# TC-MKT-049: A platform administrator sets Free trial (days) on a product, 1 to 90 or empty

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-049 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-1](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-001, BR-EVL-002](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P1 |
| Type | Functional |
| Automated | Partly |

## Preconditions
A platform administrator (`MANAGE_CATALOG`) and an existing product.

## Steps
1. Open the product edit form (`/admin/products/:id/edit`): the Free trial (days) field shows the saved value, or is empty.
2. Enter 14 and save; read the product: `trialDays` is 14.
3. Empty the field and save: `trialDays` is null (the product offers no trial).
4. On the form enter 0, 91 and 1.5: the form shows "Enter a whole number of days from 1 to 90." and does not send.
5. Call `PUT /products/{id}` directly with `trialDays` 0 and 91, and again with the field left out.

## Expected Result
Steps 2 and 3: saved as asked. Step 4: refused on the form. Step 5: 400 for 0 and 91; the call that leaves the field out clears the trial. The field has a label and the hint "Leave empty to offer no free trial", and its text is in `en.json` and `es.json`.

## Automated coverage
- `frontend/src/components/admin/ShowcaseForms.test.tsx` — "sends trialDays as null when the field is empty and as a number when set", "refuses trial days outside 1-90 or not whole", "shows an existing trial and clears it when the field is emptied"
- `frontend/src/pages/admin/EditProductPage.test.tsx` — "hands the saved free-trial days (C102) to the form"

The form part is automated. **Not covered by an automated test:** the backend validation (`@Min(1) @Max(90)` on `ProductCreateRequest`, the database check `chk_products_trial_days`), the 400 for a direct call, and that an update that omits `trialDays` clears the trial. These are in the code and migration V042.

## Actual Result
The frontend tests passed on 2026-10-15 (the eight C102 test files: 108 tests, 0 failures). The backend part was not run.

## Status
Partly passed: frontend automated run 2026-10-15; backend validation Not Run

## Linked Defect (if failed)
None.
