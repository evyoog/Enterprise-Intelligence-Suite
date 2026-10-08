# Test plan — REQ-TEN-008 Invite user

Backend: `InvitationServiceTest` (SpringBootTest, H2, fake Keycloak and email) and `LayeredArchitectureTest`. Frontend: `InvitationPage.test.tsx`, `OrganizationInvitationsCard.test.tsx`, `OrganizationMembersCard.test.tsx`, `OrganizationMembersPage.test.tsx`, `AdminOrganizationDetailPage.test.tsx`, `appNavigation.test.ts`. Cases TC-TEN-045 to TC-TEN-056. TC-TEN-056 needs real SMTP and Keycloak.
