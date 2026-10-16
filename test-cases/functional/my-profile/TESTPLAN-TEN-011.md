# Test plan — REQ-TEN-011 My profile

Backend (`backend/src/test/java/com/vyoog/eisplatform/modules/profile/`): `ProfileServiceTest` (SpringBootTest, H2, fake Keycloak), `ProfileControllerTest` (security and HTTP: anonymous 401, photo bytes and headers, error bodies), `ProfileValidatorTest`, `ProfilePhotoInspectorTest`, `PersonProfileCompletenessTest`, plus `LayeredArchitectureTest` and `OrgDirectoryServiceTest` (unchanged directory completion). Frontend (`frontend/src/`): `utils/profileValidation.test.ts`, `utils/profilePhoto.test.ts`, `api/profileApi.test.ts`, `pages/ProfilePage.test.tsx`, `components/profile/ProfileCompletionBar.test.tsx`, `components/profile/ProfileEditDialog.test.tsx` and `components/layout/AppShell.test.tsx`. Cases TC-TEN-090 to TC-TEN-138, from decision [C107](../../../docs/01-business/roadmap/open-decisions.md#c107) and the FRD [my-profile](../../../docs/02-requirements/FRD/my-profile/requirement.md).

Groups: field validation and boundaries TC-TEN-093 to TC-TEN-104; read-only data TC-TEN-105 and TC-TEN-106; security (own profile only, anonymous, admin permission) TC-TEN-107 to TC-TEN-109 and TC-TEN-130; no record and the platform administrator TC-TEN-110 to TC-TEN-113; photo TC-TEN-114 to TC-TEN-119; Keycloak mirroring and failure TC-TEN-120 to TC-TEN-123; completion and the global bar TC-TEN-124 to TC-TEN-129; admin dialog TC-TEN-131; audit TC-TEN-132; menu, guard, accessibility and Spanish TC-TEN-133 to TC-TEN-136; regression and schema TC-TEN-137 and TC-TEN-138.

Not covered by automated tests: TC-TEN-138 needs a real PostgreSQL 16 database (the tests load the same schema on H2); TC-TEN-136 (Spanish text) has no automated check (there is no test comparing `en.json` and `es.json` or rendering the page in Spanish), so it is a manual check; the real Keycloak `updateUser` HTTP call is not tested live (TC-TEN-120 and TC-TEN-121 run against the fake client), as TC-TEN-056 is for invitations. The admin pages that open the "Edit profile" dialog (organization detail members list, individuals list) are covered through `ProfileEditDialog.test.tsx` only, not through their page tests.

## Acceptance criteria to test cases
| Acceptance criterion | Test cases |
|---|---|
| AC-1 | TC-TEN-090 |
| AC-2 | TC-TEN-091, TC-TEN-092, TC-TEN-138 |
| AC-3 | TC-TEN-093, TC-TEN-094 |
| AC-4 | TC-TEN-095, TC-TEN-096, TC-TEN-097, TC-TEN-098, TC-TEN-099, TC-TEN-100, TC-TEN-101, TC-TEN-102 |
| AC-5 | TC-TEN-103, TC-TEN-104 |
| AC-6 | TC-TEN-105, TC-TEN-106 |
| AC-7 | TC-TEN-107, TC-TEN-108, TC-TEN-109 |
| AC-8 | TC-TEN-110, TC-TEN-111 |
| AC-9 | TC-TEN-112, TC-TEN-113 |
| AC-10 | TC-TEN-114, TC-TEN-115, TC-TEN-116, TC-TEN-117, TC-TEN-118 |
| AC-11 | TC-TEN-119 |
| AC-12 | TC-TEN-120, TC-TEN-121, TC-TEN-122, TC-TEN-123 |
| AC-13 | TC-TEN-124, TC-TEN-125, TC-TEN-126, TC-TEN-137 |
| AC-14 | TC-TEN-127, TC-TEN-128, TC-TEN-129 |
| AC-15 | TC-TEN-130, TC-TEN-131 |
| AC-16 | TC-TEN-132 |
| AC-17 | TC-TEN-133 |
| AC-18 | TC-TEN-134, TC-TEN-135, TC-TEN-136 |
