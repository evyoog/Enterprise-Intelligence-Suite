# Screen: platform defaults and message templates

| Field | Value |
|---|---|
| Requirement | [REQ-GOV-001.7 to .12](../../02-requirements/FRD/platform-administration/requirement.md), [C92](../../01-business/roadmap/open-decisions.md#c92) |
| Placement | **Platform → Products → Configuration** (`/admin?tab=configuration`), two sections below the feature flags |
| Permissions | `MANAGE_PLATFORM_SETTINGS` |
| Built | 2026-10-13 — `frontend/src/components/admin/PlatformDefaultsSection.tsx`, `MessageTemplatesSection.tsx` |

## Defaults for new users
Fields: **Language** (supported languages, or *No default*), **Region** (text, for example `en-IN`), **Currency** (enabled currencies, or *No default*), **Time zone** (text, for example `Asia/Kolkata`), **Date format** (*No default*, *Region default* or a format), **Renewal reminder days** (1 to 30). **Save defaults**. A rejected value shows the server's message; success shows "Defaults saved."

## Message templates
List of the 14 message types with name, description, channel chip (Email or Notification) and a *Customized* chip when any language is edited; **Edit** opens a dialog with one tab per language, **Subject** and **Message** fields, the placeholders as chips (* = required), **Preview** (example values, nothing saved), **Save**, **Reset to built-in** (enabled when the language is customized) and **Close**. Server messages (for example an unknown placeholder) are shown in the dialog.

## Application of the defaults
After sign-in the app fills the user's language, region and time zone from the defaults when the account has no saved value, and the date format when the account has no saved preferences and is still on the region's own format (`PreferenceSync`).

## Accessibility and i18n
Sections are labelled regions; the dialog is labelled by its title; the language tabs are a labelled tab list; axe tests for both. Strings under `platformDefaults.*` and `messageTemplates.*` (English and Spanish).
