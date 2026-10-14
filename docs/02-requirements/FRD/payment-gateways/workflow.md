# Workflow — Payment gateways (REQ-BIL-003)

Rule numbers (BR-n) refer to [business-rules.md](business-rules.md).

## Gateway set-up: configure → test → activate
Configured, Connected, Active and Default are four separate facts (BR-6). The gateway moves through them independently:

```mermaid
stateDiagram-v2
    [*] --> NotConfigured: provider has an adapter
    [*] --> Planned: provider has no adapter (BR-1)
    Planned --> Planned: nothing can be done, only read
    NotConfigured --> Incomplete: some required fields saved
    NotConfigured --> Configured: all required fields saved
    Incomplete --> Configured: remaining fields saved
    Configured --> Configured: save again (version +1, Connected becomes Not tested)
    Configured --> Tested: Test connection passes
    Configured --> TestFailed: Test connection fails (real status shown)
    TestFailed --> Configured: fix credentials and save
    Tested --> Active: Activate (a fresh test passes inside the request)
    Configured --> Active: Activate (a fresh test passes inside the request)
    Active --> Default: Make default (first activated gateway becomes default)
    Active --> Active: rotate credentials (tested first, old kept on failure, BR-9)
    Default --> Active: another gateway made default
    Active --> Configured: Deactivate (also clears Default)
    Default --> Configured: Deactivate
```

| Step | Actor | Rule | What happens |
|---|---|---|---|
| Configure | Administrator (`MANAGE_BILLING`) | BR-3, BR-4, BR-5, BR-14 | Enters the environment and the credentials the provider's field list asks for; a blank field keeps the stored value. Refused if a value is entered and the server has no encryption key. Version +1. |
| Test connection | Administrator | BR-7 | Real call to the provider; the outcome and the configuration version tested are stored. No call when the configuration is empty or incomplete. |
| Activate | Administrator | BR-8 | Needs a complete configuration; tests again inside the request; only a passing test activates. |
| Make default | Administrator | BR-8 | Only an active gateway; the previous default is cleared. |
| Deactivate | Administrator | BR-8 | Always allowed; clears Default; existing payments are untouched. |
| Switch off a payment method | Administrator | BR-15 | At least one method stays on. |
| Rotate | Administrator | BR-9 | Active gateway: new credentials tested first; on failure nothing is saved. |

### Saving an active gateway (rotation)
```mermaid
sequenceDiagram
    participant A as Administrator
    participant UI as Configure dialog
    participant S as GatewayManagementService
    participant P as Provider
    A->>UI: type the new secret, Save
    UI->>S: PUT /admin/billing/gateways/{code}/configuration (only typed fields)
    S->>S: key present? fields valid? environment valid? (400 / 409)
    S->>S: begin transaction, merge with stored values, save, version + 1
    S->>P: test connection with the new configuration
    alt test passes
        P-->>S: ok
        S->>S: commit configuration and passing result together
        S-->>UI: details (200); audit PAYMENT_GATEWAY_CONFIGURED
    else test fails
        P-->>S: refused or unavailable
        S->>S: roll back, old credentials and version stay
        S-->>UI: 409 "Not saved: ... The previous configuration is unchanged."
        Note over S: audit PAYMENT_GATEWAY_CONFIGURATION_REJECTED, no values
    end
```

## Payment routing
The browser never chooses a gateway (BR-10). A payment keeps the provider that created it (BR-11).

```mermaid
flowchart TD
    A[Customer chooses Pay on an OPEN invoice, optionally a method tile] --> B[POST /me/billing/invoices/id/payments]
    B --> C{Invoice OPEN?}
    C -- No --> X1[409 INVALID_STATE]
    C -- Yes --> D[Eligible gateways: implemented, active, fully configured,<br/>supports the invoice currency, method switched on]
    D --> E{Any eligible?}
    E -- No --> X2[503 PAYMENT_GATEWAY_NOT_CONFIGURED<br/>No payment gateway is available for currency method payments<br/>nothing is created or charged]
    E -- Yes --> F[Order the eligible ones: default first, then by provider code]
    F --> G[Take the first one]
    G --> H[Adapter creates the provider order]
    H --> I[Save the payment with provider and provider order id]
    I --> J[Return paymentId, providerOrderId, amount, currency, keyId, provider, clientConfig]
    J --> K{Does the checkout screen have a launcher for that provider?}
    K -- No --> X3[Error shown, no payment window opens, nothing is charged]
    K -- Yes --> L[Provider's payment window opens in the browser]
    L --> M[POST /me/billing/payments/id/confirm with the provider's result]
    M --> N[Payment's own provider verifies the signature, fetches the payment]
    N --> O{Captured?}
    O -- Yes --> P[Payment CAPTURED, invoice PAID, purchase starts]
    O -- No --> Q[Payment FAILED, invoice stays OPEN]
```

After creation, confirm, reconcile and refund use `payment.provider`; deactivating that gateway or changing the default does not move the payment to another provider. There is no fail-over: if the chosen provider errors, the customer sees the error and may try again (the next attempt is routed afresh).

## Webhook handling
```mermaid
flowchart TD
    A[Provider calls POST /webhooks/provider with the raw body] --> B{Provider known and implemented?}
    B -- No --> X1[404, not counted]
    B -- Yes --> C{Credentials complete?}
    C -- No --> F1[Count failure: gateway is not configured<br/>400 SIGNATURE_INVALID]
    C -- Yes --> D{Adapter verifies the signature on the raw body?}
    D -- No --> F2[Count failure: signature did not verify<br/>400 SIGNATURE_INVALID, nothing changed]
    D -- Yes --> E{Adapter understands the body?}
    E -- No --> F3[Count failure: body not understood<br/>400 SIGNATURE_INVALID]
    E -- Yes --> G{Event id already seen for this provider?}
    G -- Yes --> H[200, acknowledged, nothing reprocessed]
    G -- No --> I{Event type}
    I -- payment.captured --> J[Find payment by provider + order id<br/>mark CAPTURED, invoice PAID, once]
    I -- payment.failed --> K[Find payment by provider + order id<br/>mark FAILED unless already CAPTURED]
    I -- refund.processed --> L[Record only, link the payment]
    I -- other --> M[Record and ignore]
    J --> N[Save the event row, set last event and Verified on the gateway]
    K --> N
    L --> N
    M --> N
    N --> O[200]
```

A deactivated provider still passes step C and D as long as its credentials are complete, so payments already in flight finish (BR-13). Failures update one counter and the latest reason on the gateway row; they never add a row per attempt.

## Provider failure handling
| Situation | What EIS records | What the user sees |
|---|---|---|
| Provider unreachable or 5xx during a test | Connection status `PROVIDER_UNAVAILABLE` | "Provider unavailable", not "Authentication failed" |
| Provider rejects the credentials (401/403) | `AUTHENTICATION_FAILED` | Gateway stays inactive (or, if active, a rotation is not saved) |
| Order creation fails at checkout | Nothing: no payment row is saved for a failed order creation (the request is rolled back) | The provider's error as 502 `PAYMENT_GATEWAY_ERROR` (Razorpay); nothing is charged |
| Forged or unsigned webhook | Failure counter + reason only | Details show "n rejected webhook(s)" and the last reason |
| Duplicate webhook | Nothing | Nothing |

## Roles
| Step | Actor |
|---|---|
| Configure, test, activate, deactivate, default, methods | Platform administrator with `MANAGE_BILLING` |
| Pay an invoice | Customer, or organization billing user |
| Webhook | The payment provider, authenticated only by its signature |
