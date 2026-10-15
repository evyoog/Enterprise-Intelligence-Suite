# TESTPLAN-MKT-005: Product evaluation (free trials, demo requests, compare)

| Field | Value |
|---|---|
| Feature ID (required) | FTR-MKT-005 |
| Requirement(s) covered | [REQ-MKT-005](../../../docs/02-requirements/FRD/product-evaluation/requirement.md) (and the updates to [subscription-lifecycle](../../../docs/02-requirements/FRD/subscription-lifecycle/requirement.md), [renewal-reminders](../../../docs/02-requirements/FRD/renewal-reminders/requirement.md), [ticket-management](../../../docs/02-requirements/FRD/ticket-management/requirement.md), [provisioning-contract](../../../docs/02-requirements/FRD/provisioning-contract/requirement.md), [service-instances](../../../docs/02-requirements/FRD/service-instances/requirement.md), [cart-checkout](../../../docs/02-requirements/FRD/cart-checkout/requirement.md) and [catalog-showcase](../../../docs/02-requirements/FRD/catalog-showcase/requirement.md)) |
| Decision | [C102](../../../docs/01-business/roadmap/open-decisions.md#c102) |
| Author | Not specified |
| Status | Draft (the FRD is Approved, 2026-10-15; built 2026-10-15; engineering defaults to confirm) |

## Scope
Every acceptance criterion in [`acceptance-criteria.md`](../../../docs/02-requirements/FRD/product-evaluation/acceptance-criteria.md): TC-MKT-049 to TC-MKT-071 (TC number = 48 + the AC number). Out of scope (Not specified): a sandbox (03.02.01.03), view prerequisites (03.02.01.04), a trial length per plan, extending a trial, when the trial data is deleted, a trial with a card. LE-1 (learning and certification) and PO-1 (policies) are recorded in C102 and have nothing built, so nothing to test.

## Test Strategy
| Level | Approach |
|---|---|
| Unit / integration | Backend: `TrialDemoCompareTest` (12 tests on the H2-backed Spring context: trial start, one per product, guards, reminder, expiry, buying, organization trial, demo request, compare, validation) and `EvaluationAuthorizationTest` (2 MockMvc tests through the real security chain). The hourly job is not scheduled in tests: the service methods (`expireOverdueSubscriptions`, `remindEndingTrials`) are called directly. Frontend (jest-axe, API mocked): `ProductDetailPage.test.tsx` (Try it first), `MySubscriptionsPage.test.tsx` and `OrganizationSubscriptionsSection.test.tsx` (trial), `ShowcaseForms.test.tsx` and `EditProductPage.test.tsx` (trial days), `CompareTray.test.tsx` and `CatalogComponents.test.tsx` (tick and tray), `ComparePage.test.tsx`. |
| Manual | The event payload (`outbox_event`), the organization guards, the tax estimate with real tax rules and the external provider, the cart with a running trial, the renewal-reminder skip, the keyboard and Spanish checks. |
| Security (SAST/DAST) | Security cases: TC-MKT-052 (organization trial needs an administrator or Manage billing) and TC-MKT-064 (compare is public and read-only; trial and demo need sign-in). |
| Performance | Not specified |
| Acceptance / UAT | Manual check in the dev environment after V042 is applied and a product has trial days |

## Environments
Local and CI: automated tests. Manual checks: dev environment with a local PostgreSQL (never the shared database); migration V042 applied by hand after V041.

## Test Cases in This Plan
| Test Case ID | Title | Acceptance criteria | Priority | Automated? | Status |
|---|---|---|---|---|---|
| [TC-MKT-049](TC-MKT-049.md) | A platform administrator sets Free trial (days) on a product, 1 to 90 or empty | AC-1 | P1 | Partly | Partly passed |
| [TC-MKT-050](TC-MKT-050.md) | An individual starts a free trial: an active trial subscription on the highest plan | AC-2 | P0 | Yes | Passed |
| [TC-MKT-051](TC-MKT-051.md) | One trial per customer and product, ever; only where the product offers one | AC-3 | P0 | Yes | Passed |
| [TC-MKT-052](TC-MKT-052.md) | An organization trial needs an administrator or Manage billing and has the organization's seats | AC-4 | P0 | Yes | Passed |
| [TC-MKT-053](TC-MKT-053.md) | A trial provisions like any subscription: service instance and event with trial true | AC-5 | P0 | Partly | Partly passed |
| [TC-MKT-054](TC-MKT-054.md) | A trial cannot be renewed, set to auto-renew or moved to another plan; an organization trial cannot change seats | AC-6 | P0 | Partly | Partly passed |
| [TC-MKT-055](TC-MKT-055.md) | The owner is reminded once, 3 days before the trial ends | AC-7 | P1 | Yes | Passed |
| [TC-MKT-056](TC-MKT-056.md) | A trial that ends expires at once with no grace period and its instance is deprovisioned | AC-8 | P0 | Yes | Passed |
| [TC-MKT-057](TC-MKT-057.md) | Buying during a trial turns it into a normal subscription that starts after payment | AC-9 | P0 | Yes | Passed |
| [TC-MKT-058](TC-MKT-058.md) | Buying through the cart: after the trial has ended, and while it is still running | AC-10 | P0 | Yes | Passed |
| [TC-MKT-059](TC-MKT-059.md) | A demo request opens a support ticket in the category "Demo request" with SLA deadlines | AC-11 | P0 | Yes | Passed |
| [TC-MKT-060](TC-MKT-060.md) | Comparing plans multiplies each price by the seats in the item's own currency | AC-12 | P0 | Yes | Passed |
| [TC-MKT-061](TC-MKT-061.md) | A product alone is compared at its entry plan; a product without plans at its flat price; seats default to 1 | AC-13 | P1 | Yes | Passed |
| [TC-MKT-062](TC-MKT-062.md) | The tax estimate: not signed in, no billing details, estimated, or at checkout | AC-14 | P0 | Partly | Partly passed |
| [TC-MKT-063](TC-MKT-063.md) | The compare call refuses bad items and seats with the reason | AC-15 | P1 | Yes | Passed |
| [TC-MKT-064](TC-MKT-064.md) | Security: compare is public and read-only; trials and demo requests need sign-in | AC-16 | P0 | Yes | Passed |
| [TC-MKT-065](TC-MKT-065.md) | The product page "Try it first" panel: trial, demo and tutorials | AC-17 | P0 | Yes | Passed |
| [TC-MKT-066](TC-MKT-066.md) | My subscriptions shows a trial with the days left and Subscribe, and no renewal controls | AC-18 | P0 | Yes | Passed |
| [TC-MKT-067](TC-MKT-067.md) | The catalog Compare tick and tray | AC-19 | P1 | Yes | Passed |
| [TC-MKT-068](TC-MKT-068.md) | The compare page: items, seats, plans, trial badge, features, tax and add to cart | AC-20 | P0 | Yes | Passed |
| [TC-MKT-069](TC-MKT-069.md) | Regression: the renewal reminders skip trials | AC-21 | P1 | No | Not Run |
| [TC-MKT-070](TC-MKT-070.md) | Regression: subscriptions, renewal, grace, seats, cart and organization purchase of a normal subscription are unchanged | AC-22 | P0 | Yes | Passed |
| [TC-MKT-071](TC-MKT-071.md) | The evaluation screens are accessible and translated | AC-23 | P1 | Yes | Partly passed |

## Entry Criteria
FRD Approved; migration V042 applied.

## Exit Criteria
- All automated cases passing; the manual and partly manual cases run and passed before UAT sign-off.
## Blocked or partial
- **Not covered by an automated test:** the backend validation of trial days (TC-MKT-049), the `trial` field of the event payload (TC-MKT-053), the organization renew, plan and seat guards (TC-MKT-054), the `TRIAL_EXPIRED` audit entry and the expiry event (TC-MKT-056), the tax estimate with `ESTIMATED` and `AT_CHECKOUT` (TC-MKT-062), the renewal-reminder skip (TC-MKT-069), a free plan and the organization purchase over a trial (TC-MKT-057), and an unpublished product or the audience rule on a trial (TC-MKT-051).
- Frontend tests mock the API, so none sends a real request to the backend; there is no end-to-end browser test.
- The organization's trial sends no notice (BR-EVL-010); this is a stated limit, not a test.

## Risks
The engineering defaults of C102 are to be confirmed by the product owner. The trial runs in the hosted product, which must create the tenant and database from the `SubscriptionCreated` event; the provisioning contract is still a Draft ([REQ-ORD-002](../../../docs/02-requirements/FRD/provisioning-contract/requirement.md)).
