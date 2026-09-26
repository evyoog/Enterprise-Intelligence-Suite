# Acceptance criteria — Claim Mapping

| ID | Criterion | Test cases |
|---|---|---|
| AC-1 | **Given** a SAML or OIDC provider **When** the administrator saves a mapping **Then** the names are stored trimmed, blank ones mean default, reset clears all four, and another organization's provider is refused | [TC-IAM-064](../../../../test-cases/functional/claim-mapping/TC-IAM-064.md) |
| AC-2 | **Given** a SAML provider mapped to custom attributes **When** a member signs in with both the custom and the default attributes **Then** the custom values are used | [TC-IAM-065](../../../../test-cases/functional/claim-mapping/TC-IAM-065.md) |
| AC-3 | **Given** a mapping whose attribute is missing from the assertion **When** a member signs in **Then** the defaults and then the fixed fallbacks are used | [TC-IAM-066](../../../../test-cases/functional/claim-mapping/TC-IAM-066.md) |
| AC-4 | **Given** an OIDC provider mapped to custom claims **When** a member signs in **Then** the custom claims are used, and unmapped details come from the OIDC defaults | [TC-IAM-067](../../../../test-cases/functional/claim-mapping/TC-IAM-067.md) |
| AC-5 | **Given** existing providers with no mapping **When** members sign in **Then** behaviour is unchanged | [TC-IAM-068](../../../../test-cases/functional/claim-mapping/TC-IAM-068.md) |
| AC-6 | **Given** the claim mapping dialog **When** it is open **Then** its text exists in `en.json` and `es.json`, it is keyboard operable, refusals are shown, and jest-axe reports no violations | [TC-IAM-069](../../../../test-cases/functional/claim-mapping/TC-IAM-069.md) |
