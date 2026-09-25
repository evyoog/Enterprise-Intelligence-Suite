# Business rules — Configurable Claim Mapping

Rules 2, 3 and 5 are **already enforced by the backend** (cited class). Rules 1 and 4 come from decision C23 in [open-decisions.md](../../../01-business/roadmap/open-decisions.md).

| ID | Rule | Enforced in | Source |
|---|---|---|---|
| BR-IAM-007.1 | Only email, first name, last name and display name are mappable. Role mapping is out of scope. | backend | [C23](../../../01-business/roadmap/open-decisions.md#c23) |
| BR-IAM-007.2 | The default SAML attribute names are the current ones. Email: `email`, `emailaddress`, `mail`, `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress`, `urn:oid:0.9.2342.19200300.100.1.3`. First name: `firstname`, `givenname`, `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/givenname`, `urn:oid:2.5.4.42`. Last name: `lastname`, `surname`, `sn`, `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/surname`, `urn:oid:2.5.4.4`. Display name: `displayname`, `name`, `cn`. | backend | `SamlAuthenticationService` (`EMAIL_ATTRIBUTES`, `FIRST_NAME_ATTRIBUTES`, `LAST_NAME_ATTRIBUTES`, `DISPLAY_NAME_ATTRIBUTES`) |
| BR-IAM-007.3 | Current fallbacks: if no email attribute is present, the NameID is used when it contains "@"; otherwise login fails with a message asking for an email attribute. If no first name is present, the display name is used, then the part of the email before "@". If no last name is present, "SSO User" is used. | backend | `SamlAuthenticationService` (login handling) |
| BR-IAM-007.4 | Existing SAML providers keep today's behaviour unless their mapping is changed. | backend | [C23](../../../01-business/roadmap/open-decisions.md#c23) |
| BR-IAM-007.5 | Federated users join as `MEMBER`, never `ORG_ADMIN`. | backend | `SamlAuthenticationService#ensureActiveMembership`; [C23](../../../01-business/roadmap/open-decisions.md#c23) |
