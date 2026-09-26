# REQ-IAM-006 — OIDC Identity-Provider Federation

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-25

| Field | Value |
|---|---|
| Sprint | [2026.3.3](../../../01-business/roadmap/sprints/SPRINT-2026.3.3.md) |
| Requirement ID | REQ-IAM-006 |
| Application | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md) |
| Application code | `APP-IAM` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0: the application is MVP scope ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No |

## Source functions
Workbook functions from the sprint and application pages that this FRD covers:

| Function ID | Function | Application page |
|---|---|---|
| 06.04.01.02 | Configure OIDC | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060401-sso) |
| 06.04.01.04 | Test federation (OIDC) | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060401-sso) |

## Summary
Per-organization OpenID Connect (OIDC) identity-provider federation, built in the backend `federation` module and mirroring the existing SAML design: per-organization providers, enable and disable, and a test action ([C22](../../../01-business/roadmap/open-decisions.md#c22)). Federated users join as `MEMBER` ([C23](../../../01-business/roadmap/open-decisions.md#c23)).

## Actors
- Organization administrator (configures providers)
- Organization member (signs in through the provider)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-IAM-006.1 | An organization administrator can create, list, edit and delete OIDC identity providers for their organization. | P0 |
| REQ-IAM-006.2 | An organization administrator can enable and disable a provider. | P0 |
| REQ-IAM-006.3 | An organization administrator can test a provider. | P0 |
| REQ-IAM-006.4 | Members of the organization can sign in through its enabled OIDC provider. | P0 |
| REQ-IAM-006.5 | A federated user joins the organization as `MEMBER`. | P0 |
| REQ-IAM-006.6 | Claim mapping follows FRD [`claim-mapping`](../claim-mapping/requirement.md). | P0 |

## Out of scope
- SCIM provisioning ([C22](../../../01-business/roadmap/open-decisions.md#c22): not a function in [PO] or [WB])
- Role mapping from identity-provider claims ([C23](../../../01-business/roadmap/open-decisions.md#c23))

## Dependencies
- The existing SAML design in `SamlProviderService`, `SamlAuthenticationService` and `OrganizationSamlProviderController`, which this feature mirrors.

## Decisions ([C27](../../../01-business/roadmap/open-decisions.md#c27), product owner, 2026-09-26)
The open questions are resolved:
- Provider fields: name, issuer (discovery) URL, client ID, client secret, scopes (default `openid email profile`).
- Login flow: same as SAML — organization code, redirect to the provider, callback creates the normal platform session; new users join as `MEMBER`.
- Client secret: encrypted with `TotpSecretCipher` (AES-GCM), never returned after saving.
- At most one enabled provider per organization across SAML and OIDC together.
- Endpoints: `/organization/me/oidc-providers`, mirroring SAML.
- The organization MFA policy applies after OIDC sign-in too ([C29](../../../01-business/roadmap/open-decisions.md#c29)).
