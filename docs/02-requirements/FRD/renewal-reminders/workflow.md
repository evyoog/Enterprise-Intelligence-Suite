# Workflow — Auto-renewal and renewal reminders (REQ-SUB-004)

```mermaid
flowchart TD
    S[Paid subscription created: auto-renew ON, renewal date = start + period] --> R1
    subgraph Reminders [Every 15 minutes, per recipient]
      R1{Renewal date − N days ≤ today < renewal date, in the recipient's time zone?} -- No --> R0[Nothing]
      R1 -- Yes --> R2{Local time ≥ send time and reminders on?}
      R2 -- No --> R0
      R2 -- Yes --> R3{Already logged for today?}
      R3 -- Yes --> R0
      R3 -- No --> R4[Log, notify on the person's channels, audit, RenewalReminderSent]
    end
    R4 --> R1
    S --> T{Renewal date reached?}
    T -- Yes, auto-renew ON --> U{Can charge automatically?}
    U -- Yes, future: Razorpay token --> V[Charge saved method]
    U -- No, today --> W[Renewal invoice issued: pay online or by invoice]
    V --> X[Term extended, SubscriptionRenewed; pending plan and seat changes applied]
    W --> X
    X --> R1
    X -. renewal date moved forward .-> Stop[Remaining reminders for the old date stop]
    T -- Yes, auto-renew OFF --> G[Hourly job: grace end date = term end + 7 days; stays ACTIVE, read-only]
```

## Grace period (C100 RR-1 B, RR-4 A)
```mermaid
stateDiagram-v2
    [*] --> ACTIVE
    ACTIVE --> ACTIVE_READ_ONLY: term ended, auto-renew OFF (expiry job sets the grace end date, term end + 7 days)
    ACTIVE_READ_ONLY --> ACTIVE: renewed, or auto-renew turned ON (renews at once); grace end date cleared
    ACTIVE_READ_ONLY --> EXPIRED: grace end date passed (expiry job)
    EXPIRED --> ACTIVE: individual Renew
```
`ACTIVE_READ_ONLY` is not a stored status: the subscription is ACTIVE with a grace end date in the future and a term end in the past. The entitlement check answers `allowed` with `accessMode = READ_ONLY` ([REQ-SUB-002.10](../entitlements/requirement.md)).

```mermaid
flowchart TD
    A[Every 15 minutes: subscriptions whose term has ended and that have a grace end date] --> B{Per recipient, in their time zone:<br/>local date between the day the term ended<br/>and the local date of the grace end date?}
    B -- No: before, or after the last local grace day --> Z[Nothing: reminders have stopped]
    B -- Yes --> C{Local time ≥ send time, reminders on, not yet logged today?}
    C -- No --> Z
    C -- Yes --> D[Send NOTIF_RENEWAL_GRACE_REMINDER on the person's channels; log; audit; RenewalReminderSent grace=true]
```
The last reminder is on the **last local grace day**, which can be after the grace instant and after the subscription has become EXPIRED.

## Who is reminded (organization subscription)
```mermaid
flowchart LR
    A[ACTIVE organization admins] --> R[One reminder per person per day]
    B[ACTIVE members with Manage billing] --> R
    C[People an administrator added, still ACTIVE members] -->|only the chosen channels| R
```
An administrator with Manage billing edits the added list on My subscriptions; defaults are shown read-only.
