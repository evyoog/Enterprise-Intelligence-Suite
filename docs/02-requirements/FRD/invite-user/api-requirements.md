# API requirements — Invite user

Convention: `/organization/me/...` is always the caller's own organization. Public calls use the token only.

| Method | Path | Purpose | Who |
|---|---|---|---|
| POST | `/organization/me/invitations` | Send `{email, orgRole, orgNodeId?}` → 201 `{invitation, emailSent}` | `ORG_ADMIN` or `INVITE_USERS` |
| GET | `/organization/me/invitations?status=` | List (administrator: all; delegated: own) | same |
| POST | `/organization/me/invitations/{id}/resend` | New token, 7 days | same (scope BR-INV-004) |
| POST | `/organization/me/invitations/{id}/revoke` | PENDING → REVOKED | same |
| GET | `/organization/me/invitations/structure-nodes` | Active structure nodes with path (for the form) | same |
| GET | `/organization/me/invitations/inviters` | Member ids that hold the individual grant | `ORG_ADMIN` |
| PUT | `/organization/me/members/{memberId}/invite-permission` | `{allowed}` grant or remove | `ORG_ADMIN` |
| GET | `/invitations/{token}` | Preview: status, organization, inviter, role, node, expiry, account exists | public |
| POST | `/invitations/{token}/account` | `{firstName, lastName, password, confirmPassword}`: create account and join | public |
| POST | `/invitations/{token}/accept` | Accept as the signed-in account | signed in |
| POST | `/invitations/{token}/decline` | Decline | public |
| GET | `/admin/organizations/{id}/invitations` | Read-only list | platform `MANAGE_REGISTRATIONS` |

`/me/permissions` lists `INVITE_USERS` for a member with the grant. Errors: 400 invalid input, 403 not allowed, 404 invalid token or invitation, 409 duplicate pending, already a member, suspended, other organization, no seat, organization unavailable.
