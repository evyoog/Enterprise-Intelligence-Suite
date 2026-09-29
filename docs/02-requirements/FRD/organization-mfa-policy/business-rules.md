# Business rules — Organization MFA Policy

These rules are **already enforced by the backend**. They are documented here from the code, not newly defined. The cited class and method are the source of truth.

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-IAM-001.1 | Only a caller with the `MANAGE_ORGANIZATION` permission can change the policy. The permission comes from the caller's standing organization role (seeded for `ORG_ADMIN`) or an active, approved ORGANIZATION-scope privileged-access grant for it. Otherwise 403 "You do not have permission to do this". | backend | `OrganizationSelfService#updateMfaPolicy` → `#requirePermission`; `RbacSeeder` (ORG_ADMIN permissions) |
| BR-IAM-001.2 | The caller must be an ACTIVE member of an organization. Otherwise the request fails with 404 "You are not a member of an organization". | backend | `OrganizationSelfService#resolveMembership` |
| BR-IAM-001.3 | The caller's organization must be in good standing. Otherwise the request fails with 403 "Your organization's account is not currently active". | backend | `OrganizationSelfService#resolveMembership`, `AuthorizationService#organizationInGoodStanding` |
| BR-IAM-001.4 | The organization is always the caller's own, resolved from the JWT. No organization id is accepted from the client. | backend | `OrganizationController` class javadoc, `OrganizationSelfService` class javadoc |
| BR-IAM-001.5 | Changing the setting never signs anyone out. It takes effect at each member's next fresh login. | backend | `OrganizationController#updateMfaPolicy` javadoc; `OrganizationSelfService#updateMfaPolicy` javadoc |
| BR-IAM-001.6 | At login the policy is satisfied if the token's `amr` claim contains `otp`, or if the caller's organization does not require MFA. A customer with no organization membership is never blocked by this policy. | backend | `MfaPolicyService#isMfaSatisfied` |
| BR-IAM-001.7 | (Changed 2026-09-26, C29.) When the policy is not satisfied, the sign-in is held: 401 with `platformMfaEnrollmentRequired: true` and `mfaEnrollmentChallengeId`. The member sets up an authenticator (`POST /auth/mfa/enroll/start`, `/complete`); only a valid first code turns the held sign-in into a session. | backend | `SignInMfaGate#check`, `AuthController#login`, `#startSignInEnrollment`, `#completeSignInEnrollment` |
| BR-IAM-001.9 | A member who already has a platform authenticator always gets the code step (password and federated sign-in). | backend | `SignInMfaGate#check` |
| BR-IAM-001.10 | On SAML (and OIDC) sign-in a Keycloak `amr` value never satisfies the policy; the platform step is always required when the organization requires MFA. The browser is sent to `/?mfaEnroll=<id>` or `/?mfaChallenge=<id>`; no session cookie is set until the step passes. | backend | `SamlAuthenticationService#handleAcs`, `SignInMfaGate#check` |
| BR-IAM-001.11 | A held sign-in is single-use, expires with the challenge TTL, allows the configured number of wrong codes, and an ENROLL challenge cannot be used as a VERIFY challenge or the reverse. | backend | `PlatformMfaService#requireUsableChallenge` |
| BR-IAM-001.8 | Every change is recorded in the audit log as `MFA_POLICY_CHANGED` ("MFA requirement set to <value>"). | backend | `OrganizationSelfService#updateMfaPolicy` → `AuditService#recordSuccess` |
