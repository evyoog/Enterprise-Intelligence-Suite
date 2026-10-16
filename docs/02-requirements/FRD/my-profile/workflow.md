# Workflow — My profile

```mermaid
flowchart TD
  A[Signed-in person opens My profile] --> B{Has a customer record?}
  B -- yes --> C[Header card, completion, five tabs filled from the record]
  B -- no, platform admin: MANAGE_REGISTRATIONS or listed in the registry --> D[Banner: profile not created, optional. Create my profile, names prefilled]
  B -- no, other role or not listed --> N[404: nothing to edit]
  D --> E[Fill names and any field, Save]
  C --> E
  E --> F{Client validation passes?}
  F -- no --> G[Inline errors, badge on the tab, Save refused]
  F -- yes --> H[PUT /me/profile]
  H --> I{Server validation passes?}
  I -- no --> J[400 fieldErrors mapped onto the fields]
  I -- yes --> K{Names changed?}
  K -- yes --> L[Update Keycloak user]
  L -- fails --> M[Roll back everything, 502, error shown]
  L -- ok --> O[Store, set profile_updated_at, audit]
  K -- no --> O
  O --> P[Toast, global bar and header refresh]
```

## Photo
Choose a file → the browser checks type and size and shrinks it to 512 x 512 → `PUT /me/profile/photo` → the server checks type, first bytes, size and dimensions → stored, audited `PROFILE_PHOTO_UPDATED` → the avatar and the bar refresh. Remove → `DELETE /me/profile/photo` → `PROFILE_PHOTO_REMOVED` → initials return.

## Global bar
Sign in → `GET /me/profile/completion` → the bar shows when `exists` and `percent` below 100 and the page is not `/account/profile` → *Complete profile* opens the page, the dismiss control hides it for the session → a save or photo change reloads the figure.

## Platform administrator edits a customer
Organization detail (members) or the individuals list → *Edit profile* dialog → `GET /admin/registrations/individuals/{customerId}/profile` → edit the four sections (no photo) → `PUT` (same validation, Keycloak mirroring, audit `PROFILE_UPDATED_BY_ADMIN`).

## Platform administrator registry
Sign in (Keycloak) → a profile call (`/me/profile`, `/me/profile/completion` or `/me/profile/photo`) → the person has a record: continue as for everyone → no record: does the sign-in hold `MANAGE_REGISTRATIONS`, or match an enabled registry entry (by `keycloakSub`, or by a verified email ignoring letter case; never an API key)? → yes: `exists=false` and the optional "Create my profile" action; no: 404 "No Vyoog account is linked to this login". The registry is re-read lazily at most every 60 seconds from its configured location; if a refresh fails the last good copy is kept; if the first load fails nobody extra matches. Adding or disabling an administrator is a change to the file (later the S3 object), with no redeploy: see [platform-admin-registry.md](../../../08-architecture/security/platform-admin-registry.md).
