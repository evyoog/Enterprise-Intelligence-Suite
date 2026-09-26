# REQ-IAM-007 — Configurable Claim Mapping

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-25

| Field | Value |
|---|---|
| Sprint | [2026.3.3](../../../01-business/roadmap/sprints/SPRINT-2026.3.3.md) |
| Requirement ID | REQ-IAM-007 |
| Application | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md) |
| Application code | `APP-IAM` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0: the application is MVP scope ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No. This feature makes no use of AI (proposed; confirmed when the FRD is approved, C12) |

## Source functions
Workbook functions from the sprint and application pages that this FRD covers:

| Function ID | Function | Application page |
|---|---|---|
| 06.04.01.03 | Map claims | [06 Identity & Access Management](../../../01-business/roadmap/applications/06-identity-access-management.md#feature-060401-sso) |

## Summary
Claim mapping becomes configurable per identity provider, for both SAML and OIDC, for the four details the code already reads: email, first name, last name and display name. The defaults equal the current behaviour, so existing SAML providers are unchanged. Role mapping is out of scope: federated users still join as `MEMBER` ([C23](../../../01-business/roadmap/open-decisions.md#c23)).

## Actors
- Organization administrator (configures the mapping per provider)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-IAM-007.1 | For each SAML and OIDC provider, an administrator can set which attribute or claim supplies email, first name, last name and display name. | P0 |
| REQ-IAM-007.2 | A provider with no custom mapping behaves exactly as today. | P0 |

## Out of scope
- Role mapping; federated users still join as `MEMBER` ([C23](../../../01-business/roadmap/open-decisions.md#c23))
- Mapping any detail other than the four listed

## Dependencies
- FRD [`saml-federation`](../saml-federation/requirement.md) and FRD [`oidc-federation`](../oidc-federation/requirement.md).
- Current fixed mapping in `SamlAuthenticationService`.

## Decisions ([C28](../../../01-business/roadmap/open-decisions.md#c28), product owner, 2026-09-26)
The open questions are resolved:
- Default OIDC claims: `email`, `given_name`, `family_name`, `name`.
- A configured name is tried first, then the defaults.
- Fallbacks stay fixed (not configurable).
