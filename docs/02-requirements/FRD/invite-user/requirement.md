# REQ-TEN-008 — Invite user

**Status:** Approved (2026-10-08, [C84](../../../01-business/roadmap/open-decisions.md#c84))
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-08 ("Approved, use your recommendations", then "proceed with code")

| Field | Value |
|---|---|
| Sprint | [2026.4.1](../../../01-business/roadmap/sprints/SPRINT-2026.4.1.md) (decision IN-1; carried earlier by [C34](../../../01-business/roadmap/open-decisions.md#c34)) |
| Requirement ID | REQ-TEN-008 |
| Application | [05 Customer & Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md#feature-050301-user-lifecycle) |
| Functions | 05.03.01.01 Invite user. **05.03.01.02 Create user stays out** (see C84) |
| Priority | P0 |

## Summary
A person joins an organization through a secure email invitation. An organization administrator can invite by default; an administrator can also allow one specific member to send invitations (`INVITE_USERS`). The invited person uses their existing account (signs in) or creates an account from the invitation (first name, last name, password), then accepts. Acceptance creates (or re-admits) the organization membership with the invited role and an optional place in the organization structure.

## Actors
- **Organization administrator** (`ORG_ADMIN`): invites by default; grants and removes `INVITE_USERS` for a member.
- **Delegated inviter** (a `MEMBER` with `INVITE_USERS`): sends, resends and revokes **their own** invitations, only as `MEMBER`. No other member-management authority.
- **Invited person**: accepts or declines.
- **Platform administrator**: reads an organization's invitations (REQ-TEN-007 Members tab).

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-TEN-008.1 | Permission `INVITE_USERS` is separate from `MANAGE_USERS` and `MANAGE_PRODUCT_ACCESS`. `ORG_ADMIN` holds it by role. A member holds it only through an individual grant by an organization administrator (table `member_access_override`, the REQ-TEN-005 design). Removing the grant stops new invitations at once. | Must |
| REQ-TEN-008.2 | The server checks the caller on every call: organization administrator, or an active member with the grant. Only an organization administrator can invite as `ORG_ADMIN`. | Must |
| REQ-TEN-008.3 | **Invite form:** email (required, valid, normalized to lower case and trimmed), organization role (required; from the role list, today `ORG_ADMIN` and `MEMBER`), optional active structure node of the same organization. Product access is not part of an invitation. | Must |
| REQ-TEN-008.4 | **States:** PENDING, ACCEPTED, DECLINED, EXPIRED, REVOKED. Records are never deleted. | Must |
| REQ-TEN-008.5 | **Expiry:** 7 days. An overdue PENDING invitation is EXPIRED when read and by an hourly job (with an audit entry). | Must |
| REQ-TEN-008.6 | **One PENDING invitation** per organization and normalized email. Resend replaces the token (the old link stops working), restarts the 7 days and counts the send. PENDING, EXPIRED, DECLINED and REVOKED invitations can be resent. | Must |
| REQ-TEN-008.7 | **Token:** random, 32 bytes, URL-safe; only its SHA-256 hash is stored; single use; invalid after accept, decline, revoke, expiry or resend. The link carries the token only, no internal ids. The token is never returned by any list. | Must |
| REQ-TEN-008.8 | **Seats:** a PENDING invitation reserves nothing. Capacity is checked when sending (a plain check) and again on acceptance (authoritative, with the seat-overage setting and every active subscription's seat count). | Must |
| REQ-TEN-008.9 | **Organization state:** sending and accepting need an organization whose registration is in good standing and whose lifecycle is Active. | Must |
| REQ-TEN-008.10 | **Existing members:** an ACTIVE member cannot be invited; a SUSPENDED member is refused with a pointer to Reactivate; a removed (INACTIVE) member is re-admitted on acceptance on the same membership row with the new role (BR-INV-008). | Must |
| REQ-TEN-008.11 | **One active organization per person:** acceptance is refused if the person is an ACTIVE member of another organization. Nobody is moved automatically. | Must |
| REQ-TEN-008.12 | **Existing account:** the person signs in (returning to the invitation) and accepts. The signed-in account's email must equal the invited email. No second account is created. Personal subscriptions and invoices are untouched. | Must |
| REQ-TEN-008.13 | **New account:** the invitation page collects first name, last name, password and confirmation, creates the customer and the Keycloak user (enabled; the invitation proves the email), accepts the invitation and lets the person sign in. The standard password policy applies. | Must |
| REQ-TEN-008.14 | **Email:** sent through the existing SMTP service with organization, inviter, role, structure placement, expiry and the link. A delivery problem never loses the invitation: it stays PENDING and can be resent. | Must |
| REQ-TEN-008.15 | **Screens:** Invitations tab in People & structure (list, filter, invite, resend, revoke, details), Invite user dialog, public invitation page, an "Can invite" switch per member (administrators), read-only invitations in the platform administrator's organization Members tab. | Must |
| REQ-TEN-008.16 | **Visibility:** an administrator sees all invitations of the organization, a delegated inviter their own. | Must |
| REQ-TEN-008.17 | **Audit:** created, resent, revoked, accepted, declined, expired, accept refused (seat, organization state, other organization), `INVITE_USERS` granted and removed. | Must |
| REQ-TEN-008.18 | The data model and API allow bulk invitations and several organizations per person later without change (one row per invitation, no assumption of a single organization in the table). | Should |

## Out of scope (this sprint)
CSV bulk invitations, multi-organization membership, direct Create user (05.03.01.02), product roles in an invitation, permissions from structure nodes, an approval workflow for invitations, invitation analytics, external identity providers, moving a person between organizations.

## Dependencies
REQ-TEN-002 (member lifecycle), REQ-TEN-006 (organization structure), REQ-TEN-007 (directory), REQ-SUB-003 (seats), REQ-IAM-002/003 (roles and permissions), registration and Keycloak client, SMTP email, audit. A slice of REQ-TEN-005 (`member_access_override`, permission items only) is built here; the rest of REQ-TEN-005 stays Draft.
