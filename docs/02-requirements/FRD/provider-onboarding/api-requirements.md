# API requirements — Provider Onboarding

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| POST | `/partners/apply` | Register provider (BR-PTR-001, BR-PTR-002) | Public | 200 (`ProviderDto`) | 400 |
| GET | `/admin/partners` | Every provider, any status | `MANAGE_PARTNERS` | 200 (`ProviderDto[]`) | 403 |
| GET | `/admin/partners/{id}` | One provider | `MANAGE_PARTNERS` | 200 (`ProviderDto`) | 403, 404 |
| POST | `/admin/partners/{id}/verify` | Verify provider (BR-PTR-003) | `MANAGE_PARTNERS` | 200 (`ProviderDto`) | 400, 403, 404 |
| POST | `/admin/partners/{id}/approve` | Approve provider (BR-PTR-003) | `MANAGE_PARTNERS` | 200 (`ProviderDto`) | 400, 403, 404 |
| POST | `/admin/partners/{id}/activate` | Activate provider (BR-PTR-003) | `MANAGE_PARTNERS` | 200 (`ProviderDto`) | 400, 403, 404 |
| POST | `/admin/partners/{id}/reject` | Reject provider (BR-PTR-004) | `MANAGE_PARTNERS` | 200 (`ProviderDto`) | 400, 403, 404 |
| PUT | `/admin/partners/{id}/contract` | Create contract / Manage terms (BR-PTR-005–.007) | `MANAGE_PARTNERS` | 200 (`PartnerContractDto`) | 400, 403, 404 |
| GET | `/admin/partners/{id}/contract` | The provider's current contract | `MANAGE_PARTNERS` | 200 (`PartnerContractDto`) | 403, 404 (none on file yet) |

## Request / response
```json
// POST /partners/apply
{ "name": "Acme Cloud", "contactName": "Jane Doe", "contactEmail": "jane@acme.example", "description": "A cloud reseller." }
```
```json
// PUT /admin/partners/7/contract
{ "terms": "Standard reseller terms.", "startDate": "2027-04-01", "endDate": "2028-04-01" }
```
```json
// GET /admin/partners/7
{ "id": 7, "name": "Acme Cloud", "contactName": "Jane Doe", "contactEmail": "jane@acme.example",
  "description": "A cloud reseller.", "status": "VERIFIED", "createdAt": "2027-04-01T10:00:00Z" }
```

OpenAPI contract: not maintained separately — `ProviderApplicationController`/`AdminProviderController`/`ProviderDto`/`PartnerContractDto` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
