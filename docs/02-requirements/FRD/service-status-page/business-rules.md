# Business rules — Interim Service Status Page

Decided in [C20](../../../01-business/roadmap/open-decisions.md#c20) (2026-09-25) and [C26](../../../01-business/roadmap/open-decisions.md#c26) (2026-09-26). Enforced by the backend; the cited class and method are the source of truth.

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-PRT-001.1 | Status and incidents are posted **manually** by platform administrators with the platform permission `MANAGE_SERVICE_STATUS` (seeded for `ADMIN`). Otherwise 401/403. | backend | `SecurityConfig` (`/admin/service-status/**`), `RbacSeeder` |
| BR-PRT-001.2 | Status values: Operational, Degraded, Partial outage, Major outage, Maintenance. A product with no posted status is Operational. Only ACTIVE catalog products are listed or can be posted (404 "Product not found" otherwise). | backend | `ServiceStatusValue`, `ServiceStatusService#updateProductStatus` |
| BR-PRT-001.3 | Every signed-in customer sees every active product's status. Incident details (title, message, times) are returned only for products the customer has purchased: the organization's ACTIVE subscriptions for an organization member, the customer's own ACTIVE subscriptions for an individual. A caller with no customer account sees status only. | backend | `ServiceStatusService#customerView`, `#purchasedProductIds` |
| BR-PRT-001.4 | The customer view is switched on or off by `app.status-page.enabled` (default on). When off, `GET /me/service-status` returns `enabled: false` with no products or incidents; the admin view keeps working. | backend + frontend | `ServiceStatusService` constructor, `ServiceStatusPage` |
| BR-PRT-001.5 | An incident is open while it has no end time. The end time may not be before the start time: 400 "The end time must be after the start time." The customer view lists the latest 50 incidents of purchased products; the admin view the latest 200. | backend | `ServiceStatusService#apply`, `ServiceIncidentRepository` |
| BR-PRT-001.6 | The business dashboard raises one alert (type `SERVICE_STATUS`) per purchased product that is not Operational or has an open incident; severity `error` for a major outage, `warning` otherwise. | backend | `BusinessDashboardService#serviceStatusAlerts` |
| BR-PRT-001.7 | Every change is audited: `SERVICE_STATUS_CHANGED`, `INCIDENT_POSTED`, `INCIDENT_UPDATED`. | backend | `ServiceStatusService` |
| BR-PRT-001.8 | In sprint 2027.1.1 per-product status switches to Health Monitoring (10.04). In sprint 2027.1.3 incidents switch to Incident & Problem Management (12.04), and the interim page is removed after that sprint. | plan | [C20](../../../01-business/roadmap/open-decisions.md#c20) |
