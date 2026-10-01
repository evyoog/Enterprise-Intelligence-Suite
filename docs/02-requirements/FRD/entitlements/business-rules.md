# Business rules — Entitlements (REQ-SUB-002)

| ID | Rule |
|---|---|
| BR-1 | An entitlement exists only while its subscription's status is ACTIVE. SUSPENDED, CANCELLED and EXPIRED subscriptions grant nothing, with no separate action needed to "revoke" them — ending the subscription already ends the entitlement ([C52](../../../01-business/roadmap/open-decisions.md#c52)). |
| BR-2 | Included features and usage limits shown are exactly the values on the subscription's current plan — never cached or copied elsewhere, so a plan change takes effect immediately. |
| BR-3 | A usage limit is shown as a value only; nothing in this feature tracks or enforces consumption against it. |
| BR-4 | A customer sees only their own subscriptions' entitlements; an organization's entitlements are visible only to users allowed by the organization's existing permissions. Any other customer's or organization's entitlements are refused with a generic 404 (or 403 for the internal check, see BR-5). |
| BR-5 | The internal entitlements check (`POST /internal/entitlements/check`) is reachable only with a valid `X-Internal-Sso-Secret` header; missing or wrong secret is refused with 403, the same convention as the existing internal SSO bridge (`InternalSsoController`). |
