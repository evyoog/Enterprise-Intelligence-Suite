# REQ-PTR-001 — Provider Onboarding

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-28 ([C42](../../../01-business/roadmap/open-decisions.md#c42))

| Field | Value |
|---|---|
| Sprint | [2027.2.1](../../../01-business/roadmap/sprints/SPRINT-2027.2.1.md) |
| Requirement ID | REQ-PTR-001 |
| Application | [14 Partner & Provider Management](../../../01-business/roadmap/applications/14-partner-provider-management.md) |
| Application code | `APP-PTR` |
| Priority | P1/stretch — application 14 has no P0 capability this sprint ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)); see [C42](../../../01-business/roadmap/open-decisions.md#c42) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 14.01.01 | Register provider; Verify provider; Approve provider; Activate provider | [14 Partner & Provider Management](../../../01-business/roadmap/applications/14-partner-provider-management.md#1401-provider-onboarding) |
| 14.01.02 | Create contract; Manage terms; Track expiration | [14 Partner & Provider Management](../../../01-business/roadmap/applications/14-partner-provider-management.md#1401-provider-onboarding) |

14.02 Publisher Management, 14.03 Revenue Sharing and 14.04 Partner Operations are **not** covered by this requirement — see [C42](../../../01-business/roadmap/open-decisions.md#c42).

## Summary
A prospective partner applies to become a provider before it has any Vyoog identity (public, no account required). A platform admin (`MANAGE_PARTNERS`) verifies, then approves, then activates the application — three separate gates, each its own decision made at its own time. Once a provider exists, the same admin can record and edit its contract (terms, start/end date); a scheduled job flips an overdue contract from ACTIVE to EXPIRED automatically.

## Actors
- Any visitor (no Vyoog account) — applies to become a provider
- Platform administrator (`MANAGE_PARTNERS`) — verifies/approves/activates/rejects providers and manages contracts

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-PTR-001.1 | Anyone can submit a provider application (company name, contact name, contact email, optional description) with no Vyoog account. | Must |
| REQ-PTR-001.2 | A new application starts REGISTERED. An admin can move it forward one stage at a time: REGISTERED → VERIFIED → APPROVED → ACTIVE. Skipping a stage is refused. | Must |
| REQ-PTR-001.3 | An admin can reject a provider at any stage before ACTIVE. REJECTED and ACTIVE are both terminal with respect to rejection. | Must |
| REQ-PTR-001.4 | An admin can create or edit a provider's single contract (terms, start date, end date) — the same upsert for both create and edit. | Must |
| REQ-PTR-001.5 | A contract past its end date is automatically flipped from ACTIVE to EXPIRED by a scheduled job; editing a contract's terms reactivates it to ACTIVE. | Must |

## Out of scope
- Publisher Management (14.02): `Product` has no publisher/owner field in this codebase; scoping "a partner manages only its own catalog listings" needs its own decision — see [C42](../../../01-business/roadmap/open-decisions.md#c42).
- Revenue Sharing (14.03): needs Billing (08), which does not exist yet (same gap as [C38](../../../01-business/roadmap/open-decisions.md#c38)).
- Partner Operations (14.04): unprioritized in any source, and needs a Partner Manager role/identity decision.
- Any partner-facing login/portal — a provider has no Keycloak identity in this requirement; every action here is either public (apply) or platform-admin-only.

## Dependencies
- New tables `provider`, `partner_contract` ([V012](../../../../database/migrations/V012__provider_onboarding.sql)).
- New permission `MANAGE_PARTNERS` (platform ADMIN).
- New scheduled job `ContractExpiryJob`, same pattern as `SubscriptionExpiryJob` (07.04.01).
- New public route `/partners/apply` and admin routes `/admin/partners`, `/admin/partners/:id`.
