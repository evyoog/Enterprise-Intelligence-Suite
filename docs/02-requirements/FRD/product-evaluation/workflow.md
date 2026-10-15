# Workflow — Product evaluation (REQ-MKT-005)

## From exploring to buying
```mermaid
flowchart TD
    A[Customer opens a product page<br/>or the compare page] --> B{How to evaluate?}
    B -- Explore --> C[Read the product, compare with others<br/>GET /products/compare]
    B -- Request a demo --> D[POST /me/demo-requests<br/>support ticket, category Demo request<br/>SLA deadlines set]
    B -- Watch tutorials --> E[The product's videos]
    B -- Start free trial --> F{Product offers a trial,<br/>published, audience fits,<br/>no subscription ever?}
    F -- No --> X[Refused 400 / 404 / 409]
    F -- Yes --> G{Individual or organization?}
    G -- Individual --> H[POST /me/trials]
    G -- Organization --> I{Administrator or<br/>Manage billing?}
    I -- No --> Y[Refused 403]
    I -- Yes --> J[POST /organization/me/trials]
    H --> K[Trial subscription<br/>ACTIVE, highest plan, trial = true<br/>ends after trialDays, auto-renew off]
    J --> K
    K --> L[Service instance PROVISIONING<br/>SubscriptionCreated event with trial: true]
    L --> M[The product creates the trial tenant<br/>and its database, all features]
    M --> N{During the trial}
    N -- 3 days left --> O[Notice, once]
    O --> N
    N -- Customer subscribes --> P[Cart and checkout<br/>trial = false, normal subscription<br/>a paid plan starts after payment]
    N -- Days are over --> Q[Hourly job: EXPIRED at once, no grace<br/>TRIAL_EXPIRED, SubscriptionChanged EXPIRED<br/>instance DEPROVISIONED<br/>the product removes the tenant]
    Q --> P
```
Buying while the trial is still running is refused by the cart today (`ALREADY_SUBSCRIBED`, [BR-EVL-016](business-rules.md)); after the trial has ended it works. The service-level purchase turns a running trial into a normal subscription ([BR-EVL-015](business-rules.md)).

## Starting a trial
```mermaid
sequenceDiagram
    autonumber
    participant C as Customer
    participant API as EvaluationController
    participant T as TrialService
    participant S as SubscriptionService
    participant O as Outbox and service instance
    participant P as Hosted product

    C->>API: POST /me/trials { productId }
    API->>T: startForCustomer
    T->>S: startTrial
    S->>S: product ACTIVE with trialDays, audience fits, no subscription ever
    S->>S: save subscription (ACTIVE, trial, highest plan, expiresAt = now + trialDays)
    S->>C: notice "Free trial started"
    S->>S: audit TRIAL_STARTED
    S->>O: SubscriptionCreated { trial: true } and service instance PROVISIONING
    O->>P: provision requested (REQ-ORD-002)
    P->>P: create the tenant and its database, all features
    S-->>C: SubscriptionDto (trial: true, expiresAt)
```
For an organization the call is `POST /organization/me/trials`; `TrialService#startForOrganization` first checks the caller is an administrator or holds Manage billing, then `SubscriptionService#startOrganizationTrial` creates the trial with the organization's seats and refreshes member product access. No notice is sent for an organization-owned subscription.

## State of a trial subscription
```mermaid
stateDiagram-v2
    [*] --> ACTIVE_TRIAL: start trial (BR-EVL-006)
    ACTIVE_TRIAL --> EXPIRED: days over, hourly job, no grace (BR-EVL-013)
    ACTIVE_TRIAL --> SUSPENDED: suspend (as any subscription)
    SUSPENDED --> ACTIVE_TRIAL: reactivate
    ACTIVE_TRIAL --> CANCELLED: cancel (one-way)
    ACTIVE_TRIAL --> PENDING_SUBSCRIPTION: buy a paid plan (trial = false, BR-EVL-015)
    ACTIVE_TRIAL --> ACTIVE: buy a free plan (trial = false)
    EXPIRED --> PENDING_SUBSCRIPTION: buy a paid plan after the trial
    PENDING_SUBSCRIPTION --> ACTIVE: payment
```
`ACTIVE_TRIAL` is `ACTIVE` with `trial = true`. A trial has no grace period, so it never shows the read-only state of [REQ-SUB-004](../renewal-reminders/requirement.md). It cannot be renewed, set to auto-renew or moved to another plan ([BR-EVL-009](business-rules.md)).

## Compare
```mermaid
sequenceDiagram
    autonumber
    participant V as Visitor or customer
    participant API as GET /products/compare
    participant C as CompareService
    participant X as Tax rules

    V->>API: items=7:21,9&seats=3 (token optional)
    API->>C: compare
    C->>C: parse 2 to 4 items, seats 1 to 100000
    loop each item
        C->>C: product ACTIVE, plan of the product or entry plan or flat price
        C->>C: subtotal = unit price x seats, in the item's currency
        alt not signed in
            C-->>C: tax NOT_SIGNED_IN
        else no billing details
            C-->>C: tax NEEDS_BILLING_DETAILS
        else external tax provider on, or the rules cannot tax the sale
            C-->>C: tax AT_CHECKOUT
        else
            C->>X: preview(subtotal, currency, country, state, tax class, customer type)
            X-->>C: tax
            C-->>C: tax ESTIMATED, total = subtotal + tax
        end
    end
    C-->>V: seats and items
```

## Demo request
```mermaid
sequenceDiagram
    autonumber
    participant C as Customer
    participant D as DemoRequestService
    participant T as SupportTicketService
    participant A as Support team

    C->>D: POST /me/demo-requests { productId, preferredTime, message }
    D->>D: product ACTIVE, text trimmed and at most 1000 characters
    D->>T: createTicket(subject, description, category "Demo request")
    T->>T: OPEN, MEDIUM, SLA deadlines set, TICKET_CREATED audited
    T-->>C: the ticket
    A->>T: works the ticket (REQ-SUP-001, REQ-SUP-002)
```

## Transitions
| From | To | Actor | Condition / rule | Side effects |
|---|---|---|---|---|
| (no subscription) | trial ACTIVE | Customer, or organization administrator / Manage billing | BR-EVL-003 to BR-EVL-007 | Notice (individual); `TRIAL_STARTED`; `SubscriptionCreated` with `trial: true`; service instance PROVISIONING; member access refreshed (organization) |
| trial ACTIVE | (reminded) | System, hourly | Ends within 3 days, not reminded; BR-EVL-011 | Notice once; `trial_reminded_at` set |
| trial ACTIVE | EXPIRED | System, hourly | `expiresAt` passed; BR-EVL-013 | Notice (individual); `TRIAL_EXPIRED`; `SubscriptionChanged` EXPIRED; instance DEPROVISIONED; member access refreshed (organization) |
| trial ACTIVE or EXPIRED | normal subscription | Customer buys | BR-EVL-015 (cart: BR-EVL-016) | `trial` = false; plan, renewal date and auto-renew of the purchase; paid plan PENDING_SUBSCRIPTION until paid |
| (any) | demo ticket OPEN | Customer | BR-EVL-018 | Ticket in category "Demo request" with SLA deadlines; `TICKET_CREATED` |
| (any) | (nothing) | Anyone | Compare, BR-EVL-021 | Read-only |
