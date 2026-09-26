# Business rules — Member Lifecycle & Access Review

| ID | Rule | Enforced in | Source |
|----|------|-------------|--------|
| BR-TEN-010 | Suspend and Remove are refused (400) if the target is the organization's last active `ORG_ADMIN` — same guard as `changeMemberRole`. Reactivate is never blocked by this rule (it only ever increases the number of active admins). | backend | REQ-TEN-002.4 |
| BR-TEN-011 | Suspend sets status to `SUSPENDED` and stamps `deactivatedAt`; the seat is freed the same way Remove already frees it. | backend | REQ-TEN-002.1 |
| BR-TEN-012 | Reactivate is refused (400) for an `INACTIVE` (removed) member — removal is one-way; refused (409, seat-limit) if the organization has no free seat. | backend | REQ-TEN-002.2 |
| BR-TEN-013 | `resolveMembership` (the gate on every self-service/authenticated action) only resolves an `ACTIVE` row — a `SUSPENDED` member fails it exactly like an `INACTIVE` one, immediately. | backend | REQ-TEN-002.5 |
| BR-TEN-014 | Reviewing access sets `lastReviewedAt`/`lastReviewedByCustomerId` and changes nothing else about the member. | backend | REQ-TEN-002.6 |
| BR-TEN-015 | A status change notifies the affected member and is recorded in the audit log (`MEMBER_STATUS_CHANGED`); a review is recorded (`MEMBER_ACCESS_REVIEWED`) but does not notify the member. | backend | REQ-TEN-002.7 |
| BR-TEN-016 | All four actions (suspend/reactivate/remove/review) are refused (404, same message as a not-found) for a member of a different organization than the caller's own — same pattern as `requirePermissionOnMember`. | backend | REQ-TEN-002.1–.3, .6 |

Rules shared with other features belong in `docs/03-business-rules/` and are referenced here by ID.
