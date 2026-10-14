# UI requirements — Auto-renewal and renewal reminders (REQ-SUB-004)

All additions are to existing screens ([C44](../../../01-business/roadmap/open-decisions.md#c44)), described in [renewal-reminders.md](../../../05-ui/screen-requirements/renewal-reminders.md) and [my-subscriptions.md](../../../05-ui/screen-requirements/my-subscriptions.md).

| Addition | Existing screen | Route | Roles |
|---|---|---|---|
| Renewal reminders card | Preferences | `/account/preferences` | Signed-in users |
| Auto-renew switch (with a confirmation when turning it off), chip, renewal date, next reminder, "Reminders are off", **Read-only until {date}** chip and notice during the grace period | My subscriptions (individual cards and organization cards) | `/my/subscriptions` | Signed-in users; for an organization subscription Manage billing or Manage subscriptions |
| Renewal reminder recipients section (always-on people read-only; added people with Email and In-app) | My subscriptions, below the organization subscriptions | `/my/subscriptions` | `MANAGE_ORGANIZATION_BILLING` (hidden from everyone else) |
| Renewal reminders tab | Billing settings | `/admin/billing/settings?tab=reminders` | `MANAGE_BILLING` |
| Message templates `NOTIF_RENEWAL_REMINDER`, `NOTIF_RENEWAL_GRACE_REMINDER` | Platform defaults and templates | see [platform-defaults-templates.md](../../../05-ui/screen-requirements/platform-defaults-templates.md) | Platform administrators |
