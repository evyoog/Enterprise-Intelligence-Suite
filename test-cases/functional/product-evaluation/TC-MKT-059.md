# TC-MKT-059: A demo request opens a support ticket in the category "Demo request" with SLA deadlines

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-059 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-11](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-018, BR-EVL-019, BR-EVL-020](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A signed-in customer and a published product.

## Steps
1. Call `POST /me/demo-requests` with the product, a preferred time and a message.
2. Read the ticket in My tickets and as a support administrator.
3. Call it with no product, with an unknown product and with a message of 1001 characters.

## Expected Result
Step 1: a ticket with category "Demo request", subject "Demo request: {product}", the preferred time and the message in its description, status OPEN, priority MEDIUM and a first-response deadline. Step 3: 400 "Choose a product.", 404 "Product not found" and 400 "Keep each field under 1000 characters." In the UI the dialog sends the form, shows "We opened ticket #{id}" with a link to My tickets, keeps the form on a failure and refuses an over-long message before sending.

## Automated coverage
- `backend/src/test/java/com/vyoog/eisplatform/modules/evaluation/service/TrialDemoCompareTest.java` — `aDemoRequestIsATicketInTheDemoRequestCategoryWithSlaDeadlines`
- `frontend/src/pages/ProductDetailPage.test.tsx` — "requests a demo: sends the form, shows the ticket number and a link to My tickets", "sends a demo request with no optional fields when they are left empty", "shows the error and keeps the form when the demo request fails", "rejects an over-long message before sending", "sends a signed-out visitor to sign in to request a demo"

**Not covered by an automated test:** the 1000-character limit on the backend (the dialog stops it earlier), an unpublished product, and the ticket in the support team's list.

## Actual Result
The backend tests (`TrialDemoCompareTest`, 12 tests, and `EvaluationAuthorizationTest`, 2 tests) passed on 2026-10-15; the full backend run of the same day had 876 tests, 0 failures and 28 skipped. The frontend tests passed on 2026-10-15 (the eight C102 test files: 108 tests, 0 failures).

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
