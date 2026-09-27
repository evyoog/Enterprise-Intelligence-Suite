# API requirements — Platform Administration

| Method | Path | Purpose | Permission | Success | Errors |
|--------|------|---------|------------|---------|--------|
| GET | `/admin/platform-settings/currencies` | List currencies | `MANAGE_PLATFORM_SETTINGS` | 200 | 403 |
| PATCH | `/admin/platform-settings/currencies/{code}` | Enable/disable | `MANAGE_PLATFORM_SETTINGS` | 200 | 403, 404 |
| GET | `/admin/platform-settings/regions` | List regions | `MANAGE_PLATFORM_SETTINGS` | 200 | 403 |
| POST | `/admin/platform-settings/regions` | Create (body `{"code","name"}`) | `MANAGE_PLATFORM_SETTINGS` | 201 | 400, 403, 409 |
| PUT | `/admin/platform-settings/regions/{id}` | Update (body `{"name","enabled"}`) | `MANAGE_PLATFORM_SETTINGS` | 200 | 400, 403, 404 |
| DELETE | `/admin/platform-settings/regions/{id}` | Delete | `MANAGE_PLATFORM_SETTINGS` | 204 | 400 (assigned), 403, 404 |
| GET | `/admin/platform-settings/feature-flags` | List flags | `MANAGE_PLATFORM_SETTINGS` | 200 | 403 |
| POST | `/admin/platform-settings/feature-flags` | Create (body `{"flagKey","enabled","description"}`) | `MANAGE_PLATFORM_SETTINGS` | 201 | 400, 403, 409 |
| PATCH | `/admin/platform-settings/feature-flags/{flagKey}` | Update (body `{"enabled","description"}`) | `MANAGE_PLATFORM_SETTINGS` | 200 | 400, 403, 404 |
| DELETE | `/admin/platform-settings/feature-flags/{flagKey}` | Delete | `MANAGE_PLATFORM_SETTINGS` | 204 | 403, 404 |
| GET | `/admin/platform-settings/languages` | Read-only list | `MANAGE_PLATFORM_SETTINGS` | 200 | 403 |

## Request / response
```json
// POST /admin/platform-settings/regions
{ "code": "APAC", "name": "Asia Pacific" }
```
```json
// 201
{ "id": 3, "code": "APAC", "name": "Asia Pacific", "enabled": true }
```

OpenAPI contract: not maintained separately — `PlatformAdministrationController` and this file are the source of truth ([C14](../../../01-business/roadmap/open-decisions.md#c14)).
