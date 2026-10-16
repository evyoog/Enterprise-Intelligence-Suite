# Acceptance criteria — My profile

| ID | Criterion |
|---|---|
| AC-1 | Every signed-in role (platform administrator, organization administrator, member, individual) can open `/account/profile` and see their own profile on the tabs Personal, Work, Address, Online and Account. |
| AC-2 | A person can edit the fields of the first four tabs and save with one Save bar; the saved values are shown again after a reload and the display name uses the preferred name when set. |
| AC-3 | Text is trimmed, inner whitespace is collapsed, a blank optional value is cleared and `<`, `>` and control characters are refused everywhere (BR-PRF-005, BR-PRF-006). |
| AC-4 | The field rules hold at their boundaries: names, preferred name, bio, work, address, postal code (including India), mobile and alternate phone (E.164), LinkedIn and website (BR-PRF-007 to BR-PRF-011). |
| AC-5 | When fields are invalid nothing is saved, every invalid field is reported at once as `<section>.<field>`, the screen shows the message on the field and a badge on the tab, and Save is refused locally while a field is invalid (BR-PRF-012, BR-PRF-023). |
| AC-6 | Email, role and status cannot be changed from the profile; language and time zone are reached by a link to Preferences, not duplicated (BR-PRF-004, BR-PRF-022). |
| AC-7 | A person can never read or change another person's profile or photo through `/me/**`; an anonymous call is refused; no id in the request is honoured (BR-PRF-001). |
| AC-8 | A person without a customer record gets 404 unless they are a platform administrator, who sees the optional "Create my profile" banner, is blocked from nothing and sees no global bar (BR-PRF-002, BR-PRF-003). |
| AC-9 | A platform administrator creates their own record from the banner with the names prefilled; the record is audited; a token email already linked to another login gives 409 (BR-PRF-003). |
| AC-10 | A photo of type JPEG, PNG or WebP up to 1 MB and 4096 x 4096 can be uploaded, replaced and removed; SVG, other types, a wrong first-bytes content, an empty, oversize or undecodable file are refused (413 for size); the browser downsizes to 512 x 512 (BR-PRF-013, BR-PRF-014). |
| AC-11 | The photo is read only by its owner with the stored content type, `Cache-Control: private, max-age=0, must-revalidate` and an `ETag`, 404 when none, and is never part of the profile JSON (BR-PRF-014). |
| AC-12 | A name change is mirrored to Keycloak; when Keycloak fails the whole save is rolled back with 502 and nothing is changed; names never become blank; `nameEditable` is true for every person in this version and the screen shows the names read-only if it is ever false (BR-PRF-015, BR-PRF-016). |
| AC-13 | Completion is `round(filled * 100 / 11)` over the 11 items, counts only non-blank values and an existing photo, and is the same in the profile and in `/me/profile/completion`; the directory's completion is unchanged (BR-PRF-017, BR-PRF-018). |
| AC-14 | The global bar shows on every tool page while the person has a record and is under 100%, is hidden at 100%, on the profile page and for a person without a record, can be dismissed for the browser session (per person), returns in the next browser session, and refreshes after a save or photo change without polling (BR-PRF-019). |
| AC-15 | A platform administrator with `MANAGE_REGISTRATIONS` can view and edit any existing customer's profile (no photo) from the "Edit profile" dialog with the same validation and an audit entry naming the administrator; anyone else gets 403 (BR-PRF-020). |
| AC-16 | Every profile event is in the audit log with the sections changed and never a value (BR-PRF-021). |
| AC-17 | The profile menu lists My profile first and shows the email; the avatar and name block opens the profile (BR-PRF-025). |
| AC-18 | The page, the bar and the dialog work in English and Spanish and pass the accessibility checks; leaving with unsaved changes asks for confirmation (BR-PRF-023, BR-PRF-024). |
