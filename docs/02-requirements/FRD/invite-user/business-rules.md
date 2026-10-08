# Business rules — Invite user

| ID | Rule |
|---|---|
| BR-INV-001 | May invite: an active `ORG_ADMIN`, or an active member whose individual grant of `INVITE_USERS` is on. Checked on the server for every call. |
| BR-INV-002 | Only an `ORG_ADMIN` can grant or remove `INVITE_USERS`, only for another active `MEMBER` of the same organization. An `ORG_ADMIN` already has it. Nobody changes their own grant. |
| BR-INV-003 | Only an `ORG_ADMIN` can invite with the role `ORG_ADMIN`. A delegated inviter invites as `MEMBER`. |
| BR-INV-004 | A delegated inviter sees, resends and revokes only invitations they sent. An `ORG_ADMIN` sees all of the organization. |
| BR-INV-005 | Email is trimmed and lower-cased before any comparison or storage. One PENDING invitation per (organization, normalized email). |
| BR-INV-006 | Lifetime is 7 days from sending or resending. After that the invitation cannot be accepted or declined, and its state is EXPIRED. |
| BR-INV-007 | The token is shown once in the email link. Only its SHA-256 hash is stored. Resend, accept, decline, revoke and expiry end the old link. A lookup of an invalid or unknown token answers one generic "invalid or no longer available" message. |
| BR-INV-008 | Re-admission: accepting a new invitation reactivates the person's earlier, removed membership row in the same organization with the invited role; a removed member is never re-admitted any other way (REQ-TEN-002 stays one-way). |
| BR-INV-009 | SUSPENDED members are never invited; use Reactivate. ACTIVE members cannot be invited. |
| BR-INV-010 | Seat capacity: PENDING invitations are not counted. Send: refuse when there is clearly no capacity. Accept: the authoritative check (`assertSeatAvailable`), refused without creating or activating a membership. |
| BR-INV-011 | Organization state: registration status not CANCELLED or EXPIRED and lifecycle ACTIVE, at send and at accept. |
| BR-INV-012 | A person can be an ACTIVE member of one organization only (this sprint). The other-organization refusal happens on accept only, so an inviter cannot probe which accounts belong elsewhere. |
| BR-INV-013 | The accepting account's normalized email must equal the invited email. |
| BR-INV-014 | A structure node on an invitation must be an active node of the same organization when sent. On accept it is applied if it still exists and is active; otherwise the member is left unplaced. The node grants no permission. |
| BR-INV-015 | An invitation never grants product access and never changes personal subscriptions, invoices or other customer data. |
| BR-INV-016 | Removing `INVITE_USERS` does not revoke invitations already sent; an administrator can revoke them. |
| BR-INV-017 | A new account is created only when no customer with that email exists. The new customer gets first and last name from the form, status COMPLETED, an enabled Keycloak user, and the organization membership. |
