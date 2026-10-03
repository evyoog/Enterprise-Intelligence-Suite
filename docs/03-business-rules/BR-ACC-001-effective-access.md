# BR-ACC-001 — Effective access

| Field | Value |
|---|---|
| Status | Draft (with [REQ-TEN-005](../02-requirements/FRD/access-management/requirement.md)) |
| Decision | [C65](../01-business/roadmap/open-decisions.md#c65) |
| Applies to | Every organization-scope permission check and every product-access check |

## Rule
1. A member's **effective access** is, for each organization feature permission and each entitled product: the member's **individual override** if one exists (granted ON or OFF), otherwise the **role default** of the member's organization role.
2. An **Organization admin** always has every organization feature permission (locked ON).
3. Product access exists only for products in the organization's ACTIVE subscriptions ([C52](../01-business/roadmap/open-decisions.md#c52)).
4. An active, approved organization **privileged-access grant** (REQ-IAM-004) also satisfies a permission check for its duration, as today.
5. Every feature that checks an organization permission uses effective access — never the role alone. Features that do so today: member administration, product access, privileged-access approval, orders, organization settings and sign-in, business dashboard, organization audit log, organization billing, organization subscriptions and seats.

## Why
One definition, so that a permission granted or removed on the Access management screen means the same thing in every feature.
