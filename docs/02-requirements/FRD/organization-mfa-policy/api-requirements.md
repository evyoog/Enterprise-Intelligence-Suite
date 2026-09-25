# API requirements — Organization MFA Policy

All endpoints **already exist** on branch `dev`. Paths are relative to the backend base path `/api`. Error bodies are `{"message": "…"}` from `GlobalExceptionHandler`.

| Method | Path | Purpose | Permission | Success | Errors |
|---|---|---|---|---|---|
| GET | `/organization/me` | Read the organization, including `mfaRequired` | Active member | 200 `OrganizationDto` | 403, 404 |
| PATCH | `/organization/me/mfa-policy` | Set the MFA requirement | `MANAGE_ORGANIZATION` | 200 `OrganizationDto` | 400, 403, 404 |

## Request / response
`PATCH /organization/me/mfa-policy` request (`UpdateMfaPolicyRequest`):
```json
{ "mfaRequired": true }
```

`OrganizationDto` response fields: `id`, `name`, `code`, `type`, `industry`, `website`, `businessEmail`, `country`, `licensedSeats`, `activeMemberCount`, `status`, `mfaRequired`.

Frontend client: add the PATCH call to `frontend/src/api/registrationApi.ts` (the existing `organizationApi` object already has `getMyOrganization`).

OpenAPI contract: `docs/06-api/openapi/API-<APP-CODE>-<NNN>.yaml` (not created). The running backend serves its live spec at `/api/swagger-ui.html` in dev and uat.
