# REQ-<APP-CODE>-<NNN> — SAML Federation — Edit Provider

**Status:** Draft
**BRD:** Not specified
**Owner:** Not specified
**Approved by / on:** Not specified / Not specified

| Field | Value |
|---|---|
| Sprint | [2026.3.3](../../../01-business/roadmap/sprints/SPRINT-2026.3.3.md) |
| Application | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md) |
| Application code | Not assigned. `<APP-CODE>` is left as-is; application codes are an open decision ([open-decisions.md](../../../01-business/roadmap/open-decisions.md)) |

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
| REQ-<APP-CODE>-<NNN>.1 | The administrator can open an existing provider for editing, with its current values filled in. | Not specified |
| REQ-<APP-CODE>-<NNN>.2 | The administrator can change the provider name. | Not specified |
| REQ-<APP-CODE>-<NNN>.3 | The administrator can replace the connection details with IdP metadata XML, or with entity id, SSO URL and certificate. | Not specified |
| REQ-<APP-CODE>-<NNN>.4 | When the backend refuses the change, its message is shown. | Not specified |

## Out of scope
- Create, enable, disable, delete and test (already built)
- OIDC identity providers and configurable claim mapping (Not specified; see [open-decisions.md](../../../01-business/roadmap/open-decisions.md))

## Dependencies
- Existing endpoint `PUT /organization/me/saml-providers/{id}`; existing client `samlApi.update` in `frontend/src/api/samlApi.ts`.
