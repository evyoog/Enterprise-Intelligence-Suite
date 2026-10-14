# Workflow — Purchase to payment

Spans Marketplace (03), Subscription (07), Billing & Payments (08) and Order & Provisioning (09). Feature-level states are in [cart-checkout/workflow.md](../02-requirements/FRD/cart-checkout/workflow.md) and [billing-payments/workflow.md](../02-requirements/FRD/billing-payments/workflow.md). Requirements: [REQ-MKT-003](../02-requirements/FRD/cart-checkout/requirement.md) (cart, [C59](../01-business/roadmap/open-decisions.md#c59), [C100](../01-business/roadmap/open-decisions.md#c100), Draft), [REQ-ORD-001](../02-requirements/FRD/order-lifecycle/requirement.md), [REQ-SUB-003](../02-requirements/FRD/subscription-seats/requirement.md), [REQ-SUB-004](../02-requirements/FRD/renewal-reminders/requirement.md) and [REQ-BIL-001](../02-requirements/FRD/billing-payments/requirement.md).

```mermaid
flowchart TD
    A[Customer clicks Buy on a paid plan] --> A1[Item added to cart; /cart opens]
    A1 --> A3[Optional: one coupon code, not for a purchase request]
    A3 --> A2[Proceed / Submit: purchase validation]
    A2 -- Issues --> A1
    A2 -- OK --> B{Who buys?}
    B -- Individual --> C[Subscriptions created; cart emptied]
    B -- Organization admin or Manage billing --> C2[Direct purchase: order recorded as approved by the buyer; every product provisioned; admins notified if the buyer is not an admin; cart emptied]
    B -- Other organization member --> D[Reason required; ONE multi-item purchase request submitted; admins notified; cart emptied] --> E{Organization admin decides}
    E -- Rejected --> E1[Nothing provisioned]
    C --> F[ONE invoice for the whole cart, coupon applied: OPEN, or Draft if tax is blocked]
    C2 --> F
    E -- Approved --> F2[Every product provisioned; ONE invoice, no coupon] --> F
    F --> CK[Checkout: Billing details, then Payment]
    CK --> P{Payment tile}
    P -- Pay by invoice --> OF[Route OFFLINE; invoice emailed; stays OPEN]
    OF --> OR[Admin records offline payment] --> L
    P -- Card / UPI / Netbanking / Wallets --> G{Gateway configured?}
    G -- No --> H[Online tiles disabled; banner shown]
    G -- Yes --> I[Customer pays in Razorpay Checkout]
    I --> J{Signature verified?}
    J -- No --> K[Nothing recorded; error shown]
    J -- Yes, captured --> L[Payment CAPTURED; invoice PAID; receipt available]
    J -- Failed or cancelled --> M[Invoice stays OPEN; retry allowed]
    L --> N[Webhook confirms; event processed once]
    O[Renewal job] --> F
    S[Seat increase during a term] --> S1[Own invoice: added seats x price x remaining days / term days]
    S1 --> CK
```

An organization's price is **per seat**: the invoice line is price x seats ([REQ-SUB-003.7](../02-requirements/FRD/subscription-seats/requirement.md)). A seat decrease is stored and applied by the next renewal, which then bills the lower number; it creates no invoice and no credit.

## When the term ends ([C100](../01-business/roadmap/open-decisions.md#c100) RR-1 B, RR-4 A)
```mermaid
flowchart TD
    R[Term end reached] --> AR{Auto-renew on?}
    AR -- Yes --> RN[Renewal job: pending plan and seat changes applied; term extended; renewal invoice; reminders for the old date stop]
    AR -- No --> GR[Expiry job: grace end date = term end + 7 days; subscription stays ACTIVE, READ-ONLY; entitlement check says READ_ONLY]
    GR --> RM[Daily grace reminders to the recipients until the end of the local grace day]
    GR -- Auto-renew turned on, or renewed --> RN
    GR -- Grace end date passed --> EX[EXPIRED: no entitlement]
    RN --> F[Invoice OPEN] --> CK2[Customer pays as above]
```
Recipients of the reminders for an organization subscription: its administrators, its Manage billing holders and the people an administrator added ([REQ-SUB-004.18](../02-requirements/FRD/renewal-reminders/requirement.md)).

## Open points
- **Activation and payment — decided ([C97](../01-business/roadmap/open-decisions.md#c97) BI-1 C, built 2026-10-13).** A card, UPI, netbanking or wallet purchase from the cart stays PENDING_SUBSCRIPTION until its invoice is PAID, then becomes ACTIVE. Pay by invoice (only for organizations the platform administrator enables, BI-4) and an approved organization order start at once, with the invoice's due date. A tax-blocked Draft invoice cannot be paid, so its purchase stays pending. Activation happens once per subscription. The diagram above shows the invoice and payment steps; the activation points are drawn in [billing-payments/workflow.md](../02-requirements/FRD/billing-payments/workflow.md#when-a-purchase-starts-c97-bi-1). An unpaid OPEN invoice also gets reminders 3 days before, on and 7 days after its due date (BI-2).
- **Several products in one cart — decided ([C100](../01-business/roadmap/open-decisions.md#c100) CA-1 B, built 2026-10-14).** One cart is paid with **one invoice** (one line per product). An organization member's cart becomes **one** multi-item purchase request with a reason; an organization administrator or a Manage billing holder buys directly with no request (the administrators are told when the buyer is not one). A coupon code (one per cart; tax on the discounted amounts) applies to the invoice of a direct or individual purchase, never to a request. See [cart-checkout/workflow.md](../02-requirements/FRD/cart-checkout/workflow.md).
- **Provisioning** ([C56](../01-business/roadmap/open-decisions.md#c56), [REQ-ORD-002](../02-requirements/FRD/provisioning-contract/requirement.md), Draft): once built, subscription start (and suspension, resumption, cancellation) also notifies the hosted product, which reports the tenant back as a service instance ([C57](../01-business/roadmap/open-decisions.md#c57)). Not drawn until the contract is approved.
