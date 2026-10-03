# UI requirements — Auto-renewal and renewal reminders (REQ-SUB-004)

All three are additions to existing screens ([C44](../../../01-business/roadmap/open-decisions.md#c44)), described in [renewal-reminders.md](../../../05-ui/screen-requirements/renewal-reminders.md).

| Addition | Existing screen | Route | Roles |
|---|---|---|---|
| Renewal reminders card | Preferences | `/account/preferences` | Signed-in users |
| Auto-renew chip, renewal date, next reminder, "Reminders are off" | My subscriptions | `/my/subscriptions` | Signed-in users |
| Renewal reminders tab | Billing settings | `/admin/billing/settings?tab=reminders` | `MANAGE_BILLING` |
