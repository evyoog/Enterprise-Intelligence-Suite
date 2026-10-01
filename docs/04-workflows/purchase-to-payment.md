# Workflow — Purchase to payment

Spans Marketplace (03), Subscription (07), Billing & Payments (08) and Order & Provisioning (09). Feature-level states are in [cart-checkout/workflow.md](../02-requirements/FRD/cart-checkout/workflow.md) and [billing-payments/workflow.md](../02-requirements/FRD/billing-payments/workflow.md). Requirements: [REQ-MKT-003](../02-requirements/FRD/cart-checkout/requirement.md) (cart, [C59](../01-business/roadmap/open-decisions.md#c59), Draft) and [REQ-BIL-001](../02-requirements/FRD/billing-payments/requirement.md).

```mermaid
flowchart TD
    A[Customer clicks Buy on a paid plan] --> A1[Item added to cart; /cart opens]
    A1 --> A2[Proceed / Submit: purchase validation]
    A2 -- Issues --> A1
    A2 -- OK --> B{Who buys?}
    B -- Individual --> C[Subscriptions created; cart emptied]
    B -- Organization member --> D[Order submitted; cart emptied] --> E{Organization admin decides}
    E -- Rejected --> E1[Nothing provisioned]
    C --> F[Invoice generated and finalized: OPEN]
    E -- Approved --> F
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
```

## Open points
- **Activation and payment — Not specified** (REQ-BIL-001 Open question 3). Today a subscription becomes active on subscribe or on order approval, before any payment. The diagram shows today's order; activation is not drawn as depending on payment.
- **Several products in one cart** (REQ-MKT-003 Open question 1): whether one invoice covers several subscriptions, and how an organization cart becomes orders, is not decided. The diagram does not fix either.
- **Provisioning** ([C56](../01-business/roadmap/open-decisions.md#c56), [REQ-ORD-002](../02-requirements/FRD/provisioning-contract/requirement.md), Draft): once built, subscription start (and suspension, resumption, cancellation) also notifies the hosted product, which reports the tenant back as a service instance ([C57](../01-business/roadmap/open-decisions.md#c57)). Not drawn until the contract is approved.
