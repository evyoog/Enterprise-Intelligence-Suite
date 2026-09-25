# Business rules — SAML Federation — Edit Provider

These rules are **already enforced by the backend**. They are documented here from the code, not newly defined. The cited class and method are the source of truth.

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-IAM-005.1 | Managing SAML providers requires `MANAGE_ORGANIZATION` in the caller's own organization. Otherwise 403 "You do not have permission to do this". | backend | `OrganizationSamlProviderController#requireOrgId` → `OrganizationSelfService#requireOrganizationManagement` |
| BR-IAM-005.2 | The provider must belong to the caller's organization. Otherwise the generic 403 "You do not have permission to do this" is returned, so the backend never confirms that the id exists elsewhere. An unknown id returns 404 "SAML provider not found". | backend | `SamlProviderService#findOwnedOrThrow` |
| BR-IAM-005.3 | The name is changed only if a non-blank `name` is sent. | backend | `SamlProviderService#update` |
| BR-IAM-005.4 | Connection details are changed only if at least one of `metadataXml`, `entityId`, `ssoUrl` or `certificatePem` is sent. | backend | `SamlProviderService#update` |
| BR-IAM-005.5 | If `metadataXml` is sent, it is checked for unsafe XML and parsed. It must contain an IdP entity id, an SSO URL and a signing certificate. Otherwise 400 with the specific missing item, or "Could not parse the provided IdP metadata: …". | backend | `SamlProviderService#applyFields`, `SamlXmlSecurity#rejectUnsafeXml` |
| BR-IAM-005.6 | Without metadata XML, all three of `entityId`, `ssoUrl` and `certificatePem` are required (400 "Provide either metadataXml, or all three of entityId/ssoUrl/certificatePem."). `ssoUrl` must be a valid absolute URL, and `certificatePem` must parse as an X.509 certificate. Manual details replace any stored metadata. | backend | `SamlProviderService#applyFields` |
| BR-IAM-005.7 | Editing does not change whether the provider is enabled. At most one provider per organization is enabled, and that is handled by enable and disable (existing). | backend | `SamlProviderService#update`, `SamlProviderService#setEnabled` |
| BR-IAM-005.8 | Every edit is recorded in the audit log as `SAML_PROVIDER_UPDATED`. | backend | `SamlProviderService#update` → `AuditService#recordSuccess` |
| BR-IAM-005.9 | The caller must be an ACTIVE member of an organization. Otherwise the request fails with 404 "You are not a member of an organization". | backend | `OrganizationSelfService#resolveMembership` |
| BR-IAM-005.10 | The caller's organization must be in good standing. Otherwise the request fails with 403 "Your organization's account is not currently active". | backend | `OrganizationSelfService#resolveMembership`, `AuthorizationService#organizationInGoodStanding` |
