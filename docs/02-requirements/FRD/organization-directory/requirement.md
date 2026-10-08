# REQ-TEN-007 — Organizations directory (platform administration)

**Status:** Approved (2026-10-08, [C83](../../../01-business/roadmap/open-decisions.md#c83))
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-10-08 ("okay proceed with this plan")

| Field | Value |
|---|---|
| Sprint | [2026.4.2](../../../01-business/roadmap/sprints/SPRINT-2026.4.2.md) (added by C83; dates unchanged) |
| Requirement ID | REQ-TEN-007 |
| Application | [05 Customer & Tenant Management](../../../01-business/roadmap/applications/05-customer-tenant-management.md) |
| Replaces | the "Registrations" page (tabs Organizations, Individuals, Pending Keycloak provisioning) |
| Priority | P1 |

## Summary
The platform administrator opens one **Organizations** screen listing every organization and every individual customer, with a filter row on top, and opens any row to see its details in tabs, including the organization's hierarchy (REQ-TEN-006) and how completely its profile is filled in.

## Actors
- **Platform administrator** (`MANAGE_REGISTRATIONS`).

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-TEN-007.1 | One list shows organizations and individuals together, with a **Type** column. There are no Individuals or Pending-provisioning tabs: the sign-in account is created at registration, and a "Sign-in linked" column flags any account that is not linked. | Must |
| REQ-TEN-007.2 | Columns: name, code, type, industry, location (city, state, country), contact (name, email, phone), seats (used / licensed), status, lifecycle, region, parent organization, products, hierarchy nodes, open tickets, outstanding invoices, MFA required, sign-in linked, registered on, **profile completion**. | Must |
| REQ-TEN-007.3 | A filter row on top: search (name, code, email), Type, Status, Lifecycle, Country, Region, Industry, Profile (Complete, In progress, Not started), Seats (Full, Available, Over limit), Registered from/to, and *Clear filters*. Columns sort. The list pages. Filters are kept in the address. | Must |
| REQ-TEN-007.4 | **Profile completion** (percentage and the list of missing items) is shown for every row and as summary counts above the list (complete, in progress, not started, average). | Must |
| REQ-TEN-007.5 | Row actions as before: view, edit, suspend, activate, close, change seats (organizations); reset MFA by email stays available on the page. | Must |
| REQ-TEN-007.6 | Opening an organization shows tabs **Overview**, **Structure** (the org chart, read-only), **Members**, **Subscriptions**, **Billing**, **Support**, **Security**, **Activity**. | Must |
| REQ-TEN-007.7 | Opening an individual shows tabs **Profile**, **Subscriptions**, **Billing**, **Support**, **Activity**. | Must |
| REQ-TEN-007.8 | Everything on the detail pages is read-only except the existing organization actions; the platform administrator cannot change an organization's hierarchy (C82/C83). | Must |
| REQ-TEN-007.9 | All text is translated (English, Spanish), keyboard accessible and passes the accessibility checks. | Must |

## Out of scope (not built)
CSV export of the list, a server-side filtered query for very large tenant counts (the list is filtered and paged in the browser), editing an organization's hierarchy as the platform administrator, per-organization audit export. See [C83](../../../01-business/roadmap/open-decisions.md#c83).
