# Screen: API keys

| Field | Value |
|---|---|
| Requirement | [REQ-INT-001.1, .4](../../02-requirements/FRD/api-management/requirement.md) |
| Placement | **Organization → Sign-in security → API keys tab** (`/organization/identity-federation?tab=apiKeys`), for organization administrators ([C92](../../01-business/roadmap/open-decisions.md#c92), API-1 B). **Account → Security** keeps a section for legacy personal keys, shown only while some exist |
| Permissions | *Manage organization* (organization administrators), for the organization's keys; any signed-in user for their own legacy personal keys (revoke only) |
| Built | 2026-10-03; reworked 2026-10-13 — `frontend/src/components/organization/OrganizationApiKeysSection.tsx` (organization keys), `frontend/src/components/security/ApiKeysSection.tsx` (legacy personal keys) |

## Organization API keys (C92)
Heading "Organization API keys", text explaining that keys belong to the organization, **Create key**. Table: Name, Key, **Scope** (Read-only / Read-write), Status, **Created by**, Created, Expires, Last used, **Revoke**. Empty state "No API keys for your organization yet."

## Legacy personal keys section (Account → Security)
Shown only when the person still has personal keys: "Personal API keys (legacy)", explaining they keep working read-write; same table with Scope and **Revoke**; no create button.

## API keys section (before C92)
- Heading "API keys" with a key icon, short text "Use a key to call the EIS API from your own integrations. A key acts with your permissions." and **Create key**.
- Table: Name, Key (prefix + `…`), Status chip (Active / Revoked / Expired), Created, Expires, Last used, **Revoke** (active keys).
- Empty state: "No API keys yet."

## Create key dialog
Fields: **Name** (required, max 100), **Scope** (Read-only default / Read-write, with a one-line explanation), **Expires on** (optional date, after today). **Create**. Then the dialog shows the full key in a monospace box with **Copy**, and a warning "Copy this key now. You won't be able to see it again." with **Done**.

## Revoke confirmation
"Revoke {name}? Integrations using it stop working immediately." **Revoke** / **Cancel**.

## Admin API key usage
Route `/admin/integrations/api-keys` (Admin → Integrations → API keys, `MANAGE_INTEGRATIONS`): table of every key — Owner (email), **Organization** (or "Personal (legacy)"), Name, Key prefix, **Scope**, Status, Created, Last used, Requests. Read-only.

## Accessibility and i18n
Dialogs labelled; the one-time key box is a read-only text field with a label; axe tests for the section, the dialogs and the admin page. Strings under `apiKeys.*`, `orgApiKeys.*`, `orgSecurity.tabs.apiKeys` and `admin.apiKeys.*`.
