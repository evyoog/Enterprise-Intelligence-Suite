# API requirements — Organization Lifecycle

New endpoints on `AdminRegistrationController`. Paths are relative to `/api`. All require `MANAGE_REGISTRATIONS`. Error bodies are `{"message": "…"}` from `GlobalExceptionHandler`.

| Method | Path | Purpose | Success | Errors |
|---|---|---|---|---|
| PUT | `/admin/registrations/organizations/{organizationId}` | Edit company details | 200 `OrganizationAdminDto` | 400, 401, 403, 404 |
| POST | `/admin/registrations/organizations/{organizationId}/suspend` | Suspend | 200 `OrganizationLifecycleResultDto` | 400, 401, 403, 404 |
| POST | `/admin/registrations/organizations/{organizationId}/activate` | Activate | 200 `OrganizationLifecycleResultDto` | 401, 403, 404 |
| POST | `/admin/registrations/organizations/{organizationId}/close` | Close (soft) | 200 `OrganizationLifecycleResultDto` | 400, 401, 403, 404 |

## Request / response
`PUT` body (`UpdateOrganizationRequest`):
```json
{ "name": "Acme", "businessEmail": "biz@acme.example", "country": "India", "phone": "+91 …",
  "type": null, "industry": null, "website": null, "state": null, "city": "Chennai", "address": null,
  "gstin": null, "pan": null, "companyRegistrationNumber": null, "taxVatNumber": null,
  "billingSameAsAddress": true, "billingAddress": null, "billingCountry": null, "billingState": null, "billingCity": null }
```

Lifecycle body (`OrganizationLifecycleRequest`, optional): `{ "reason": "Unpaid invoice" }`.

`OrganizationLifecycleResultDto`: `{ "organization": OrganizationAdminDto, "accountsUpdated": 2, "accountsNotUpdated": ["ops@acme.example"] }`.

`OrganizationAdminDto` gains `lifecycleStatus`: `ACTIVE` | `SUSPENDED` | `CLOSED`.

Frontend client: `adminRegistrationApi.updateOrganization` and `adminRegistrationApi.changeOrganizationLifecycle` in `frontend/src/api/adminRegistrationApi.ts`.
