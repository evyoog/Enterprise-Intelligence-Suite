# Business dashboard (C69)

| Field | Value |
|---|---|
| Route | `/organization/business-dashboard` (unchanged) |
| Who | Organization admins (`MANAGE_ORGANIZATION`). A member without it (403) or an individual (404) is sent to `/my/products`, as before |
| Decision | [C69](../../01-business/roadmap/open-decisions.md#c69) |
| Code | `frontend/src/pages/BusinessDashboardPage.tsx`, `frontend/src/components/dashboard/*` |

Full content width; every row shares the same left and right edges. White panels (12 px radius, thin border) with an uppercase title and an optional action link at the top-right.

## Layout and data
| Row | Content | Source |
|---|---|---|
| Header | "ORGANIZATION", organization name, "Business dashboard", **Identity Federation** | business dashboard |
| Welcome | "Good morning/afternoon/evening, {name}", one-line summary; overall status (links to `/status`); "N unused seats" or "Seat limit exceeded" → `/organization/settings#members` | members, service status, business dashboard |
| Quick actions | My applications `/my/products`, Product catalog `/products`, Members `/organization/settings#members`, Billing `/organization/billing`, Support `/support/tickets` | — |
| KPIs | Licensed seats (% used, meter) · Active members (of N licensed) · Applications (N with an active subscription) · Application launches (all-time total) — each card links to its module | business dashboard |
| Usage analytics | Filters: Application, Subscription status, Refresh. **Usage by application** (horizontal bars, all-time launches; a bar opens `/products/:id`). **Adoption**: share of applications launched at least once, most used, not launched yet | business dashboard |
| Organization health | **Service health**: overall status, EIS platform, each purchased application, identity federation (connected / not configured) → `/status`. **Seat utilization**: licensed, used, available, % → manage seats. **Subscription status** donut: active vs not subscribed / inactive; a legend row filters the table | business dashboard, service status, SAML/OIDC providers |
| Your workspace | **Recently used** (own launches, relative time, Open) and **Favorites** (star to add/remove, Open). Open = launch URL in a new tab (recorded as a launch), else details | personal dashboard |
| Your account | Avatar, name, email, status; role (Organization admin / Member), two-step verification (+ "Required by your organization"), organization SSO, signed in (current session start); organization permissions; links to Preferences and Security settings | members, MFA, providers, sessions, permissions |
| Applications | Sortable table: Application, Status, Members, Launches, Last used, Action (Open / Details); row → details; horizontal scroll on narrow screens | business dashboard, personal dashboard |
| Activity | **Recent activity**: latest 8 organization audit entries, labelled (e.g. "Subscription renewed"), failures marked, relative time with exact time on hover. **Attention required**: the backend's own alerts (seat limit, unused seats, unused application access, service issues, MFA required, subscription expiring, privileged access), each with an action link; otherwise "You're all caught up" | audit log, business + personal dashboard alerts |
| Billing | Active subscriptions, next renewal (product, date, auto/manual), spent this period with trend vs last period → `/organization/billing` | business dashboard, renewals |

## Not shown (no data)
Launches over time and period filters; member or launch trends; separate Authentication/Billing component status; "View audit activity" (no organization audit page). See C69.

## States
Skeletons per panel while loading; empty states with a route (Explore Product Catalog, Explore applications); a failing source shows "{Section} is unavailable right now." with **Retry** while the rest of the page keeps working.

## Motion
Entrance fade/slide (≈320 ms, staggered 40–60 ms), KPI count-up (700 ms, once), bars grow (600 ms), donut and meters draw (700 ms), KPI hover lift 2 px (150 ms). All off when *Reduce motion* is on or the OS prefers reduced motion.

## Accessibility
Panels are labelled regions; status is always dot **and** text; bars and legend rows are buttons with full names; tables have sortable headers; charts repeat values as text. i18n: `bizDash.*` (en, es).
