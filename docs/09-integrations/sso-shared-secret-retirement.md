# Retiring `INTERNAL_SSO_SHARED_SECRET` (decision Q13)

**Status:** plan only. Separate change, scheduled after the synchronization is in production. Not started.

## Today
The platform and each tool call each other's `/internal/sso/token` and `/internal/sso/logout` with the header `X-Internal-Sso-Secret`, one secret shared by every app (`INTERNAL_SSO_SHARED_SECRET`; the platform's `application.yml` still has a committed development default). Anyone who knows it can ask any app for a token for any session id it can guess or learn.

## Target
Each app calls the others with its **own Keycloak service client** (client credentials), exactly like the synchronization: `eis-bridge`, `thittam-bridge`, … The receiver checks the token's `azp` against an allow-list and the audience, and the secret header goes away.

## Steps (each deployable and reversible)
1. **Receiver accepts both.** Add the bearer-token check beside the header check in `/internal/sso/*` on the platform and on the Macro Planner (a call is allowed by either). No behaviour change.
2. **Clients created.** Keycloak service clients per app with an Audience mapper for the receiver (as in the [Keycloak guide](../../deployment/keycloak-sync-clients.md)); secrets in the secrets manager.
3. **Callers switch.** Each app's partner client sends the bearer token and stops sending the header. Watch for 401s.
4. **Receiver refuses the header.** Remove the header check; delete `INTERNAL_SSO_SHARED_SECRET` from the code, the configuration and every environment.
5. **Rotate** anything that ever shared the old secret (it is in git history).

## Done when
No environment variable named `INTERNAL_SSO_SHARED_SECRET` exists, `/internal/sso/*` answers 401 without a valid service-client token, and the Macro Planner's `platform.sso.shared-secret` key is gone.
