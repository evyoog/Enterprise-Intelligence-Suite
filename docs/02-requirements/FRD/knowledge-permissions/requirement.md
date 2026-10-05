# REQ-KNW-008 — Knowledge permissions

**Status:** Approved (2026-10-05, [C78](../../../01-business/roadmap/open-decisions.md#c78)) — built early on 2026-10-05
**Owner:** Product owner
**Decision:** [C71](../../../01-business/roadmap/open-decisions.md#c71)

| Field | Value |
|---|---|
| Sprint | [2027.1.1](../../../01-business/roadmap/sprints/SPRINT-2027.1.1.md) (planned; dates unchanged) |
| Requirement ID | REQ-KNW-008 |
| Application | [11 Training & Knowledge Management](../../../01-business/roadmap/applications/11-training-knowledge-management.md); [06 Identity & Access](../../../01-business/roadmap/applications/06-identity-access-management.md) (RBAC) |
| Priority | P0 |

## Summary
Only platform administrators and the persons a platform administrator assigns may create or upload knowledge content. Everyone else can only view content allowed by its audience (BR-KVS-001).

## Permissions (proposed — confirm)
| Permission | Who | Allows |
|---|---|---|
| Platform administrator (`ADMIN` role) | Platform admins | Every knowledge action, always |
| **Knowledge contributor** — new `KNOWLEDGE_CONTRIBUTE` | Persons the platform admin assigns | Create and edit drafts; upload videos, documents, images, audio and templates; edit metadata, transcripts and chapters; submit for review |
| **Knowledge publisher** — the existing `MANAGE_KNOWLEDGE_BASE`, renamed in the UI | Persons the platform admin assigns | Everything a contributor can do, plus review, approve, schedule, publish, unpublish, deprecate, archive, restore versions, delete, replace media, manage taxonomy and the knowledge search index |

Reuse: `MANAGE_KNOWLEDGE_BASE` already guards `/admin/knowledge-base/**` and is seeded for ADMIN (`RbacSeeder`), so it becomes the publisher permission instead of adding `KNOWLEDGE_PUBLISH`. Full search-index rebuild stays with `MANAGE_SEARCH` (C70). Both knowledge permissions are added to the ADMIN role.

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-KNW-008.1 | The two permissions exist as platform permissions; ADMIN holds both. | Must |
| REQ-KNW-008.2 | A platform administrator can create a platform role holding one or both permissions on the existing Roles & permissions screen (REQ-IAM-003). How the role reaches a person: **Not specified** (see Open questions). | Must |
| REQ-KNW-008.3 | Every knowledge create, upload-URL, complete-upload, update, submit, approve, schedule, publish, unpublish, deprecate, archive, restore, delete and replace endpoint checks the permission on the backend (403 otherwise), whatever the UI shows. | Must |
| REQ-KNW-008.4 | The Knowledge Management navigation and every admin entry point (edit links, "Create content" actions) are shown only to users holding a knowledge permission. | Must |
| REQ-KNW-008.5 | A contributor cannot publish, approve, delete published content or change another contributor's content in Review (publisher only). | Must |
| REQ-KNW-008.6 | Granting or removing a knowledge permission (role change) and every content action are written to the audit log (existing `AuditService`). | Must |
| REQ-KNW-008.7 | Customers, organization admins, members and partners can only view content (REQ-KNW-005, BR-KVS-001). | Must |

## Out of scope
- Organization admins publishing content for their own organization (default **no** — open question).
- A new permission system: none; platform roles and permissions are reused.

## Open questions
| # | Question | Blocks approval |
|---|---|---|
| 1 | Two permissions (contributor, publisher) as proposed, or one? | Yes |
| 2 | Reuse `MANAGE_KNOWLEDGE_BASE` as the publisher permission (proposed), or add `KNOWLEDGE_PUBLISH`? | Yes |
| 3 | How does a platform admin give a knowledge role to a person? Today platform roles come only from Keycloak client roles; no EIS screen assigns a platform role to one person. Options: assign in Keycloak; add a per-person assignment to the Roles screen (new feature); use Privileged access requests. | Yes |
| 4 | May organization admins publish content visible only to their own organization? Default: no. | No — default applies |
| 5 | Can a contributor edit Published content (creating a new draft version), or only their own drafts? Not specified. | No — confirm in review |


## Answers applied on 2026-10-05 (C78)
The product owner said "start develop the code" on 2026-10-05 without answering the open questions. The recommended answers were applied as defaults; each can still be changed.

- 1. Two permissions: contributor `KNOWLEDGE_CONTRIBUTE` (new) and publisher `MANAGE_KNOWLEDGE_BASE` (reused, no duplicate). ADMIN holds both.
- 3. A person gets a knowledge role through a Keycloak client role mapped to a platform role holding the permission (no new per-person screen).
- 4. Organization admins do not publish (default kept).
- 5. A contributor may start a new draft of Published or Deprecated content; content in Review, Approved or Scheduled is read-only for contributors.
