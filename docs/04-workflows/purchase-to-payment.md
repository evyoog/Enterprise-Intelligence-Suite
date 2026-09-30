# Workflow — Purchase to payment

Spans Marketplace (03), Subscription (07), Billing & Payments (08) and Order & Provisioning (09). Feature-level states are in [billing-payments/workflow.md](../02-requirements/FRD/billing-payments/workflow.md). Requirement: [REQ-BIL-001](../02-requirements/FRD/billing-payments/requirement.md).

```mermaid
flowchart TD
    A[Customer chooses a plan] --> B{Who buys?}
    B -- Individual --> C[Subscribe]
    B -- Organization --> D[Submit order] --> E[Organization admin approves]
    C --> F[Invoice generated and finalized: OPEN]
    E --> F
    F --> G{Gateway configured?}
    G -- No --> H[Invoice stays OPEN; Pay disabled; banner shown]
    G -- Yes --> I[Customer pays in Razorpay Checkout]
    I --> J{Signature verified?}
    J -- No --> K[Nothing recorded; error shown]
    J -- Yes, captured --> L[Payment CAPTURED; invoice PAID; receipt available]
    J -- Failed or cancelled --> M[Invoice stays OPEN; retry allowed]
    L --> N[Webhook confirms; event processed once]
    O[Renewal job] --> F
```

## Open point: activation and payment
**Not specified** (FRD Open question 3). Today a subscription becomes active on subscribe or on order approval, before any payment. The product owner must decide whether activation should wait for the first payment. The diagram shows today's order; activation is not drawn as depending on payment.
