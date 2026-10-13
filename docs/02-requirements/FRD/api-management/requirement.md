# REQ-INT-001 — API management

**Status:** Draft
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved
**Decision:** [C61](../../../01-business/roadmap/open-decisions.md#c61) (answer to D12, option B); open questions answered by [C92](../../../01-business/roadmap/open-decisions.md#c92) (API-1 to API-5)
**Built:** 2026-10-03 at the product owner's request, with the engineering defaults below. Test cases: [TESTPLAN-INT-001](../../../../test-cases/functional/api-management/TESTPLAN-INT-001.md).

| Field | Value |
|---|---|
| Sprint | [2026.4.2](../../../01-business/roadmap/sprints/SPRINT-2026.4.2.md) |
| Requirement ID | REQ-INT-001 |
| Application | [13 Integration & API Platform](../../../01-business/roadmap/applications/13-integration-api-platform.md) |
| Application code | `APP-INT` |
| Priority | P0 |
| AI required | No |

## Source functions
| Function ID | Function | Covered here |
|---|---|---|
| 13.01.01.01 | Register API | No. There is one platform API; registering third-party APIs is Not specified |
| 13.01.01.02 | Publish API | Partly: the OpenAPI document already published by springdoc; no developer portal |
| 13.01.01.03 | Version API | Yes (.3) |
| 13.01.01.04 | Deprecate API | Yes (.6, C92): deprecation headers with a removal date |
| 13.01.02.01 | Authenticate API | Yes: API keys (.1) besides the existing Keycloak JWT |
| 13.01.02.02 | Authorize API | Yes: a key acts as an administrator of its organization, read-only or read-write (.1, .7) |
| 13.01.02.03 | Rate limit API | Yes (.2) |
| 13.01.02.04 | Monitor API | Partly: per-key last-used time and request count (.4) |

## Summary
API management inside the existing backend ([C61](../../../01-business/roadmap/open-decisions.md#c61)): users create API keys for their integrations, every request is rate-limited, and the API has an explicit version. A cloud API gateway may take some of this over at deployment (D42 not decided).

## Actors
- **Organization administrator** (*Manage organization*, C92 API-1 B): creates, lists and revokes the organization's keys. Keys belong to the organization.
- **Integration** (machine): calls the API with the header `X-API-Key`.
- **Platform administrator** (`MANAGE_INTEGRATIONS`): sees every key's usage.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-INT-001.1 | **API keys:** an organization administrator can create an API key for the organization (name, scope, optional expiry), see its prefix, scope, creator and creation date, and revoke it. The full key is shown **once** at creation and stored only as a hash. The key **belongs to the organization** and keeps working when its creator leaves. Calls authenticated with an API key act as an administrator of the organization (C92). | Must |
| REQ-INT-001.2 | **Rate limits:** requests are limited per API key and per signed-in user, and per IP address for unauthenticated requests. When exceeded, the response is HTTP **429** with a `Retry-After` header. | Must |
| REQ-INT-001.3 | **Versioning:** the API has an explicit version. Existing frontend calls keep working. | Must |
| REQ-INT-001.4 | **Usage visibility:** a platform administrator can see, per key, last-used time and request count. | Should |
| REQ-INT-001.5 | Creating a key, revoking a key and using a revoked key are audited. | Must |
| REQ-INT-001.6 | **Deprecation (C92, API-5 B):** when an endpoint is deprecated, every response under its path prefix carries `Deprecation: true`, `Sunset` (the removal date) and, if configured, `Link: …; rel="successor-version"`. Configured with `app.api.deprecations`; empty today. | Should |
| REQ-INT-001.7 | **Scopes (C92, API-4 B):** a key is **read-only** (`GET`, `HEAD`, `OPTIONS`) or **read-write**. A read-only key that tries any other method gets 403 `API_KEY_READ_ONLY`. | Must |

## Engineering defaults (built 2026-10-03; changed by C92)
| Topic | Default | Where |
|---|---|---|
| Who gets keys (OQ 1, **API-1 B**) | Organization administrators (*Manage organization*), for the organization's integrations; at most 10 active keys **per organization**. Placement: **Organization → Sign-in security → API keys**. A key cannot create or revoke keys | `OrganizationApiKeyService`, `OrganizationSecurityPage` |
| Who a key acts as | The organization's **oldest active administrator who can sign in**; none left: 401 and `API_KEY_NO_ADMINISTRATOR` audited. The administrator's roles are copied **except platform administration** (`ROLE_ADMIN`): admin endpoints are not reachable with a key | `ApiKeyService`, `ApiKeyAuthenticationFilter` |
| Legacy personal keys | Keep working as read-write until revoked; cannot be created any more (403). V032 moved the keys of active administrators to their organization. Listed on Account → Security only while they exist | `ApiKeyController`, `ApiKeysSection` |
| Key format | `eis_<8-character prefix>_<40 random characters>`; stored: the prefix and a SHA-256 hash | `ApiKeyService` |
| Header | `X-API-Key`; ignored when an `Authorization` header is present | `ApiKeyAuthenticationFilter` |
| Rate limits (OQ 2, **API-2 B**) | Per key 120, per signed-in user 300, per IP (unauthenticated) 60 requests per minute; fixed one-minute windows, counted in each backend instance's memory. Every response has `X-RateLimit-Limit` and `X-RateLimit-Remaining`. Higher limits for higher plans: later | `RateLimitFilter`, `app.rate-limit.*` |
| Versioning (OQ 3, **API-3 A**) | Path prefix: every endpoint is also served at `/v1/…`; today's paths stay as aliases. Every response has the header `API-Version: 1` | `ApiVersionFilter` |
| Scopes (OQ 4, **API-4 B**) | `READ` and `READ_WRITE`; a key without a scope is read-only; legacy personal keys are read-write | `ApiKeyAuthenticationFilter` |
| Deprecation (**API-5 B**) | `app.api.deprecations[]` with `path-prefix`, `sunset` (date) and optional `successor`; empty until `/v2` exists | `ApiDeprecationFilter` |
| Expired key | Rejected like a revoked key (401) | `ApiKeyService` |

## Out of scope
- Registering or proxying third-party APIs; a developer portal; plan-based rate limits; key rotation; scopes finer than read/write.
- A cloud API gateway (D42).
- Hosted products' internal calls: they keep the internal shared secret (`INTERNAL_SSO_SHARED_SECRET`).

## Dependencies
Spring Security (filters before the JWT resource server), audit log, `MANAGE_INTEGRATIONS` permission (REQ-INT-002).

## Where each part of this FRD lives
| Part | Location |
|---|---|
| Requirement, business rules, workflow, acceptance criteria | this folder |
| Screens | [ui-requirements.md](ui-requirements.md) → [api-keys.md](../../../05-ui/screen-requirements/api-keys.md) |
| API | [api-requirements.md](api-requirements.md) → [api-management.md](../../../06-api/api-requirements/api-management.md) |
| Data model | [api-management.md](../../../07-database/data-model/api-management.md) |

## Open questions
All four answered on 2026-10-13 ([C92](../../../01-business/roadmap/open-decisions.md#c92)): 1 → B (organization administrators; keys belong to the organization), 2 → B (limits stay; higher limits for higher plans later), 3 → A (path prefix), 4 → B (read-only and read-write). Deprecation (13.01.01.04) → B (headers with a removal date). **Not specified:** plan-based limits, key rotation, finer scopes.
