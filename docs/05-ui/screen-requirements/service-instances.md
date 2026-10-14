# Screens: Service instances

| Field | Value |
|---|---|
| Requirement | [REQ-SRM-001](../../02-requirements/FRD/service-instances/requirement.md) (.6 to .9); business rules [BR-SRM-009 to BR-SRM-014](../../02-requirements/FRD/service-instances/business-rules.md) |
| Decision | [C101](../../01-business/roadmap/open-decisions.md#c101) (SR-3 B, SR-4 A, SR-5 B) |
| Built | Sprint 2027.1.2: `frontend/src/pages/admin/AdminServiceInstancesPage.tsx` (Instances tab), `frontend/src/pages/OrganizationServiceInstancesPage.tsx`, `frontend/src/pages/ServiceStatusPage.tsx` (caption), `frontend/src/api/serviceInstancesApi.ts`, `frontend/src/api/serviceStatusApi.ts` |

Shared presentation rules: unified MUI theme ([C45](../../01-business/roadmap/open-decisions.md#c45)), light and dark mode; status and health are text chips, never colour alone. Strings are under `serviceInstances.*` and `serviceTabs.*` in `frontend/src/i18n/locales/{en,es}.json`.

## 1. Instances tab (platform administrator)

| Field | Value |
|---|---|
| Route | **Platform → Service status** (`/status`), the **Instances** tab: `/status?tab=instances`. The admin tabs are `Status \| Manage \| Instances \| Incidents \| Problems` (query parameter `?tab=`) |
| Permission | Platform permission `MANAGE_SERVICE_STATUS`. Without it the tabs are not shown and `/status` is the customer status page; the API returns 403 |

### Layout
- **Header:** title **"Service instances"**, subtitle "One instance is created for every active subscription. Products report its health every minute; you can also change it by hand."
- **Summary:** five figures from `GET …/instances/summary`: **Instances**, **Healthy**, **Degraded**, **Down**, **No recent report**.
- **Filters** (`aria-label` "Filter service instances"): **Product** (All products), **Status** (All statuses), **Health** (All health). The filters narrow the table (and are sent to the API).
- **Table:**

| Column | Content |
|---|---|
| Product | Product name |
| Owner | Organization or individual name, with the owner type chip (Organization / Individual) |
| Status | Chip: Provisioning, Active, Suspended, Failed, Deprovisioned |
| Health | Chip: Healthy, Degraded, Down, Unknown; the health message under it; **"No recent report"** when `stale` |
| Last report | Date and time, or "Never" |
| Last changed by | System, Product or Administrator (with the change note for an administrator) |
| Tenant reference | The tenant reference when reported |
| Actions | **Change by hand** (accessible name "Change {product} ({owner}) by hand") |

### Change by hand dialog
Title "Change {product} by hand"; intro "Set the status or health yourself. A note is required and the change is recorded in the audit log."

| Field | Type | Required | Validation | Message |
|---|---|---|---|---|
| Status | Select (the five statuses) | One of status and health | Changed from the current value | "Change the status or the health first." |
| Health | Select (Healthy, Degraded, Down, Unknown) | One of status and health | Changed from the current value | same |
| Note | Text | Yes | Not blank | "Enter a note." |

The request sends only what changed. **Save change** (toast "Instance updated."); the server's refusal is shown in the dialog.

### Reporter keys
A section under the table, "Reporter keys": "Each product reports instance health with its own reporter key. The secret is shown only once, when you create the key." with the call to use: "Products send health with a POST to `/api/product-reports/health` using the header `X-Reporter-Key`."

| Column | Content |
|---|---|
| Product, Label | The product and the optional label |
| Key prefix | The first 12 characters (never the whole key) |
| Created, Last used, Revoked | Date and time, or empty |
| Actions | **Revoke** (accessible name "Revoke the {product} key {prefix}"), hidden for a revoked key |

- **Create key** opens a dialog: **Product** (required, "Choose a product.") and **Label (optional)**. On success a second dialog shows **"Reporter key for {product}"** with the secret, the warning **"Copy it now. It will not be shown again."**, a **Copy** button ("Copied") and **"I have copied it"**. The secret exists only in that dialog.
- **Revoke** asks "Revoke the {product} key?" — "The product can no longer report health with this key. This cannot be undone." and toasts "Key for {product} revoked."

### States
| State | Behaviour |
|---|---|
| Loading | Skeleton blocks |
| Empty | "No service instances" — "An instance is created when a subscription becomes active."; with filters "No instances match these filters."; keys: "No reporter keys" — "Create a key so a product can report the health of its instances." |
| Load error | "Could not load the service instances." / "Could not load the reporter keys." with a retry |
| Action error | The backend message, or "Could not complete this action." |

## 2. Service instances (organization administrator)

| Field | Value |
|---|---|
| Route | **Organization → Service instances**, `/organization/service-instances`; sidebar item **Service instances** in the **Organization** group, shown to organization administrators only |
| Permission | Signed-in organization administrator (organization permission `MANAGE_ORGANIZATION`). Others: the link is hidden and the API returns 403. **Read-only**: no action is offered |

- **Header:** "Service instances" — "The instances of your organization's subscriptions and their health."
- **Table:** Product, Status chip, Health chip (with message and "No recent report" when stale), Last report, Tenant reference, **Access URL** as a link. Deprovisioned instances are not listed.
- **Empty:** "No service instances yet" — "An instance appears here when one of your subscriptions becomes active." **Error:** "Could not load your service instances." with a retry.

## 3. Status page caption
On the customer status page (`/status`, the **Status** tab) a product whose status is derived from instances shows, under its status chip: **"Based on N reporting instances (x down, y degraded)"** (singular "Based on 1 reporting instance (…)"), from `source = INSTANCES`, `reportingInstances`, `downInstances` and `degradedInstances`. A product with `source = MANUAL` shows no caption, only the note the administrator wrote. The Manage tab shows the same caption so an administrator can tell a derived status from the one they set.

## Accessibility and i18n
Tables and the filter group have accessible names; every row action names its product; dialogs are labelled by their title; the secret field has an accessible name ("Reporter key secret"); statuses and health are text. axe tests on the Instances tab and the organization screen. All strings in `en.json` and `es.json`.

## API used
[service-instances.md](../../06-api/api-requirements/service-instances.md): `GET /admin/service-status/instances`, `…/instances/summary`, `PUT …/instances/{id}`, `GET`, `POST`, `DELETE …/reporter-keys`, `GET /organization/me/service-instances`, and the `ProductStatusDto` fields of `GET /me/service-status`.
