# API requirements — Tenant Lifecycle

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| PUT | `/admin/registrations/organizations/{organizationId}` | Now also accepts `regionId` (nullable) and `allowSeatOverage` | `MANAGE_REGISTRATIONS` | 200 (`OrganizationAdminDto`, now with `regionId`, `regionName`, `allowSeatOverage`) | 400, 403, 404 (unknown region) |

## Request / response
```json
// PUT /admin/registrations/organizations/42
{
  "name": "Acme Corp",
  "businessEmail": "ops@acme.example",
  "country": "India",
  "billingSameAsAddress": true,
  "regionId": 3,
  "allowSeatOverage": true
}
```
```json
// 200
{
  "id": 42,
  "regionId": 3,
  "regionName": "Asia Pacific",
  "allowSeatOverage": true
}
```

OpenAPI contract: not maintained separately — `AdminRegistrationController`/`OrganizationAdminDto` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
