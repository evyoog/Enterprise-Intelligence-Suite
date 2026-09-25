# REQ-IAM-005 — SAML Federation — Edit Provider

**Status:** Draft
**BRD:** Not specified
**Owner:** Not specified
**Approved by / on:** Not specified / Not specified

| Field | Value |
|---|---|
| Sprint | [2026.3.3](../../../01-business/roadmap/sprints/SPRINT-2026.3.3.md) |
| Requirement ID | REQ-IAM-005 |
| Application | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md) |
| Application code | `APP-IAM` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0: the application is MVP scope ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No. This feature makes no use of AI (proposed; confirmed when the FRD is approved, C12) |

## Source functions
Workbook functions from the sprint and application pages that this FRD covers:

| Function ID | Function | Application page |
|---|---|---|
| 06.04.01.01 | Configure SAML (edit provider) | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060401-sso) |

## Summary
An organization administrator can edit an existing SAML identity provider for their organization: its name, and its connection details, either as IdP metadata XML or as entity id, SSO URL and signing certificate. Creating, enabling, disabling, deleting and testing providers are already built in `OrganizationSamlProvidersPage`. This feature adds the Edit action, reusing the existing create form.

## Actors
- Organization administrator (holding `MANAGE_ORGANIZATION`)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-IAM-005.1 | The administrator can open an existing provider for editing, with its current values filled in. | P0 |
| REQ-IAM-005.2 | The administrator can change the provider name. | P0 |
| REQ-IAM-005.3 | The administrator can replace the connection details with IdP metadata XML, or with entity id, SSO URL and certificate. | P0 |
| REQ-IAM-005.4 | When the backend refuses the change, its message is shown. | P0 |

## Out of scope
- Create, enable, disable, delete and test (already built)
- OIDC identity providers (FRD [`oidc-federation`](../oidc-federation/requirement.md), [C22](../../../01-business/roadmap/open-decisions.md#c22)) and configurable claim mapping (FRD [`claim-mapping`](../claim-mapping/requirement.md), [C23](../../../01-business/roadmap/open-decisions.md#c23))

## Dependencies
- Existing endpoint `PUT /organization/me/saml-providers/{id}`; existing client `samlApi.update` in `frontend/src/api/samlApi.ts`.
