# REQ-INT-001 — API management

**Status:** Draft
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Not yet approved
**Decision:** [C61](../../../01-business/roadmap/open-decisions.md#c61) (answer to D12, option B)
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
| 13.01.01.04 | Deprecate API | No. A deprecation policy is Not specified |
| 13.01.02.01 | Authenticate API | Yes: API keys (.1) besides the existing Keycloak JWT |
| 13.01.02.02 | Authorize API | Yes: a key acts with its owner's permissions (.1) |
| 13.01.02.03 | Rate limit API | Yes (.2) |
| 13.01.02.04 | Monitor API | Partly: per-key last-used time and request count (.4) |

## Summary
API management inside the existing backend ([C61](../../../01-business/roadmap/open-decisions.md#c61)): users create API keys for their integrations, every request is rate-limited, and the API has an explicit version. A cloud API gateway may take some of this over at deployment (D42 not decided).

## Actors
- **Key owner** (signed-in user with a Vyoog account — Open question 1): creates, lists and revokes their own keys.
- **Integration** (machine): calls the API with the header `X-API-Key`.
- **Platform administrator** (`MANAGE_INTEGRATIONS`): sees every key's usage.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-INT-001.1 | **API keys:** an authorized user can create an API key (name, optional expiry), see its prefix and creation date, and revoke it. The full key is shown **once** at creation and stored only as a hash. Calls authenticated with an API key act with the permissions of the key's owner. | Must |
| REQ-INT-001.2 | **Rate limits:** requests are limited per API key and per signed-in user, and per IP address for unauthenticated requests. When exceeded, the response is HTTP **429** with a `Retry-After` header. | Must |
| REQ-INT-001.3 | **Versioning:** the API has an explicit version. Existing frontend calls keep working. | Must |
| REQ-INT-001.4 | **Usage visibility:** a platform administrator can see, per key, last-used time and request count. | Should |
| REQ-INT-001.5 | Creating a key, revoking a key and using a revoked key are audited. | Must |

## Engineering defaults (built 2026-10-03, until the open questions are answered)
| Topic | Default | Where |
|---|---|---|
| Who gets keys (OQ 1) | Any signed-in user with a Vyoog account, for their own use; at most 10 active keys each. Placement: **Account → Security → API keys** | `ApiKeyService`, `SecuritySettingsPage` |
| Key permissions | The owner's Keycloak roles are copied onto the key at creation, **except platform administration** (`ROLE_ADMIN`): admin endpoints are not reachable with a key until Open questions 1 and 4 are answered. Organization permissions are read live from the owner's membership | `ApiKeyAuthenticationFilter` |
| Key format | `eis_<8-character prefix>_<40 random characters>`; stored: the prefix and a SHA-256 hash | `ApiKeyService` |
| Header | `X-API-Key`; ignored when an `Authorization` header is present | `ApiKeyAuthenticationFilter` |
| Rate limits (OQ 2) | Per key 120, per signed-in user 300, per IP (unauthenticated) 60 requests per minute; fixed one-minute windows, counted in each backend instance's memory. Every response has `X-RateLimit-Limit` and `X-RateLimit-Remaining` | `RateLimitFilter`, `app.rate-limit.*` |
| Versioning (OQ 3) | Path prefix: every endpoint is also served at `/v1/…`; today's paths stay as aliases. Every response has the header `API-Version: 1` | `ApiVersionFilter` |
| Scopes (OQ 4) | None | — |
| Expired key | Rejected like a revoked key (401) | `ApiKeyService` |
| Keys creating keys | Not allowed: a key cannot create another key (403); keys are created when signed in | `ApiKeyController` |
| Rate-limit configuration | `app.rate-limit.per-key`, `per-user`, `per-ip` in `application.yml` | `ApiManagementConfig` |

## Out of scope
- Registering or proxying third-party APIs; a developer portal; deprecation policy.
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
| # | Question | Blocks approval |
|---|---|---|
| 1 | **Who gets API keys:** organization admins (for their integrations), partners (application 14), hosted products (they already use the internal shared secret), or developers in general? | Yes |
| 2 | **Rate-limit values:** requests per minute per key, per user and per IP. Different limits per plan? | No — confirm in review |
| 3 | **Versioning scheme:** a path prefix such as `/v1/…` (with today's paths kept as aliases), or a request header? | No — confirm in review |
| 4 | Do API keys carry **scopes** narrower than the owner's permissions? | No — confirm in review |
