# Workflow — Tax Rules and Calculation (REQ-BIL-002)

## Calculation decision flow
Runs once per invoice, inside `InvoiceService` when an invoice or a credit note is generated.

```mermaid
flowchart TD
    A[Invoice generation starts] --> B[Read tax settings once<br/>engine + configuration version pinned]
    B --> C{External Tax Engine switch}
    C -- Off --> D[Internal engine]
    C -- On --> P[External engine]

    D --> D1{Customer country<br/>and seller country known?}
    D1 -- No --> X
    D1 -- Yes --> D2{Approved exemption<br/>in force?}
    D2 -- Yes --> R[Result: Exempt with reason]
    D2 -- No --> D3[Work out supply type<br/>and place of supply]
    D3 --> D4[Find approved, active, in-force rules<br/>that match each line]
    D4 --> D5{Matches}
    D5 -- None --> X
    D5 -- Two equal --> X
    D5 -- One best --> D6[Components x rates per line<br/>rounded per component]
    D6 --> R2[Result: tax lines]

    P --> P1{Provider configured,<br/>validated, supports country,<br/>currency, type?}
    P1 -- No --> X
    P1 -- Yes --> P2[Read supplier, registrations,<br/>customer, tax class, amounts]
    P2 --> P3[Send with idempotency key]
    P3 --> P4{Transient failure?}
    P4 -- Yes, attempts left --> P3
    P4 -- Yes, none left --> X
    P4 -- No --> P5{Response valid?}
    P5 -- No --> X
    P5 -- Yes --> R2

    R --> F
    R2 --> F[Finalize invoice:<br/>number, issue and due date,<br/>tax lines, event]
    X[Blocked: invoice stays Draft<br/>with block code and message,<br/>audit entry] --> Y[Admin fixes the cause<br/>and presses Retry tax]
    Y --> B
```

## Tax rule states
```mermaid
stateDiagram-v2
    [*] --> Draft: Created or new version
    Draft --> PendingApproval: Submit
    Rejected --> PendingApproval: Submit again
    PendingApproval --> Approved: Approve (another administrator)
    PendingApproval --> Rejected: Reject with reason
    Approved --> Approved: Activate / deactivate
    Approved --> Approved: Superseded (end date set)
```

## Switching the engine
```mermaid
flowchart LR
    S1[Off to On] --> V{Provider selected,<br/>credentials present,<br/>connection and capabilities pass?}
    V -- Yes --> ON[On: future calculations use the provider]
    V -- No --> KEEP[Setting unchanged;<br/>failed checks listed]
    S2[On to Off] --> OFF[Off: future calculations use internal rules<br/>warn if no rule is in force]
```
Issued invoices are never touched by either change.

## Actors
| Step | Actor |
|---|---|
| Switch engine, manage classes, registrations, provider settings | Platform admin (`MANAGE_BILLING`) |
| Create, edit, submit a rule or exemption | Platform admin |
| Approve or reject | A different platform admin (or the same one when the setting allows it) |
| Retry tax on a Draft invoice | Platform admin |
| Calculate tax | `InvoiceService` (system) through `TaxCalculationService` |
