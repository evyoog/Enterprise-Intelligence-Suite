# Workflow — Platform events

Requirement: [REQ-INT-002](../02-requirements/FRD/event-platform/requirement.md), decision [C62](../01-business/roadmap/open-decisions.md#c62). Every publisher writes its event in the same transaction as its change; the dispatcher delivers to handlers.

```mermaid
flowchart LR
    subgraph Publishers
      ORD[Order lifecycle REQ-ORD-001] -->|OrderApproved| OB[(outbox_event)]
      SUB[Subscription lifecycle REQ-SUB-001] -->|SubscriptionCreated / Changed / Suspended / Resumed / Cancelled / Renewed| OB
      CART[Cart REQ-MKT-003] -->|CheckoutCompleted| OB
      BIL[Billing REQ-BIL-001] -->|InvoiceGenerated / PaymentAuthorized / PaymentFailed| OB
      SEAT[Seats REQ-SUB-003] -->|SeatsChanged| OB
      REM[Renewal reminders REQ-SUB-004] -->|RenewalReminderSent| OB
    end
    OB --> D[Dispatcher every 5 s]
    D --> H1[Provisioning handler — REQ-ORD-002, not built]
    D --> H2[Webhook handler — D19, not decided]
```

| Event | Publisher (code) | Handlers today |
|---|---|---|
| `OrderApproved` | `OrderService.approveOrder` | None (planned: provisioning) |
| `SubscriptionCreated` | `SubscriptionService.subscribe`, `subscribeOrganization`, `subscribeFromCart` | None (planned: provisioning) |
| `SubscriptionChanged` | `SubscriptionService.changePlan` | None |
| `SubscriptionSuspended` / `SubscriptionResumed` / `SubscriptionCancelled` | `SubscriptionService.suspend/reactivate/cancelSubscription` | None (planned: provisioning) |
| `SubscriptionRenewed` | `SubscriptionService.renewSubscription`, auto-renew job | None |
| `CheckoutCompleted` | `CartService.checkout` | None |
| `InvoiceGenerated` | `InvoiceService.generateForSubscriptions` | None |
| `PaymentAuthorized` / `PaymentFailed` | `PaymentService.confirmPayment` and webhook, `OfflinePaymentService.recordOfflinePayment` | None |
| `SeatsChanged` | `SubscriptionSeatService.changeSeats` | None |
| `RenewalReminderSent` | `RenewalReminderService` | None |

With no handler, an event is marked DELIVERED (REQ-INT-002.3); it stays visible in the admin view until retention removes it.
