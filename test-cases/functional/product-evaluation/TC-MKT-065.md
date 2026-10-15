# TC-MKT-065: The product page "Try it first" panel: trial, demo and tutorials

| Field | Value |
|---|---|
| Test Case ID (required) | TC-MKT-065 |
| Requirement ID (required) | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (C102) |
| Acceptance Criterion | [AC-17](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md) |
| Business rule(s) | [BR-EVL-003, BR-EVL-005, BR-EVL-007](../../../docs/02-requirements/FRD/product-evaluation/business-rules.md) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Test Plan | [TESTPLAN-MKT-005](TESTPLAN-MKT-005.md) |
| Priority | P0 |
| Type | Functional |
| Automated | Yes |

## Preconditions
A product with trial days and published videos; a product with neither; a visitor, an individual, an organization administrator, an ordinary member, a customer who already has a subscription.

## Steps
1. Open the product page as a visitor; click Start free trial and Request a demo.
2. As an individual click Start free trial.
3. As an organization administrator click Start free trial.
4. As an ordinary member and as a customer with a subscription, look at the trial button.
5. Click Request a demo, fill the form and send it.
6. Click Watch tutorials; open the page of a product with no videos and of one with no trial days.

## Expected Result
Visitor: sent to sign in and back. Individual: the success message with the end date and a link to My subscriptions. Organization: the trial is for the organization (note shown) and starts. Member: the button is disabled with "Only an organization administrator or a person with Manage billing can start a free trial …"; a customer with a subscription: disabled with "You already have, or had, a subscription …". Demo: ticket confirmation. Tutorials: opens the Resources tab on the videos; hidden when there are none. No trial days: no trial offer, the demo button stays.

## Automated coverage
- `frontend/src/pages/ProductDetailPage.test.tsx` — "offers no free trial when the product sets no trial days, but still a demo", "shows the trial offer and sends a signed-out visitor to sign in", "starts the trial for an individual and shows the end date with a link to My subscriptions", "shows the backend message when the trial cannot be started", "disables the trial, with the reason, when the customer already has a subscription to the product", "starts the trial for the organization when the customer may buy for it", "disables the trial when the organization already has a subscription", "tells an ordinary organization member to ask an administrator", "shows the organization trial refusal from the backend", "requests a demo: sends the form, shows the ticket number and a link to My tickets", "shows Watch tutorials only when the product has videos, and opens the Resources tab on its videos", "hides Watch tutorials when published content has no videos"

The same file also covers the demo dialog ([TC-MKT-059](TC-MKT-059.md)) and the axe test ([TC-MKT-071](TC-MKT-071.md)). The API is mocked in these tests.

## Actual Result
The frontend tests passed on 2026-10-15 (the eight C102 test files: 108 tests, 0 failures).

## Status
Passed (automated run 2026-10-15)

## Linked Defect (if failed)
None.
