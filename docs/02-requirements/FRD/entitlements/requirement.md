# REQ-SUB-002 — Entitlements

**Status:** Draft
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved. Cannot be approved until the open questions below marked **Blocks approval** are answered.
**Decision:** [C52](../../../01-business/roadmap/open-decisions.md#c52)

| Field | Value |
|---|---|
| Sprint | [2026.4.3](../../../01-business/roadmap/sprints/SPRINT-2026.4.3.md) |
| Requirement ID | REQ-SUB-002 |
| Application | [07 Subscription & Entitlement Management](../../../01-business/roadmap/applications/07-subscription-entitlement-management.md) |
| Application code | `APP-SUB` |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 07.02.01 | Validate entitlement; Check feature access | Yes — derived, read-only check (see Summary) |
| 07.02.01 | Grant entitlement; Revoke entitlement | No — there is nothing to grant or revoke; an entitlement exists exactly as long as its subscription is ACTIVE (see [C52](../../../01-business/roadmap/open-decisions.md#c52)) |
| 07.02.02 | Check quota | Partly — the limit value is shown; counting consumption against it is not built (no metering, [C50](../../../01-business/roadmap/open-decisions.md#c50)) |
| 07.02.02 | Allocate quota; Adjust quota | No — Not covered (see Out of scope) |

## Summary
An entitlement answers "is this feature or product available to this customer or organization, and what are its limits?". Per [C52](../../../01-business/roadmap/open-decisions.md#c52), this is **derived at runtime** from an ACTIVE subscription plus its plan's existing `includedFeatures` and `usageLimit` fields ([REQ-CAT-002](../plan-management/requirement.md)) — there is no separate entitlement table to keep in sync, grant, or revoke. A suspended, cancelled or expired subscription grants nothing. The same derived check is used by the platform's own UI and is exposed to hosted products over the existing internal service-to-service channel.

## Actors
- Individual customer — sees their own subscriptions' entitlements
- Organization member — sees the organization's entitlements, per existing organization permissions
- Hosted product backend (Valam.ai, Varthan.ai, etc.) — calls the internal check
- Platform UI (My products / organization subscriptions) — calls the customer-facing check

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-SUB-002.1 | Entitlements are derived at runtime from ACTIVE subscriptions and their plans' `includedFeatures` and `usageLimit` fields. No entitlement table is created. | Must |
| REQ-SUB-002.2 | A customer can see, per active subscription: product, plan, status, term end (`expiresAt`), included features, and usage limits (limit values only, no consumption). | Must |
| REQ-SUB-002.3 | An organization's entitlements are the union of its organization subscriptions; who may view them follows existing organization permissions. | Must |
| REQ-SUB-002.4 | A check answers "is feature X / product Y available to this customer or organization, and what are its limits?" for the platform UI and for hosted products. | Must |
| REQ-SUB-002.5 | Hosted products call the check through the existing internal service-to-service channel (the internal SSO bridge secret, `X-Internal-Sso-Secret`); exact request/response contract — see Open questions. | Must |
| REQ-SUB-002.6 | A suspended, cancelled or expired subscription grants nothing — the check returns not-allowed for every feature of that subscription's product. | Must |

## Out of scope
- Counting usage against limits (quota consumption/enforcement) — no usage metering exists ([C50](../../../01-business/roadmap/open-decisions.md#c50)).
- Licensing and seat/quantity entitlements (07.03, D14) — not decided.
- An entitlement grant/revoke history or audit trail — there is no store to hold one ([C52](../../../01-business/roadmap/open-decisions.md#c52)); the subscription's own existing audit trail is the only record.
- A structured feature-flag model for `includedFeatures` — it stays free text, per [REQ-CAT-002](../plan-management/requirement.md).

## Dependencies
- [subscription-lifecycle](../subscription-lifecycle/requirement.md) (REQ-SUB-001) — subscription status and `expiresAt`.
- [plan-management](../plan-management/requirement.md) (REQ-CAT-002) — `includedFeatures`, `usageLimit`.
- [order-lifecycle](../order-lifecycle/requirement.md) (REQ-ORD-001) — organization subscriptions.
- Existing internal SSO bridge secret (`INTERNAL_SSO_SHARED_SECRET`, `X-Internal-Sso-Secret` header) for the hosted-product channel.

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement (this file), feature business rules, feature workflow, acceptance criteria | this folder |
| Screens (UI) | [docs/05-ui/screen-requirements/](../../../05-ui/screen-requirements/) — see [ui-requirements.md](ui-requirements.md) |
| API | [docs/06-api/api-requirements/entitlements.md](../../../06-api/api-requirements/entitlements.md) — see [api-requirements.md](api-requirements.md) |

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | **Internal check contract:** the exact request/response shape of `POST /internal/entitlements/check` — Not specified; to confirm with hosted product teams. | Yes |
| 2 | **Admin read access:** should a platform administrator be able to look up any customer's or organization's entitlements (for support purposes)? Assumed yes, read-only, permission to confirm. | No — confirm in review |
| 3 | **Licensing and quantity (D14):** not decided; out of scope here regardless of the answer. | No |
