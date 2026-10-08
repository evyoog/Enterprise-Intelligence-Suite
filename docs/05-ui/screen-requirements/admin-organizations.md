# Screen: Organizations (`/admin/organizations`)

**Requirement:** [REQ-TEN-007](../../02-requirements/FRD/organization-directory/requirement.md) · **Access:** `MANAGE_REGISTRATIONS`

- **Header:** "Organizations", action *Reset MFA by email*.
- **Summary strip:** total, organizations, individuals, profile complete / in progress / not started, average completion.
- **Filter row:** search, Type, Status, Lifecycle, Country, Region, Industry, Profile, Seats, Registered from/to, *Clear filters*.
- **Table:** sortable columns of REQ-TEN-007.2; the profile completion cell is a bar with the percentage and a tooltip listing the missing items; a warning chip when sign-in is not linked; row click opens the detail page; row menu has the lifecycle actions.
- **States:** loading, error with retry, empty ("no organization matches the filters").
