# Test plan — REQ-TEN-005 Access management

Backend: `AccessManagementServiceTest` (SpringBootTest, H2), with the older `OrganizationSelfServiceTest`, `SubscriptionSeatServiceTest`, `BusinessDashboardServiceTest`, `AuthorizationServiceTest` updated for the new defaults (default-ON product access, named seats, BR-3), `ProjectServiceTest` and `InvitationServiceTest` unchanged. Frontend: `AccessManagement.test.tsx`. Cases TC-TEN-068 to TC-TEN-079. UAT: [access-management UAT scripts](../../UAT/access-management/README.md).

Not run: migration V030 against a real PostgreSQL (the tests use H2 with generated tables, so the partial unique indexes and the data copy are not exercised); a run of the startup backfill against a copy of a real database; sending the notification emails through a real mail server (the in-app notices are asserted).
