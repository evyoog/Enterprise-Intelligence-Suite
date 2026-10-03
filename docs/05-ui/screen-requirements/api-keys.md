# Screen: API keys

| Field | Value |
|---|---|
| Requirement | [REQ-INT-001.1, .4](../../02-requirements/FRD/api-management/requirement.md) |
| Placement | **Section on Account → Security** (`/account/security`) — default until REQ-INT-001 Open question 1 (who gets keys) is answered ([C44](../../01-business/roadmap/open-decisions.md#c44): no new screen) |
| Permissions | Any signed-in user with a Vyoog account, for their own keys |
| Built | 2026-10-03 — `frontend/src/components/security/ApiKeysSection.tsx` |

## API keys section
- Heading "API keys" with a key icon, short text "Use a key to call the EIS API from your own integrations. A key acts with your permissions." and **Create key**.
- Table: Name, Key (prefix + `…`), Status chip (Active / Revoked / Expired), Created, Expires, Last used, **Revoke** (active keys).
- Empty state: "No API keys yet."

## Create key dialog
Fields: **Name** (required, max 100), **Expires on** (optional date, after today). **Create**. Then the dialog shows the full key in a monospace box with **Copy**, and a warning "Copy this key now. You won't be able to see it again." with **Done**.

## Revoke confirmation
"Revoke {name}? Integrations using it stop working immediately." **Revoke** / **Cancel**.

## Admin API key usage
Route `/admin/integrations/api-keys` (Admin → Integrations → API keys, `MANAGE_INTEGRATIONS`): table of every key — Owner (email), Name, Key prefix, Status, Created, Last used, Requests. Read-only.

## Accessibility and i18n
Dialogs labelled; the one-time key box is a read-only text field with a label; axe tests for the section, the dialogs and the admin page. Strings under `apiKeys.*` and `admin.apiKeys.*`.
