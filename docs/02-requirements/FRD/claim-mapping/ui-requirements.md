# UI requirements — Claim Mapping

| Screen | Where | Roles |
|---|---|---|
| "Claim mapping" dialog | A "Claim mapping" button on each SAML provider and each OIDC provider, `/organization/identity-federation` | Organization administrator |

Four optional fields (Email, First name, Last name, Display name), each showing the protocol's default names underneath. "Reset to defaults" clears all four. Backend messages are shown as returned and keep the dialog open. Text under `claimMapping` in `en.json` and `es.json`; jest-axe check in `ClaimMappingDialog.test.tsx`.
