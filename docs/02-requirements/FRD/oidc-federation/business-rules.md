# Business rules — OIDC Identity-Provider Federation

These rules come from the decisions recorded in [open-decisions.md](../../../01-business/roadmap/open-decisions.md) on 2026-09-25. There is no backend code for them yet. Rules marked **(to confirm)** mirror the existing SAML design and must be confirmed when the FRD is approved.

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-IAM-006.1 | Providers are per organization and are built in the backend `federation` module. | backend | [C22](../../../01-business/roadmap/open-decisions.md#c22) |
| BR-IAM-006.2 | Client secrets are never hard-coded and are encrypted at rest. | backend | [C22](../../../01-business/roadmap/open-decisions.md#c22); `CLAUDE.md` rule 7 |
| BR-IAM-006.3 | A federated user joins the organization as `MEMBER`; no identity-provider claim can grant `ORG_ADMIN`. | backend | [C23](../../../01-business/roadmap/open-decisions.md#c23); same rule as `SamlAuthenticationService#ensureActiveMembership` |
| BR-IAM-006.4 | **(to confirm)** Managing providers requires `MANAGE_ORGANIZATION` in the caller's own organization, as for SAML. | backend | Mirrors `OrganizationSamlProviderController#requireOrgId` |
| BR-IAM-006.5 | **(to confirm)** A provider of another organization returns the generic 403 "You do not have permission to do this", as for SAML. | backend | Mirrors `SamlProviderService#findOwnedOrThrow` |
| BR-IAM-006.6 | **(to confirm)** Every create, update, enable, disable and delete is audited, as for SAML. | backend | Mirrors `SamlProviderService` |
