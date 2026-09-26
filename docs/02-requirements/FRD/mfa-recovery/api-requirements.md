# API requirements — MFA Recovery

| Method | Path | Purpose | Permission | Success | Errors |
|---|---|---|---|---|---|
| POST | `/organization/me/members/{memberId}/mfa/reset` | Reset a member's MFA | `MANAGE_USERS` on the member | 204 | 400, 403, 404 |
| POST | `/admin/registrations/mfa-reset` | Reset any account's MFA by email; body `{ "email": "…" }` | `MANAGE_REGISTRATIONS` | 204 | 400, 401, 403, 404 |

Frontend: `organizationApi.resetMemberMfa`, `adminRegistrationApi.resetMfa`.
