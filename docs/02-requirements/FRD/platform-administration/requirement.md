# REQ-GOV-001 — Platform Administration

**Status:** Approved
**BRD:** Not specified
**Owner:** Product owner
**Approved by / on:** Product owner / 2026-09-27 ([C36](../../../01-business/roadmap/open-decisions.md#c36)); extended 2026-10-13 ([C92](../../../01-business/roadmap/open-decisions.md#c92), TM-1 A and TM-2 A)

| Field | Value |
|---|---|
| Sprint | [2026.4.2](../../../01-business/roadmap/sprints/SPRINT-2026.4.2.md) |
| Requirement ID | REQ-GOV-001 |
| Application | [15 Administration & Governance](../../../01-business/roadmap/applications/15-administration-governance.md) |
| Application code | `APP-GOV` ([DN-5](../../../01-business/roadmap/open-decisions.md#dn-5-application-codes)) |
| Priority | P0 ([C4](../../../01-business/roadmap/open-decisions.md#c4), [C5](../../../01-business/roadmap/open-decisions.md#c5)) |
| AI required | No ([C12](../../../01-business/roadmap/open-decisions.md#c12)) |

## Source functions
| Function ID | Function | Application page |
|---|---|---|
| 15.01.01 | Configure platform (currencies, feature flags) | [15 Administration & Governance](../../../01-business/roadmap/applications/15-administration-governance.md#1501-platform-administration) |
| 15.01.01 | Configure languages (read-only) | same |
| 15.01.02 | Configure regions (Global Settings) | same |
| 15.01.02.02 | Configure defaults for new users (C92, TM-2 A) | same |
| 15.01.02.03 | Manage message templates (C92, TM-1 A) | same |

15.01.02.02 Configure defaults and 15.01.02.03 Manage templates were carried forward by [C36](../../../01-business/roadmap/open-decisions.md#c36) and are built by [C92](../../../01-business/roadmap/open-decisions.md#c92) (REQ-GOV-001.7 to .12).

## Summary
A platform administrator manages platform-wide settings on Platform → Products → Configuration (the admin Common settings page): which currencies are offered on the plan editor, a free-form list of regions organizations can be assigned to, and runtime feature flags that gate whether a built feature is currently switched on. Supported languages are shown for reference only; adding one requires real translated strings, not a setting. Since C92 the same page also holds the **defaults new users start with** and the **message templates** for the emails and notifications EIS sends.

## Actors
- Platform administrator (holding `MANAGE_PLATFORM_SETTINGS`)

## Functional requirements
| ID | Requirement | Priority |
|---|---|---|
| REQ-GOV-001.1 | An admin can view every currency and toggle it enabled/disabled. Disabling a currency only removes it from the plan editor's choices; it does not alter any existing plan. | P0 |
| REQ-GOV-001.2 | An admin can create, rename, enable/disable and delete a region. A region assigned to an organization cannot be deleted. | P0 |
| REQ-GOV-001.3 | An admin can create, update (enabled/description) and delete a feature flag. | P0 |
| REQ-GOV-001.4 | Any module can check whether a named feature flag is enabled; an unknown key is treated as enabled. | P0 |
| REQ-GOV-001.5 | The "groups_enabled" flag gates every 05.04.01 Groups action; disabling it makes Groups behave, for every organization, exactly as it does when the caller lacks the underlying permission. | P0 |
| REQ-GOV-001.6 | An admin can view the platform's supported languages (read-only). | P0 |
| REQ-GOV-001.7 | **Message templates (C92, TM-1 A):** an admin can see the 14 message types EIS sends (5 emails, 9 notifications), and for each type and language (English, Spanish) edit the subject and body, preview them with example values, and reset the language to the built-in text. Text uses `{placeholders}` such as `{name}`. | P0 |
| REQ-GOV-001.8 | A template may use only the placeholders its type offers; placeholders marked required must appear; subject up to 200 and body up to 5000 characters. Otherwise the save is refused with a message naming the problem. | P0 |
| REQ-GOV-001.9 | Every email and notification of those types is built from the template for the **recipient's language** (the recipient's own language, else the platform default language, else English); a language with no text falls back to English. With nothing edited, the text is what EIS sent before this change. | P0 |
| REQ-GOV-001.10 | **Defaults for new users (C92, TM-2 A):** an admin can set a default language, region, currency, time zone, date format and renewal reminder days. An empty field means no platform default. Values are validated (a supported language, a real locale, an enabled currency, a real time zone, a known date format, reminder days 1 to 30). | P0 |
| REQ-GOV-001.11 | A user's own saved choice always wins. A default fills only what the user has not chosen: language, region and time zone when the account has no saved value; date format only for an account with no saved preferences that is still on the region's own format. The defaults are returned with the user's preferences so the app can apply them. | P0 |
| REQ-GOV-001.12 | Template and default changes are audited (`MESSAGE_TEMPLATE_CHANGED`, `MESSAGE_TEMPLATE_RESET`, `PLATFORM_DEFAULTS_CHANGED`). | P0 |

## Out of scope
- Currency conversion, multi-currency billing, or any actual charge being affected by a currency's enabled state (C36)
- A fixed or admin-editable list of regions with real geography — regions are freely admin-defined (C36)
- Adding or removing a language (needs real translated strings; out of scope) (C36)
- Templates for messages other than the 14 listed (billing, tickets, order and subscription notices); a template version history; per-organization templates or defaults; applying the default currency to plans (C92)

## Dependencies
- New module `modules/administration`: `PlatformCurrency`, `PlatformRegion`, `PlatformFeatureFlag`, `PlatformAdministrationService`, `PlatformFeatureFlagService`.
- New tables `platform_currency`, `platform_region`, `platform_feature_flag` ([V007](../../../../database/migrations/V007__platform_administration_tenant_lifecycle.sql)).
- C92: new module `modules/template` (`MessageCatalog`, `MessageTemplateService`, `MessageLanguageResolver`), `PlatformDefaultsService`, tables `message_template` and `platform_default` ([V033](../../../../database/migrations/V033__message_templates_platform_defaults.sql)); the renewal reminder default is read and written through `BillingSettingsService`. Details: [platform-defaults-templates](../../../06-api/api-requirements/platform-defaults-templates.md), [data model](../../../07-database/data-model/platform-defaults-templates.md), [screens](../../../05-ui/screen-requirements/platform-defaults-templates.md).
- New permission `MANAGE_PLATFORM_SETTINGS`, seeded for ADMIN.
- Consumed by [tenant-lifecycle](../tenant-lifecycle/requirement.md) (region assignment) and by 05.04.01 Groups (the "groups_enabled" flag).
