# TC-CAT-025: Case studies

| Field | Value |
|---|---|
| Test Case ID (required) | TC-CAT-025 |
| Requirement ID (required) | [REQ-CAT-004](../../../docs/02-requirements/FRD/product-content/requirement.md) — [acceptance criteria](../../../docs/02-requirements/FRD/product-content/acceptance-criteria.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
An application.

## Steps
1. Save a case study without a customer name.
2. Save one with a PDF and a logo, publish it, open the product page.
3. Choose a PDF as the logo.

## Expected Result
Refused without a customer; saved with its files; the page shows customer, logo, problem, result and the PDF button; a PDF is not accepted as a logo.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/productcontent/service/ProductContentServiceTest.java` — `aCaseStudyNeedsACustomerAndMayHaveAPdfAndALogo`
- `frontend/src/components/productcontent/ProductContent.test.tsx` — `shows every kind of content and passes the accessibility checks`

## Actual Result
The automated tests above passed on 2026-10-07.

## Status
Passed (automated run 2026-10-07)

## Linked Defect (if failed)
