# UI requirements — Organization Lifecycle

## Screens
| Screen | Route | Roles | Wireframe |
|---|---|---|---|
| Organization detail page header (moved from the Registrations list by [C83](../../../01-business/roadmap/open-decisions.md#c83)); the list is the Organizations directory | `/admin/organizations/organization/:id` (`/admin/registrations` redirects) | `MANAGE_REGISTRATIONS` | Not specified |
| Edit organization dialog | same | same | Not specified |
| Confirm suspend / activate / close dialog | same | same | Not specified |

## Per organization card
- A lifecycle chip ("Lifecycle: Active / Suspended / Closed") next to the registration status chip.
- Buttons: **Edit** (disabled when Closed), **Suspend** (only when Active), **Activate** (when Suspended or Closed), **Close** (when not Closed). Each has an accessible name including the organization name, for example "Suspend Acme".

## Edit dialog fields
| Field | Required | Validation | Error |
|---|---|---|---|
| Name, Business email, Country | Yes | Save disabled while empty; backend validates email and lengths | Backend message shown as returned |
| Phone, Type, Industry, Website, State, City, Address, GSTIN, PAN, Company registration number, Tax / VAT number | No | Backend lengths | Backend message |
| Billing same as address (checkbox); Billing address, country, state, city (shown when unchecked) | No | - | - |

The dialog states that the code, seats, MFA policy and parent are not changed here.

## Confirm dialog
Title "Suspend / Activate / Close {{name}}?", a sentence on the effect, an optional Reason (max 500, recorded in the audit log), Cancel and the action button. A refusal keeps the dialog open with the backend message.

## After an action
A success alert "{{name}} is suspended. N member login(s) disabled." If some logins were not updated, a warning alert lists them and says to repeat the action to retry.

## Accessibility and localization
New text is in `en.json` and `es.json` under `adminOrgLifecycle`. A jest-axe check runs in `AdminOrganizationDetailPage.test.tsx`.
