# Business rules — MFA Recovery

Decided with the product owner on 2026-09-26 ([C30](../../../01-business/roadmap/open-decisions.md#c30)).

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-IAM-008.1 | An organization administrator needs `MANAGE_USERS`, and the member must belong to their organization. Otherwise 403 "You do not have permission to do this" (same response for another organization's member). | backend | `OrganizationSelfService#resetMemberMfa` → `#requirePermissionOnMember` |
| BR-IAM-008.2 | A platform administrator needs `MANAGE_REGISTRATIONS`. An unknown email returns 404 "No account uses that email address." | backend | `SecurityConfig`, `AdminRegistrationService#resetMfaByEmail` |
| BR-IAM-008.3 | A reset of your own account is refused: 400 "You cannot reset your own two-factor authentication here. Use Security settings instead." | backend | `PlatformMfaService#resetByAdmin` |
| BR-IAM-008.4 | A user with no authenticator: 400 "This user has not set up two-factor authentication." | backend | `PlatformMfaService#resetByAdmin` |
| BR-IAM-008.5 | A reset deletes the authenticator, all recovery codes and any held sign-in challenge of the user. | backend | `PlatformMfaService#resetByAdmin` |
| BR-IAM-008.6 | The user gets a SECURITY / WARNING notification ("Two-factor authentication was reset") and the reset is audited as `MFA_RESET_BY_ADMIN` with the actor and organization. | backend | `PlatformMfaService#resetByAdmin` |
