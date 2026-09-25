# REQ-TEN-001 — Organization Lifecycle (platform admin)

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-25 ([C25](../../../01-business/roadmap/open-decisions.md#c25))

| Field | Value |
|---|---|
| Sprint | [2026.4.2](../../../01-business/roadmap/sprints/SPRINT-2026.4.2.md), built early in 2026.3.3 ([C25](../../../01-business/roadmap/open-decisions.md#c25)) |
| Requirement ID | REQ-TEN-001 |
| Application | [05 Customer / Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md) |
| Application code | `APP-TEN` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0: the application is MVP scope ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No. This feature makes no use of AI ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 05.01.01.02 | Update organization | [05 Customer / Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md#feature-050101-organization-lifecycle) |
| 05.01.01.03 | Suspend organization | same |
| 05.01.01.04 | Activate organization | same |
| 05.01.01.05 | Close organization | same |

05.01.01.01 Create organization is **not** in this FRD: organizations are created only through self-registration ([C25](../../../01-business/roadmap/open-decisions.md#c25)).

## Summary
On the admin registrations page (`/admin/registrations`, Organizations tab) a platform administrator can edit an organization's company details and suspend, activate or close it. Close is a soft close: nothing is deleted, and a closed organization can be activated again. Suspending or closing disables the Keycloak login of every active member and ends their sessions; activating enables them again.

## Actors
- Platform administrator (holding `MANAGE_REGISTRATIONS`, the existing gate on `/admin/registrations/**`)
- Organization members (their logins are disabled or enabled)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-TEN-001.1 | The administrator can edit an organization's company details: name, business email, phone, type, industry, website, country, state, city, address, GSTIN, PAN, company registration number, tax / VAT number and billing address. | P0 |
| REQ-TEN-001.2 | The administrator can suspend an organization, with an optional reason. | P0 |
| REQ-TEN-001.3 | The administrator can activate a suspended or closed organization, with an optional reason. | P0 |
| REQ-TEN-001.4 | The administrator can close an organization (soft close), with an optional reason. | P0 |
| REQ-TEN-001.5 | Each organization shows its lifecycle status (Active, Suspended, Closed) next to its registration status. | P0 |
| REQ-TEN-001.6 | After a lifecycle action the page reports how many member logins were updated, and lists any Keycloak did not update. | P0 |

## Out of scope
- Creating organizations from the admin page (C25)
- Hard delete of any organization, member or customer (C25)
- Lifecycle actions on individual customers (C25)
- Editing the organization code, seats (existing seat editor), MFA policy (organization admin's setting) or parent organization
- Inviting, suspending or removing single members (05.03.01 User Lifecycle)

## Dependencies
- Existing `GET /admin/registrations/organizations`, `KeycloakAdminClient#setEnabled` and `#logoutAllSessions`, `AuditService`.
- New column `organization.lifecycle_status` ([V001](../../../../database/migrations/V001__add_organization_lifecycle_status.sql)).
