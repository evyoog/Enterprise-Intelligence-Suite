# Acceptance criteria — Platform Administration

Each criterion maps to at least one test case in `test-cases/functional/platform-administration/`.

| ID | Criterion | Test cases |
|----|-----------|------------|
| AC-1 | **Given** the backend has started **When** an admin lists currencies **Then** USD, EUR, GBP and INR all exist and are enabled | TC-GOV-001 |
| AC-2 | **Given** an enabled currency **When** an admin disables it, then re-enables it **Then** both changes are saved and returned | TC-GOV-002 |
| AC-3 | **Given** an admin **When** they create a region, then rename and disable it **Then** all three changes are saved | TC-GOV-003 |
| AC-4 | **Given** a region assigned to an organization **When** an admin tries to delete it **Then** the request is refused | TC-GOV-004 |
| AC-5 | **Given** an admin **When** they create, disable and delete a feature flag **Then** each change is saved, and the flag reads as enabled again once deleted (fails open) | TC-GOV-005 |
| AC-6 | **Given** no row exists for a flag key **When** any module checks it **Then** it reads as enabled | TC-GOV-006 |
| AC-7 | **Given** the "groups_enabled" flag is disabled **When** an organization admin tries any Groups action **Then** every one is refused, and re-enabling the flag restores them | TC-GOV-007 |
| AC-8 | **Given** nothing was edited **When** a message is built **Then** the text is what EIS sent before and its placeholders are filled in the recipient's language | TC-GOV-008 |
| AC-9 | **Given** an admin **When** they edit one language of a message type, then reset it **Then** only that language changes, the reset restores the built-in text, and each change is audited | TC-GOV-009 |
| AC-10 | **Given** a template **When** it has an unknown placeholder, lacks a required one or is too long **Then** the save is refused and nothing is stored | TC-GOV-010 |
| AC-11 | **Given** edited text **When** an admin previews it **Then** example values are shown and nothing is saved | TC-GOV-011 |
| AC-12 | **Given** edited templates **When** a notification or email of that type is sent **Then** it uses the edited text in the recipient's language | TC-GOV-012 |
| AC-13 | **Given** a recipient with and without a saved language **When** a message is sent **Then** the language is the recipient's own, else the platform default, else English | TC-GOV-013 |
| AC-14 | **Given** no defaults **When** an admin sets and clears them **Then** values are validated, saved, audited and a bad value is refused | TC-GOV-014 |
| AC-15 | **Given** defaults **When** a user reads their preferences **Then** the defaults are included and the user's own choices are not overwritten | TC-GOV-015 |
| AC-16 | **Given** a caller without `MANAGE_PLATFORM_SETTINGS` **When** they call the templates or defaults endpoints **Then** 403 | TC-GOV-016 |
| AC-17 | **Given** the defaults section **Then** an admin can save, a rejected value shows the server message, and it passes the axe test | TC-GOV-017 |
| AC-18 | **Given** the templates section **Then** an admin can edit, preview, reset, a rejected placeholder shows the server message, and it passes the axe test | TC-GOV-018 |
