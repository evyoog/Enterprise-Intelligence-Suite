# Workflow — API management (REQ-INT-001)

## Key lifecycle
```mermaid
stateDiagram-v2
    [*] --> ACTIVE: organization administrator creates key (full key shown once)
    ACTIVE --> REVOKED: an administrator of the organization revokes
    ACTIVE --> EXPIRED: expiry date passes
    REVOKED --> [*]
    EXPIRED --> [*]
```

## Request
```mermaid
flowchart TD
    A[Request] --> V[/v1 prefix? strip it; add API-Version: 1/]
    V --> D[/Deprecated prefix? add Deprecation, Sunset, Link/]
    D --> K{X-API-Key and no Authorization?}
    K -- Yes --> K2{Key ACTIVE?}
    K2 -- No --> R401[401; audited if revoked]
    K2 -- Yes --> ADM{Organization key: active administrator who can sign in?}
    ADM -- No --> R401[401; API_KEY_NO_ADMINISTRATOR audited]
    ADM -- Yes --> RO{Read-only key and not GET/HEAD/OPTIONS?}
    RO -- Yes --> R403[403 API_KEY_READ_ONLY]
    RO -- No --> AUTH[Act as that administrator; update last used]
    K -- No --> J[Keycloak JWT or anonymous]
    AUTH --> RL{Within rate limit?}
    J --> RL
    RL -- No --> R429[429 + Retry-After]
    RL -- Yes --> E[Endpoint with normal security rules]
```
