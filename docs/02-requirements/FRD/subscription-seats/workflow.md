# Workflow — Seats and quantity (REQ-SUB-003)

```mermaid
flowchart TD
    A[Manage billing user opens My subscriptions → Organization subscriptions] --> B[Seats: in use of quantity; price per seat; renewal date]
    B --> C[Change quantity, Save]
    C --> D{New quantity valid?<br/>1–100000, not below seats in use,<br/>term not ended}
    D -- No --> F[Refused with the reason]
    D -- Yes --> E{Compared with the current quantity}
    E -- Same, decrease pending --> W[Pending decrease withdrawn]
    E -- Same, nothing pending --> U[Unchanged]
    E -- Higher --> G[Quantity saved now; org licensed seats raised if needed]
    G --> G1[Own invoice: added seats × price × remaining days ÷ term days, with tax]
    G1 --> H[Audit + SeatsChanged event; members left without a seat get their product]
    E -- Lower --> L{Renewal date?}
    L -- Yes --> P[pendingQuantity stored; no invoice, no credit; audit]
    L -- No --> N[Quantity saved now]
    P --> R[Renewal: quantity = max of pending, seats in use, 1]
    R --> R1[Renewal invoice bills the new quantity × price; audit + SeatsChanged]
    P -. Withdraw .-> W
```

## States of a subscription's seats
| State | Meaning |
|---|---|
| Quantity only | No change scheduled |
| Quantity and pending quantity | A decrease will apply at the renewal date; the screen shows "Falls to {n} seats on {date}. Nothing is credited." and **Withdraw** |
