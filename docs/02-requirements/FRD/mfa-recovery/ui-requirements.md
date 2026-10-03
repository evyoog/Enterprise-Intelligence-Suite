# UI requirements — MFA Recovery

| Screen | Where | Roles |
|---|---|---|
| "Reset 2FA" button per member, with a confirm dialog | Members and roles card on Organization settings (`/organization/settings`, C69) | Organization administrator |
| "Reset a user's 2FA" button, with a confirm dialog that asks for the email | `/admin/registrations` header | Platform administrator |

The dialog explains what is removed and that the user is notified. Refusals are shown as returned by the backend and keep the dialog open. Text is under `mfaReset` in `en.json` and `es.json`.
