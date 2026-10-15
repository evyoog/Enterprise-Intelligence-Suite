# REQ-MKT-005 — Product evaluation: free trials, demo requests and compare

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-15 ([C102](../../../01-business/roadmap/open-decisions.md#c102), TR-1 note, TR-2 A). The engineering defaults listed under [Engineering defaults](#engineering-defaults) are applied "to confirm"; none blocks use of the feature.
**Decision:** [C102](../../../01-business/roadmap/open-decisions.md#c102) (TR-1, TR-2). Supersedes the "03.02 Product Evaluation not built" line of [C41](../../../01-business/roadmap/open-decisions.md#c41), which waited for service instances ([C101](../../../01-business/roadmap/open-decisions.md#c101) built them).
**Built:** 2026-10-15 (migration [V042](../../../../database/migrations/V042__trials_and_demo_requests.sql), apply by hand after V041: Flyway is disabled). Test cases: [TESTPLAN-MKT-005](../../../../test-cases/functional/product-evaluation/TESTPLAN-MKT-005.md).

| Field | Value |
|---|---|
| Sprint | [2027.1.3](../../../01-business/roadmap/sprints/SPRINT-2027.1.3.md) (built early, on 2026-10-15; the sprint's dates are unchanged) |
| Requirement ID | REQ-MKT-005 |
| Application | [03 Marketplace](../../../01-business/roadmap/applications/03-marketplace.md#0302-product-evaluation) |
| Application code | `APP-MKT` |
| Priority | P0 / Commit for 03.02.01 and 03.02.02 in the sprint page; the two functions that are not built are Not specified |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 03.02.01.01 | Start trial | Yes: a free trial of a product that offers one, for an individual or an organization (.1 to .9) |
| 03.02.01.02 | Request demo | Yes: a form that opens a support ticket in the category "Demo request" (.10) |
| 03.02.01.03 | Launch sandbox | **No. Not specified.** The product owner did not describe it and no source does |
| 03.02.01.04 | View prerequisites | **No. Not specified.** Same |
| 03.02.02.01 | Compare products | Yes: 2 to 4 products side by side (.12) |
| 03.02.02.02 | Compare plans | Yes: an item can be a product with one chosen plan, so the plans of one product can be compared (.12) |
| 03.02.02.03 | Estimate cost | Yes: price x seats + tax for every item (.13) |

"Watch tutorials" (the third way to explore before a trial, TR-1) is a link to the product's existing videos and needs no new function (.11).

## Summary
Before buying, a customer can **explore** a product, **request a demo**, **watch the product's video tutorials** or **start a free trial** ([C102](../../../01-business/roadmap/open-decisions.md#c102) TR-1). A product offers a trial when a platform administrator sets its **trial days** (1 to 90; empty means no trial, which is the default for every product). A trial is a normal **subscription** marked `trial`, on the product's **highest plan** (all features), with **no card**, **no auto-renew** and **no grace period**. It goes through the same path as any new subscription, so a **service instance** is created and the **"provision requested" event** goes out carrying `trial: true`: **the product creates the customer's tenant and database; EIS does not** ([REQ-ORD-002](../provisioning-contract/requirement.md), [REQ-SRM-001](../service-instances/requirement.md)). The trial ends when its days are over: the subscription expires and the product is told to deprovision. The customer can **buy** at any time through the normal cart and checkout, which turns the trial into a normal subscription. A customer or organization gets **one trial per product, ever**.

A **demo request** is a support ticket in the category **"Demo request"**, so the support team and the [SLA](../sla-management/requirement.md) handle it. The **compare page** ([C102](../../../01-business/roadmap/open-decisions.md#c102) TR-2 A) shows 2 to 4 items (a product, or a product with one plan) side by side with a **cost estimate of price x seats + tax**. It is open to everyone; the tax line needs a signed-in customer with billing details.

## Actors
- **Visitor** (not signed in): sees the product page and the compare page (without a tax estimate); signs in to start a trial or ask for a demo.
- **Individual customer**: starts a trial, requests a demo, compares, buys.
- **Organization administrator**, or a member with **Manage billing** (`MANAGE_ORGANIZATION_BILLING`): starts a trial for the organization (the same people who may buy directly, [C100](../../../01-business/roadmap/open-decisions.md#c100) CA-2). Other members can compare, request a demo and see the trial, but not start one.
- **Platform administrator** (`MANAGE_CATALOG`): sets a product's trial days on the product form.
- **Support team** (`MANAGE_SUPPORT_TICKETS`): works the "Demo request" tickets.
- **Hosted product** (a system): receives the provision and deprovision events and creates or removes the trial tenant.
- **System**: the hourly subscription job ends trials and sends the "3 days left" notice.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-MKT-005.1 | A product **offers a free trial** when its **trial days** are set: a whole number from **1 to 90** (`products.trial_days`). **Empty = no trial**, the default for every product. A value outside 1 to 90 is refused (400). The trial length is the same for every plan: a trial is always on the highest plan. | Must |
| REQ-MKT-005.2 | **Start trial (individual).** A signed-in customer starts a trial of a published (ACTIVE) product that offers one, through `POST /me/trials`. It creates a **subscription with `trial` = true**, status **ACTIVE**, on the product's **highest plan** (the plan with the highest price; none for a product without plans), for **1 seat**, starting now and ending `trialDays` days later (`expiresAt`), with **auto-renew off**. **No card is asked for and no invoice is created.** | Must |
| REQ-MKT-005.3 | **Start trial (organization).** A member of an organization starts the organization's trial through `POST /organization/me/trials`. Only an **organization administrator** or a person with **Manage billing** may (the rule of buying directly, `OrderService#canPurchaseDirectly`); anyone else is refused (403). The trial has **the organization's seats** (the licensed seats, at least the active members, at least 1). Member product access is refreshed, as for any organization entitlement change. | Must |
| REQ-MKT-005.4 | **One trial per product, ever.** A customer (or organization) that already has **or had** a subscription to the product, in any status, cannot start a trial of it (400). A product that offers no trial refuses (400). A product that is not published is "not found" (404). The audience rule of the product applies as for buying ([REQ-CAT-005](../offering-management/requirement.md)): an organization-only product refuses an individual and the reverse (409). | Must |
| REQ-MKT-005.5 | **Provisioning.** The trial goes through the same path as any new subscription: a **service instance** is created (`PROVISIONING`, [REQ-SRM-001.1](../service-instances/requirement.md)) and the **`SubscriptionCreated` event** is published with the subscription's fields and **`trial: true`** ([REQ-INT-002](../event-platform/requirement.md), contract [REQ-ORD-002](../provisioning-contract/requirement.md)), so the **product creates the trial tenant and its database with all features**. EIS creates no database itself. A trial is audited (`TRIAL_STARTED`). | Must |
| REQ-MKT-005.6 | **A trial is not a normal subscription.** It **cannot be renewed**, **auto-renew cannot be turned on**, its **plan cannot be changed** and, for an organization, its **seats cannot be changed**; each is refused (400) with a message that says to subscribe. Suspend and cancel work as for any subscription. | Must |
| REQ-MKT-005.7 | **Notices.** The owner is told when the trial **starts**, **3 days before it ends** (once) and **when it ends**, in the usual in-app notification and email. An organization-owned subscription is not notified by the subscription feature ([REQ-SUB-001](../subscription-lifecycle/requirement.md), BR-SUB-009), so an **organization's trial sends no notice** (see [Open points](#open-points)). A trial that is shorter than 3 days is reminded at the next hourly run. The trial's start and end are audited (`TRIAL_STARTED`, `TRIAL_EXPIRED`). | Must |
| REQ-MKT-005.8 | **The trial ends.** When `expiresAt` has passed, the hourly subscription job sets the subscription to **EXPIRED at once, with no grace period** (a trial is never read-only), the **service instance is deprovisioned**, a **`SubscriptionChanged` event with status `EXPIRED`** tells the product to deprovision, and the member product access of an organization is refreshed. An EXPIRED subscription grants nothing. **When the trial data is deleted is the product's decision**: EIS sends the request only. | Must |
| REQ-MKT-005.9 | **Buying.** Subscribing (the normal cart and checkout, or the organization's direct purchase or approved order) **turns the trial into a normal subscription**: `trial` becomes false, the chosen plan and the plan's renewal date and auto-renew apply, and a **paid plan starts after payment** as for any purchase. The subscription row, and so its service instance, is the same one. A trial that has ended can be bought in the same way. **Known gap:** while the trial is still **running**, the cart refuses the item as `ALREADY_SUBSCRIBED` (see [Open points](#open-points)). | Must |
| REQ-MKT-005.10 | **Request demo.** A signed-in customer asks for a demo of a published product through `POST /me/demo-requests` with an optional **preferred time** and **message** (each up to 1000 characters). It creates a **support ticket** ([REQ-SUP-001](../ticket-management/requirement.md)) with the **category "Demo request"**, the subject "Demo request: {product}" and a description with the product, the preferred time and the message. The ticket is OPEN and MEDIUM and gets its **SLA deadlines** like any ticket ([REQ-SUP-002](../sla-management/requirement.md)). A product need not offer a trial to be demoed. | Must |
| REQ-MKT-005.11 | **Watch tutorials.** The product page links to the product's **videos** (published product content and knowledge videos, [REQ-CAT-004](../product-content/requirement.md), [REQ-KNW-004](../knowledge-videos/requirement.md)). Nothing new is stored. | Must |
| REQ-MKT-005.12 | **Compare.** The **public** `GET /products/compare?items=<productId>[:<planId>],…&seats=<n>` returns **2 to 4** items side by side. An item is a **product** (shown at its **entry plan**, the plan with the lowest price, or at its flat price when it has no plans) or a **product with one of its plans**; so both products and the plans of one product can be compared. The same item twice is refused; the products must be published; a plan must belong to its product. `seats` is **1 to 100 000** (default 1). The call changes nothing. | Must |
| REQ-MKT-005.13 | **Estimate cost.** Each item shows its **unit price, billing period, seats, subtotal (price x seats)** and a **tax estimate**, in **its own currency (never converted)**. The estimate is `ESTIMATED` (tax and total from EIS's own tax rules, [REQ-BIL-002](../tax-rules/requirement.md)) only for a **signed-in customer with billing details**; otherwise `NOT_SIGNED_IN` or `NEEDS_BILLING_DETAILS`. With the **external tax provider** on, or when the EIS rules cannot tax the sale, it is `AT_CHECKOUT` ("Tax is worked out at checkout"). Each item also lists its **included features** and its **trial days**. | Must |
| REQ-MKT-005.14 | **Admin setup.** A platform administrator (`MANAGE_CATALOG`) sets **Free trial (days)** on the product form; it is saved through the existing product create and update calls (`trialDays`) and returned on the product. **A product update replaces every field, so an update that omits `trialDays` clears the trial.** | Must |
| REQ-MKT-005.15 | **Where it shows** ([ui-requirements.md](ui-requirements.md)): the product page has a **"Try it first"** panel (Start free trial, Request a demo, Watch tutorials); **My subscriptions** and the organization's subscriptions show a **"Free trial" chip** with the days left and a **Subscribe** button; the admin product form has the **"Free trial (days)"** field; the catalog cards have a **Compare** toggle and a **tray**; the public page `/compare` shows the comparison. | Must |

## Engineering defaults
Applied to make C102 work; **to confirm** with the product owner. None changes an answer of C102.

| Topic | Default | Where |
|---|---|---|
| Trial length | One `trialDays` per product, the same for every plan; a trial is always on the highest plan (the highest price) | `Product#trialDays`, `SubscriptionService#highestPlanId` |
| Range | 1 to 90 days | `ProductCreateRequest`, check `chk_products_trial_days` |
| Seats | 1 for an individual; the organization's seats for an organization (licensed seats, at least the active members, at least 1) | `SubscriptionService#startTrial`, `#startOrganizationTrial`, `#initialSeats` |
| One trial ever | Any existing subscription of the owner to the product, in any status, blocks a trial | `SubscriptionService#startTrial`, `#startOrganizationTrial` |
| Who for an organization | Organization administrator or Manage billing, the rule of buying directly | `TrialService#startForOrganization`, `OrderService#canPurchaseDirectly` |
| Notice before the end | 3 days, once; sent by the hourly job (`TRIAL_REMINDER_DAYS`) | `SubscriptionService#remindEndingTrials`, `SubscriptionExpiryJob` |
| End | Expired at once at `expiresAt` (checked hourly, so up to an hour late); no grace | `SubscriptionService#expireOverdueSubscriptions`, `#expireTrial` |
| Trial in the renewal reminders | Trials are skipped by the renewal reminders; a trial has its own notice | `RenewalReminderService` |
| Demo request | A ticket in the free-text category "Demo request"; fields up to 1000 characters; the product must be published | `DemoRequestService`, `SupportTicketService#createTicket(…, category)` |
| Compare items | 2 to 4; seats 1 to 100 000; a product without a plan is priced in USD at its flat price | `CompareService` |
| Tax estimate | From the EIS tax rules for the customer's billing country and state, the product's tax class and the customer type (an organization member is a business); amounts in the item's currency | `CompareService#tax`, `TaxRuleService#preview` |
| Features listed | The plan's included features (split on new lines, commas and semicolons), else the product's feature tags | `CompareService#features` |

## Out of scope
**Not specified:** a **sandbox** (03.02.01.03) and **view prerequisites** (03.02.01.04); a **trial length per plan** (a trial is on the highest plan); **extending a trial**; **when the trial data is deleted** (the product's decision); a **trial with a card**; a trial of a product without a trial setting; a limit on how many trials a person can run across products; a comparison saved or shared by name; a grand total across items of different currencies; notices for an organization trial.

## Dependencies
- Subscriptions ([REQ-SUB-001](../subscription-lifecycle/requirement.md), `SubscriptionService`), service instances ([REQ-SRM-001](../service-instances/requirement.md)), the event outbox ([REQ-INT-002](../event-platform/requirement.md)) and the provisioning contract ([REQ-ORD-002](../provisioning-contract/requirement.md)).
- Cart and checkout ([REQ-MKT-003](../cart-checkout/requirement.md)) for buying; tickets ([REQ-SUP-001](../ticket-management/requirement.md)) and SLA ([REQ-SUP-002](../sla-management/requirement.md)) for demo requests; tax rules ([REQ-BIL-002](../tax-rules/requirement.md)) and billing details for the estimate; renewal reminders ([REQ-SUB-004](../renewal-reminders/requirement.md)), which skip trials.
- The catalog and product form ([REQ-CAT-003](../catalog-showcase/requirement.md), [REQ-CAT-001](../product-lifecycle/requirement.md)).
- Columns from [V042](../../../../database/migrations/V042__trials_and_demo_requests.sql); [data model](../../../07-database/data-model/trials.md).

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement, business rules, workflow, acceptance criteria | this folder |
| Screens | [ui-requirements.md](ui-requirements.md) → [product-evaluation.md](../../../05-ui/screen-requirements/product-evaluation.md) |
| API | [api-requirements.md](api-requirements.md) → [product-evaluation.md](../../../06-api/api-requirements/product-evaluation.md) |
| Data model | [trials.md](../../../07-database/data-model/trials.md) (V042) |
| Code | `backend/src/main/java/com/vyoog/eisplatform/modules/evaluation/` (`TrialService`, `DemoRequestService`, `CompareService`, `EvaluationController`, `EvaluationDtos`); trial methods in `registration/service/SubscriptionService.java`; `registration/job/SubscriptionExpiryJob.java` |
| Test cases | [TESTPLAN-MKT-005](../../../../test-cases/functional/product-evaluation/TESTPLAN-MKT-005.md) |

## Decisions ([C102](../../../01-business/roadmap/open-decisions.md#c102), product owner, 2026-10-15)
| # | Question | Answer |
|---|---|---|
| TR-1 | How does a trial work? | **No option chosen; the product owner's note:** a trial also creates the database and provisions the product with all features. Before a trial the customer can explore, or request a demo, or watch the video tutorials |
| TR-2 | Comparison | **A:** a compare page, 2 to 4 items side by side, with a cost estimate of price x seats + tax |
| LE-1 | Learning and certification | "Later I will tell." Nothing built. Not part of this FRD |
| PO-1 | Policies | **A:** specific policies when needed. Nothing built. Not part of this FRD |

## Open points
1. **Buying while the trial is running is blocked by the cart.** `CartService#issuesFor` flags `ALREADY_SUBSCRIBED` for any subscription whose status is ACTIVE, and a running trial is ACTIVE, so checkout is refused until the trial has ended. The service-level purchase (`SubscriptionService#subscribeFromCart`, the organization purchase) does turn a running trial into a normal subscription and is tested; only the cart check needs to treat a trial as not subscribed. C102 asks for "buying during or after the trial". The cart was not changed in this build.
2. **An organization's trial sends no notice** (start, 3 days left, end) because `SubscriptionService#notify` skips organization-owned subscriptions. The trial's audit entries are written. Confirm whether organization administrators should be told.
3. **A trial's length is one number per product**; a trial per plan, an extension and a trial with a card are Not specified.
4. **Trial data:** EIS sends the deprovision request only; when the data is deleted is the product's decision.
5. **A product update that omits `trialDays` clears the trial** (the update replaces every field); the admin form always sends it.
6. **The trial is on the highest plan by price**, so a product with a free top plan trials that plan.
7. Apply V042 by hand (Flyway is disabled), after V041.
