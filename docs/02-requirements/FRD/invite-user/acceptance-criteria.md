# Acceptance criteria — Invite user

| ID | Criterion |
|---|---|
| AC-1 | An organization administrator can invite; a plain member cannot. |
| AC-2 | An administrator can grant `INVITE_USERS` to a member; that member can then invite (as `MEMBER` only) and sees only their own invitations. |
| AC-3 | Removing the grant stops new invitations at once; `MANAGE_USERS` was never granted by it. |
| AC-4 | The email is sent with the link; the token is hashed, single use and ends on accept, revoke, expiry and resend. |
| AC-5 | A second invitation to the same pending email is refused; resend replaces the link. |
| AC-6 | An existing account signs in and accepts; a new person creates an account from the invitation and joins. |
| AC-7 | Pending invitations use no seat; capacity is checked on send and again on accept. |
| AC-8 | Active members, suspended members and people active elsewhere cannot join through an invitation; a removed member is re-admitted on the same row. |
| AC-9 | A suspended or closed organization cannot invite or accept. |
| AC-10 | A structure node can be chosen and is applied on accept; product access is untouched. |
| AC-11 | The Invitations tab lists, filters, resends and revokes; the platform administrator can read them. |
| AC-12 | Every event is in the audit log; the screens work in English and Spanish and pass the accessibility checks. |
