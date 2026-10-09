# BR-INT-001 — Single writer and tenant scope

| Field | Value |
|---|---|
| Status | In Review (with [REQ-INT-003](../02-requirements/FRD/platform-tool-sync/requirement.md)) |
| Decision | [C86](../01-business/roadmap/open-decisions.md#c86) |
| Applies to | Every product that holds a copy of platform data; every message between the platform and a product |

## Rule
1. **Single writer.** The platform is the only writer of organizations, hierarchy, users, memberships, subscriptions and product access. A product changes such a field only by calling the platform and applying the platform's answer. Fields that only the product uses stay with the product.
2. **Same identifiers.** A product keeps the platform's identifiers (and, for people, the Keycloak user id) as references, in addition to its own ids.
3. **Tenant scope.** A product resolves the tenant of a request or message from its own registry and the caller's identity. A database name, schema name or connection detail is never taken from a request, a header the caller controls, or a message.
4. **Isolation.** A user of one tenant cannot read or change another tenant's data, whatever host, header or id they supply.
5. **No silent fail-open.** If a product cannot confirm a sensitive action with the platform, it denies the action.

## Why
One owner per field removes conflicts when the same data lives in several databases; a registry-based tenant removes a way for a caller to choose another customer's data.
