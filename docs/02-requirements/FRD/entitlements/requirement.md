# REQ-SUB-002 — Entitlements

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-13 ([C97](../../../01-business/roadmap/open-decisions.md#c97), answers EN-1 to EN-5)
**Decision:** [C52](../../../01-business/roadmap/open-decisions.md#c52), [C97](../../../01-business/roadmap/open-decisions.md#c97), [C100](../../../01-business/roadmap/open-decisions.md#c100) (RR-1 B: the read-only grace period adds `accessMode`)
**Built:** 2026-10-13 — see [Where it is built](#where-it-is-built); **`accessMode` and `graceEndsAt` added 2026-10-14** ([C100](../../../01-business/roadmap/open-decisions.md#c100))

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
| 07.02.01 | Validate entitlement; Check feature access | Yes — derived, read-only check (see Summary); hosted products call `POST /internal/entitlements/check` ([C97](../../../01-business/roadmap/open-decisions.md#c97) EN-2) |
| 07.02.01 | Grant entitlement; Revoke entitlement | No — there is nothing to grant or revoke; an entitlement exists exactly as long as its subscription is ACTIVE (see [C52](../../../01-business/roadmap/open-decisions.md#c52)) |
| 07.02.02 | Check quota | Partly — the plan's limit value is shown. EIS stores **only the limit**; each product counts its own usage and enforces it ([C97](../../../01-business/roadmap/open-decisions.md#c97) EN-5) |
| 07.03.01 | Issue license; Validate license; Expire license; Revoke license | **Dropped** — no licence keys; all products are online SaaS ([C97](../../../01-business/roadmap/open-decisions.md#c97) EN-4) |
| 07.02.02 | Allocate quota; Adjust quota | No — Not covered (see Out of scope) |

## Summary
An entitlement answers "is this feature or product available to this customer or organization, and what are its limits?". Per [C52](../../../01-business/roadmap/open-decisions.md#c52), this is **derived at runtime** from an ACTIVE subscription plus its plan's existing `includedFeatures` and `usageLimit` fields ([REQ-CAT-002](../plan-management/requirement.md)) — there is no separate entitlement table to keep in sync, grant, or revoke. A suspended, cancelled or expired subscription grants nothing. A subscription whose term has ended with auto-renew off and that is in its 7-day grace period ([REQ-SUB-004.13](../renewal-reminders/requirement.md)) still answers `allowed`, but **read-only** ([C100](../../../01-business/roadmap/open-decisions.md#c100) RR-1 B). The same derived check is used by the platform's own UI and is exposed to hosted products over the existing internal service-to-service channel.

[C97](../../../01-business/roadmap/open-decisions.md#c97) confirmed this and settled the five questions that were open: entitlements are **read-only and derived** (EN-1); EIS proposes the internal check `POST /internal/entitlements/check` and the product teams adopt it (EN-2); platform administrators can look up any customer's or organization's entitlements, read-only, with the new support permission **View entitlements** (`VIEW_ENTITLEMENTS`) (EN-3); there are **no licence keys** (EN-4); and EIS stores **only the plan's limit** — each product counts usage and enforces the limit (EN-5).

## Actors
- Individual customer — sees their own subscriptions' entitlements
- Organization member — sees their own entitlements (the organization's products they have access to) and may call the organization endpoint
- Platform administrator with `VIEW_ENTITLEMENTS` — read-only support lookup of any customer or organization
- Hosted product backend (Valam.ai, Varthan.ai, etc.) — calls the internal check
- Platform UI (My subscriptions, the admin Entitlements lookup) — calls the customer-facing and admin endpoints

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-SUB-002.1 | Entitlements are derived at runtime from ACTIVE subscriptions and their plans' `includedFeatures` and `usageLimit` fields. No entitlement table is created. | Must |
| REQ-SUB-002.2 | A customer can see, per active subscription: product, plan, status, term end (`expiresAt`), included features, and usage limits (limit values only, no consumption). | Must |
| REQ-SUB-002.3 | An organization's entitlements are the union of its organization subscriptions; the organization endpoint is open to its ACTIVE members (see Defaults applied). | Must |
| REQ-SUB-002.4 | A check answers "is feature X / product Y available to this customer or organization, and what are its limits?" for the platform UI and for hosted products. | Must |
| REQ-SUB-002.5 | Hosted products call the check through the existing internal service-to-service channel (the internal SSO bridge secret, `X-Internal-Sso-Secret`); request and response are in [the API doc](../../../06-api/api-requirements/entitlements.md#internal-endpoint-hosted-products). | Must |
| REQ-SUB-002.6 | A suspended, cancelled or expired subscription grants nothing — the check returns not-allowed for every feature of that subscription's product. | Must |
| REQ-SUB-002.7 | A platform administrator with `VIEW_ENTITLEMENTS` can look up any customer's or any organization's entitlements, read-only. Every lookup is written to the audit log (`ENTITLEMENTS_VIEWED`). ([C97](../../../01-business/roadmap/open-decisions.md#c97) EN-3) | Must |
| REQ-SUB-002.8 | Only the plan's `usageLimit` is stored and exposed. Entitlements never carry consumption; each hosted product counts and enforces usage itself. ([C97](../../../01-business/roadmap/open-decisions.md#c97) EN-5) | Must |
| REQ-SUB-002.9 | For an organization member, a product is available only when the organization has an ACTIVE subscription **and** the member has ACTIVE access to that product (the existing per-member product access). The check says `NO_MEMBER_ACCESS` when the subscription exists but the member lacks access. | Must |
| REQ-SUB-002.10 | **Access mode** ([C100](../../../01-business/roadmap/open-decisions.md#c100) RR-1 B). Every entitlement and every internal check that is `allowed` carries `accessMode`: **`FULL`**, or **`READ_ONLY`** while the subscription's term has ended with auto-renew off and its grace period (7 days) is still running, together with `graceEndsAt`, the end of the grace period. A hosted product that receives `READ_ONLY` must **not allow changes** and should show the customer that renewing restores full access. When the grace period ends the subscription is EXPIRED and the check answers `NO_SUBSCRIPTION`. A check that is not allowed has no `accessMode`. The customer's My subscriptions screen marks a read-only entitlement "Read-only until {date}". | Must |

## Out of scope
- Counting usage against limits (quota consumption/enforcement) — each product does that itself; EIS keeps no usage data ([C97](../../../01-business/roadmap/open-decisions.md#c97) EN-5, [C50](../../../01-business/roadmap/open-decisions.md#c50)).
- Licence keys and licence lifecycle (07.03.01) — dropped ([C97](../../../01-business/roadmap/open-decisions.md#c97) EN-4). Seat quantity is a separate feature ([REQ-SUB-003](../subscription-seats/requirement.md)).
- Granting, revoking or editing an entitlement by hand — entitlements are read-only and derived ([C97](../../../01-business/roadmap/open-decisions.md#c97) EN-1).
- An entitlement grant/revoke history or audit trail — there is no store to hold one ([C52](../../../01-business/roadmap/open-decisions.md#c52)); the subscription's own existing audit trail is the only record.
- A structured feature-flag model for `includedFeatures` — it stays free text, per [REQ-CAT-002](../plan-management/requirement.md).

## Dependencies
- [subscription-lifecycle](../subscription-lifecycle/requirement.md) (REQ-SUB-001) — subscription status and `expiresAt`.
- [plan-management](../plan-management/requirement.md) (REQ-CAT-002) — `includedFeatures`, `usageLimit`.
- [order-lifecycle](../order-lifecycle/requirement.md) (REQ-ORD-001) — organization subscriptions.
- Existing internal SSO bridge secret (`INTERNAL_SSO_SHARED_SECRET`, `X-Internal-Sso-Secret` header) for the hosted-product channel.
- [access-management](../access-management/requirement.md) — the per-member product access and the new platform permission `VIEW_ENTITLEMENTS`.

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement (this file), feature business rules, feature workflow, acceptance criteria | this folder |
| Screens (UI) | [docs/05-ui/screen-requirements/](../../../05-ui/screen-requirements/) — see [ui-requirements.md](ui-requirements.md) |
| API | [docs/06-api/api-requirements/entitlements.md](../../../06-api/api-requirements/entitlements.md) — see [api-requirements.md](api-requirements.md) |

## Where it is built
| Part | Location |
|---|---|
| Service (derivation and the internal check) | `backend/src/main/java/com/vyoog/eisplatform/modules/registration/service/EntitlementService.java` |
| Controllers | `…/registration/controller/EntitlementController.java` (customer, organization, admin), `…/registration/controller/InternalEntitlementController.java` (hosted products) |
| DTOs | `…/registration/dto/EntitlementDto.java`, `EntitlementCheckRequest.java`, `EntitlementCheckResponse.java` |
| Permission | `VIEW_ENTITLEMENTS` (platform), enforced in `SecurityConfig` for `/admin/entitlements/**`; seeded in `RbacSeeder` |
| Frontend | `frontend/src/components/subscriptions/EntitlementsSection.tsx` (My subscriptions), `frontend/src/pages/admin/AdminEntitlementsPage.tsx` (route `/admin/entitlements`) |
| Tests | `EntitlementServiceTest`, `EntitlementsAuthorizationTest`, `SeatsGraceAndRecipientsTest` (backend); `AdminEntitlementsPage.test.tsx`, `MySubscriptionsPage.test.tsx` (frontend). Cases: [test-cases/functional/entitlements/](../../../../test-cases/functional/entitlements/) |

## Open questions
All answered by [C97](../../../01-business/roadmap/open-decisions.md#c97) (product owner, 2026-10-13). None remains open.

| # | Question | Answer |
|---|---|---|
| 1 | ~~**Internal check contract:** the exact request/response shape of `POST /internal/entitlements/check`.~~ | **Answered — EN-2 A.** EIS proposes the contract and the product teams adopt it. Request `{customerId?, organizationId?, productId, featureKey?}`; response `{allowed, reason, productId, plan, ownerType, expiresAt, includedFeatures, usageLimit}` with reasons `NO_SUBSCRIPTION`, `NO_MEMBER_ACCESS`, `FEATURE_NOT_INCLUDED`, `UNKNOWN_PRODUCT`, `BAD_REQUEST`. See [the API doc](../../../06-api/api-requirements/entitlements.md). |
| 2 | ~~**Admin read access:** should a platform administrator be able to look up any customer's or organization's entitlements?~~ | **Answered — EN-3 A.** Yes, read-only, with a new support permission **View entitlements** (`VIEW_ENTITLEMENTS`). Lookups are audited (`ENTITLEMENTS_VIEWED`). |
| 3 | ~~**Licensing and quantity (D14):** not decided.~~ | **Answered — EN-4 A and EN-5 A.** No licence keys (07.03.01 dropped; all products are online SaaS). EIS stores only the plan's limit; each product counts usage and enforces it. Seats are [REQ-SUB-003](../subscription-seats/requirement.md). |
| 4 | ~~**Is an entitlement a stored record or derived?**~~ | **Answered — EN-1 A.** Read-only and derived from the active subscription, the plan's features and the member's product access. |

## Defaults applied (not asked)
Recorded here because the product owner did not specify them:
- **Feature match** is case-insensitive on the trimmed `includedFeatures` entry (comma-separated list on the plan).
- **A subscription past its `expiresAt`** grants nothing even before the hourly expiry job flips it to EXPIRED ([BR-1](business-rules.md)), **unless** the job has already set its grace end date and that is still in the future: then it is read-only ([BR-11](business-rules.md)). The job runs hourly, so for up to an hour after a term ends with auto-renew off the check still says `NO_SUBSCRIPTION`.
- **`accessMode` on a plan-less or never-ending subscription** is `FULL`; `graceEndsAt` is null unless the answer is `READ_ONLY`.
- **A product that is not ACTIVE in the catalogue** is `UNKNOWN_PRODUCT` and is left out of the lists.
- **A request naming neither `customerId` nor `organizationId`** is answered `BAD_REQUEST` (HTTP 200, `allowed: false`); a body without `productId` is refused with HTTP 400.
- **Organization endpoint access:** `GET /organization/me/entitlements` is open to any ACTIVE member of the organization (the existing `/organization/me/**` rule); no extra organization permission is required, although the first draft of REQ-SUB-002.3 said access follows existing organization permissions. The data is only the products and plans, no money or personal data. Tighten it (for example to `MANAGE_SUBSCRIPTIONS`) if the product owner wants.
- **`customerId` + `organizationId` together** means "this customer as a member of that organization".
