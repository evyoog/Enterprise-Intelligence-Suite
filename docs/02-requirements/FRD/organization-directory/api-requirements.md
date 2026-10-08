# API requirements — Organizations directory

All under `/admin/organizations`, permission `MANAGE_REGISTRATIONS`, read-only.

| Method | Path | Purpose |
|---|---|---|
| GET | `/directory` | Every organization and individual with the columns of REQ-TEN-007.2, plus `summary` counts |
| GET | `/{id}` | Organization overview (full company details) with profile completion |
| GET | `/{id}/members` | Members with role, status and hierarchy node |
| GET | `/{id}/subscriptions` | Subscriptions (product, status, seats, dates, auto-renew) |
| GET | `/{id}/invoices` | Invoices (number, status, total, dates) |
| GET | `/{id}/tickets` | Tickets raised by the organization's members |
| GET | `/{id}/org-hierarchy` | Tree and levels (empty when never opened) |
| GET | `/{id}/org-hierarchy/nodes/{nodeId}` | Node detail |
| GET | `/{id}/org-hierarchy/nodes/{nodeId}/history` | Move history |
| GET | `/individuals/{customerId}` | Individual profile with completion, subscriptions, invoices and tickets |

The Activity tab uses the existing `GET /admin/audit-log?organizationId=`. Editing and lifecycle actions use the existing `/admin/registrations/**` endpoints.
OpenAPI contract: not maintained separately ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
