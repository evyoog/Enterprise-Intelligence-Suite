# Business rules — Organization MFA Policy

These rules are **already enforced by the backend**. They are documented here from the code, not newly defined. The cited class and method are the source of truth. `BR-IAM-<NNN>` numbers are placeholders until rule IDs are assigned.

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-IAM-<NNN>.1 | Only a caller with the `MANAGE_ORGANIZATION` permission can change the policy. The permission comes from the caller's standing organization role (seeded for `ORG_ADMIN`) or an active, approved ORGANIZATION-scope privileged-access grant for it. Otherwise 403 "You do not have permission to do this". | backend | `OrganizationSelfService#updateMfaPolicy` → `#requirePermission`; `RbacSeeder` (ORG_ADMIN permissions) |
| BR-IAM-<NNN>.2 | The caller must be an ACTIVE member of an organization. Otherwise the request fails with 404 "You are not a member of an organization". | backend | `OrganizationSelfService#resolveMembership` |
| BR-IAM-<NNN>.3 | The caller's organization must be in good standing. Otherwise the request fails with 403 "Your organization's account is not currently active". | backend | `OrganizationSelfService#resolveMembership`, `AuthorizationService#organizationInGoodStanding` |
| BR-IAM-<NNN>.4 | The organization is always the caller's own, resolved from the JWT. No organization id is accepted from the client. | backend | `OrganizationController` class javadoc, `OrganizationSelfService` class javadoc |
| BR-IAM-<NNN>.5 | Changing the setting never signs anyone out. It takes effect at each member's next fresh login. | backend | `OrganizationController#updateMfaPolicy` javadoc; `OrganizationSelfService#updateMfaPolicy` javadoc |
| BR-IAM-<NNN>.6 | At login the policy is satisfied if the token's `amr` claim contains `otp`, or if the caller's organization does not require MFA. A customer with no organization membership is never blocked by this policy. | backend | `MfaPolicyService#isMfaSatisfied` |
| BR-IAM-<NNN>.7 | When the policy is not satisfied, login is refused with 403 and the flag `organizationMfaRequired: true` in the error body. | backend | `AuthController#login`, `GlobalExceptionHandler#handleOrganizationMfaRequired` |
| BR-IAM-<NNN>.8 | Every change is recorded in the audit log as `MFA_POLICY_CHANGED` ("MFA requirement set to <value>"). | backend | `OrganizationSelfService#updateMfaPolicy` → `AuditService#recordSuccess` |
