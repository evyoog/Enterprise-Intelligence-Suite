# Application layout: public website vs signed-in tool

Requested by the product owner on 2026-09-26. The frontend has two separate frames, chosen by whether the user is signed in.

| State | Frame | Navigation |
|---|---|---|
| Not signed in | **Public Vyoog website** | `SiteNavbar` (Products mega-menu, Solutions, Pricing, Company, Resources, Login, Get started) |
| Signed in | **Vyoog software tool** (`AppShell`) | Sidebar filtered by role and permissions; slim top bar with notifications, theme and the account menu. No website header or marketing copy |

## Routes
| Route | Visitor | Signed-in user |
|---|---|---|
| `/` | Website home | Redirected to the tool home: `/admin` for a platform admin, otherwise `/organization/business-dashboard` (which sends members without `MANAGE_ORGANIZATION` and individuals on to `/my/products`) |
| `/products` | Public catalog with the website header and banner | Catalog inside the tool: title, search and filters only |
| `/account/preferences` | Public page with the website header | Inside the tool |
| `/my/products`, `/organization/business-dashboard`, `/account/security`, `/organization/identity-federation` | Redirected to `/` (`RequireAuth`) | Inside the tool |
| `/admin/**` | Redirected to `/` (`RequireAdmin`) | Inside the tool for a platform admin; a non-admin is sent to `/products` |
| `/register/**`, `/forgot-password`, `/reset-password` | Public pages | Public pages. The website header then shows only "My Workspace" / "Admin Panel" and Sign out |

## Sidebar
Built by `buildAppNavigation` (`frontend/src/components/layout/appNavigation.ts`) from `useAuth().isAdmin` and `GET /me/permissions`. Each item is gated on the permission its backend endpoints check. This is UI filtering only; the backend stays the security boundary.

| Section | Item | Shown when |
|---|---|---|
| Workspace | Business dashboard | Organization permission `MANAGE_ORGANIZATION` (while permissions load: any non-admin) |
| Workspace | My products | Not a platform admin, or a member of an organization |
| Workspace | Product catalog | Always |
| Organization | Identity federation | Organization permission `MANAGE_ORGANIZATION` |
| Administration | Platforms, All apps, Settings (App / Products / Common) | Platform admin with `MANAGE_CATALOG` |
| Administration | Registrations | Platform admin with `MANAGE_REGISTRATIONS` |
| Administration | Privileged access | Platform admin with `MANAGE_PRIVILEGED_ACCESS` |
| Administration | Roles / Permissions | Platform admin with `MANAGE_ROLES` / `MANAGE_PERMISSIONS` |
| Administration | Audit log | Platform admin with `VIEW_AUDIT_LOG` |
| Account | Security, Preferences | Always |

While `GET /me/permissions` is loading, or if it fails, a platform admin sees the full admin menu (the behavior of the old `AdminLayout`), and organization-only items stay hidden.

## States and accessibility
- Desktop: permanent 260px sidebar. Below the `md` breakpoint it becomes a drawer, opened from the top bar.
- A skip link goes to `#main-content`. The sidebar is a `nav` landmark labelled "Application", and the current page has `aria-current="page"`.
- Text is under `appShell` in `en.json` and `es.json`.
- Tests: `AppShell.test.tsx` (including jest-axe) and `appNavigation.test.ts`.
