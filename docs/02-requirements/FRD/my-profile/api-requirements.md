# API requirements — My profile

Convention: `/me/profile` is always the signed-in person's own profile; the caller is resolved from the token, never from an id. All calls need sign-in (`/me/**` is authenticated). JSON is camelCase. Full payloads and errors: [my-profile.md](../../../06-api/api-requirements/my-profile.md).

| Method | Path | Purpose | Who |
|---|---|---|---|
| GET | `/me/profile` | Own profile (`ProfileDto`) | signed in |
| PUT | `/me/profile` | Replace personal, work, address and online; creates the record of a platform administrator who has none → `ProfileDto` | signed in |
| GET | `/me/profile/completion` | `{exists, percent, filled, total, missing}` for the global bar; 200 with `exists:false` when the person has no record (never 404) | signed in |
| PUT | `/me/profile/photo` | Multipart field `file` → `ProfileDto` | signed in |
| DELETE | `/me/profile/photo` | Remove the photo → `ProfileDto` | signed in |
| GET | `/me/profile/photo` | Image bytes, stored `Content-Type`, `Cache-Control: private, max-age=0, must-revalidate`, `ETag` (304 on a matching `If-None-Match`); 404 when none | signed in |
| GET | `/admin/registrations/individuals/{customerId}/profile` | Any existing customer's profile (no photo) | platform `MANAGE_REGISTRATIONS` |
| PUT | `/admin/registrations/individuals/{customerId}/profile` | Replace the four sections of that customer | platform `MANAGE_REGISTRATIONS` |

Errors have the body `{timestamp, status, error, message}`; a validation failure adds `fieldErrors`. 400 validation (`message:"Validation failed", fieldErrors:{"<section>.<field>": "<message>"}`; also a login with no email claim trying to create a record) or a wrong photo type or content, 401 not signed in, 403 not allowed (admin paths, or an API key used on `/me/profile`), 404 no customer record (non-administrator) or unknown customer, 409 the token's email belongs to a customer linked to a different login (or a platform administrator with no record adds or removes a photo), 413 photo too large (or a multipart request over the server limit), 502 Keycloak could not be updated (nothing saved).
