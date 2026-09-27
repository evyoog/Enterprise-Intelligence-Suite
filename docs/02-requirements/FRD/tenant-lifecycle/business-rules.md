# Business rules — Tenant Lifecycle

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-TEN-020 | Assigning a `regionId` that does not exist in `platform_region` is refused (404). A `null` regionId clears the assignment. | backend | REQ-TEN-004.2 |
| BR-TEN-021 | `assertSeatAvailable` returns immediately, before counting active members, when `organization.allowSeatOverage` is true. | backend | REQ-TEN-004.4 |
| BR-TEN-022 | `isOverLimit` is unaffected by `allowSeatOverage` — it always compares the active member count to `licensedSeats`. | backend | REQ-TEN-004.5 |
| BR-TEN-023 | Both `regionId` and `allowSeatOverage` are editable only through `AdminRegistrationService#updateOrganization` (platform-admin-only, `MANAGE_REGISTRATIONS`) — no organization self-service endpoint can set either. | backend | REQ-TEN-004.1, .3 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
