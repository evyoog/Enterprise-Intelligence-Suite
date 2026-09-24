# Scope — EIS Platform

## In scope (shared platform layer)
| Area | Capability |
|------|------------|
| Catalog | Platforms, products, plans and pricing; product search and facets |
| Registration & tenancy | Organization and customer registration, email verification, members and org roles, seats |
| Subscriptions | Product subscriptions and per-organization product access |
| Identity | Keycloak SSO, session management, password reset, platform TOTP MFA with recovery codes, internal SSO bridge to product suites |
| Federation | Per-organization SAML identity providers |
| Authorization | RBAC roles and permissions, organization MFA policy, just-in-time privileged access |
| Experience | User and business dashboards, favorites, usage, search history, theme and locale preferences |
| Notifications | In-app notifications and preferences |
| Audit | Platform and organization audit logs |
| AI service | Shared AI capabilities exposed to the platform and suites (`ai-service/`) |

## Out of scope (owned by each product suite)
- Domain features of Valam.ai, Varthan.ai, Thittam.ai, Thiran.ai, Yukth.ai and Tharav.ai.
- Suite-specific data stores and business logic.
