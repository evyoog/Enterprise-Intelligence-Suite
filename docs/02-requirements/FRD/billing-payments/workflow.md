# Workflow — Billing & Payments (REQ-BIL-001)

The end-to-end flow across subscriptions, orders and billing is in [docs/04-workflows/purchase-to-payment.md](../../../04-workflows/purchase-to-payment.md). This file covers the states inside this feature.

## Invoice states
```mermaid
stateDiagram-v2
    [*] --> DRAFT: Tax could not be determined (C96): no number, not payable, hidden from the customer
    DRAFT --> OPEN: Retry tax issues it (number and due date set)
    [*] --> OPEN: Generated and finalized (subscription start / renewal / approved order)
    OPEN --> PAID: Payment verified (checkout signature or webhook)
    OPEN --> OPEN: Payment failed or cancelled (retry allowed)
    OPEN --> VOID: Not specified who can void (Open question)
    PAID --> PARTIALLY_REFUNDED: Partial refund processed
    PAID --> REFUNDED: Full refund processed
    PARTIALLY_REFUNDED --> REFUNDED: Remaining amount refunded
```
Payment reminders (BR-17) go out while an invoice is OPEN; they stop at PAID or REFUNDED.

`VOID` is listed because an unpaid invoice must be cancellable in some way; who may void and when is **Not specified**.

## When a purchase starts ([C97](../../../01-business/roadmap/open-decisions.md#c97) BI-1)
```mermaid
flowchart TD
    A[Cart purchase of a paid plan] --> B[Subscription PENDING_SUBSCRIPTION<br/>invoice issued OPEN, or Draft if tax is blocked]
    A2[Free plan, or product already active] --> ACT
    A3[Approved organization order] --> ACT
    B --> C{Customer's route}
    C -- Card / UPI / Netbanking / Wallet --> D[Payment captured] --> E[Invoice PAID: markPaid]
    C -- Pay by invoice<br/>enabled organization only --> F[Route OFFLINE, invoice stays OPEN<br/>due date shown] --> G[Activation event PAY_BY_INVOICE]
    E --> H[Activation event PAID]
    G --> ACT[Subscription ACTIVE, term starts]
    H --> ACT
    F --> I[Admin records the bank payment] --> E
    B -. Draft: cannot be paid .-> B
```
A second activation event for a subscription that is already ACTIVE does nothing (BR-14).

## Invoice with a coupon ([C100](../../../01-business/roadmap/open-decisions.md#c100), REQ-BIL-001.35)
```mermaid
flowchart TD
    A[Invoice creation for a cart or an approved/direct organization purchase] --> B{Coupon code on the cart?}
    B -- No --> D[Lines at full amount]
    B -- Yes --> C{Check again: exists, enabled, not expired,<br/>currency, worth less than the subtotal}
    C -- No --> X[Refused with the reason; nothing created]
    C -- Yes --> E[Discount shared across the lines;<br/>redeemedCount + 1]
    E --> D2[Lines carry their discount; subtotal = gross − discount]
    D --> T[Tax on the line amounts minus their discount share]
    D2 --> T
    T --> F{Tax calculated?}
    F -- Yes --> O[Invoice OPEN: total = subtotal + tax]
    F -- No --> DR[Invoice DRAFT: total = subtotal; the coupon stays counted]
```
A seat-increase invoice and a renewal invoice never take a coupon.

## Payment states
```mermaid
stateDiagram-v2
    [*] --> CREATED: Razorpay order created for an invoice
    CREATED --> CAPTURED: Signature verified / payment.captured webhook
    CREATED --> FAILED: payment.failed webhook or checkout failure
    CAPTURED --> PARTIALLY_REFUNDED: refund.processed (partial)
    CAPTURED --> REFUNDED: refund.processed (full)
    PARTIALLY_REFUNDED --> REFUNDED: refund.processed (remaining)
```
Authorize and capture happen together (Razorpay auto-capture). Separate authorize-then-capture is Not specified.

## Payment method states
`ACTIVE` (saved token usable) → `REMOVED` (customer removed it; token deleted at Razorpay). Card expiry is shown; expired cards are marked **Expired** and cannot be set as default.

## Actors
| Step | Actor |
|---|---|
| Edit billing details, pay, add/remove payment methods | Customer, or an organization member with Manage billing (BR-19) |
| Add a purchase-order number | The invoice's owner, until it is paid (BR-22) |
| Refund, reconcile, view all, choose the active gateway, enable Pay by invoice for an organization, create, edit, disable and delete coupons | Platform admin (`MANAGE_BILLING`) |
| Send payment reminders | The invoice reminder job (every 30 minutes), BR-17 |
| Payment confirmation | Razorpay (checkout result + webhook) |
