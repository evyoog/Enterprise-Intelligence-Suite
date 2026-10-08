# Screens: Invitations

**Requirement:** [REQ-TEN-008](../../02-requirements/FRD/invite-user/requirement.md)

1. **Invitations tab** (People & structure; visible to `ORG_ADMIN` and members with `INVITE_USERS`). Header button *Invite user*. Status filter and search. Table: email, status chip, role, structure node, invited by, invited at, expires at, accepted at, last resent. Row actions: PENDING Resend and Revoke (confirmation); EXPIRED, DECLINED, REVOKED Resend; ACCEPTED View details. A drawer shows the details and send count; no token.
2. **Invite user dialog.** Email, Organization role (Organization admin disabled with a reason for delegated inviters), Structure node (optional, shows the path), summary line ("expires in 7 days"), *Send invitation*. Server messages are shown in the dialog; a pending duplicate offers *Resend*.
3. **Member row.** Administrators see a *Can invite* switch for each member (not for administrators); the note says it does not grant member management.
4. **Public invitation page** `/invitations/:token`. States: account exists and signed out (summary and *Log in to accept*, returning here); no account (first name, last name, password, confirm); signed in with the right email (summary, *Accept*, *Decline*); signed in with another email (explanation and sign out); and panels for expired, revoked, declined, accepted, invalid, already a member, suspended, other organization, no seat, organization unavailable.
5. **Platform administrator.** Read-only *Pending invitations* table in an organization's Members tab.
