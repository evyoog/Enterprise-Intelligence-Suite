# Test plan — REQ-TEN-008 Invite user

Backend: `InvitationServiceTest` (SpringBootTest, H2, fake Keycloak and email) and `LayeredArchitectureTest`. Frontend: `InvitationPage.test.tsx`, `OrganizationInvitationsCard.test.tsx`, `OrganizationMembersCard.test.tsx`, `OrganizationMembersPage.test.tsx`, `AdminOrganizationDetailPage.test.tsx`, `appNavigation.test.ts`. Cases TC-TEN-045 to TC-TEN-059 (TC-TEN-057 to TC-TEN-059: inviter notified on accept or decline, [C87](../../../docs/01-business/roadmap/open-decisions.md#c87)). TC-TEN-056 needs real SMTP and Keycloak.
