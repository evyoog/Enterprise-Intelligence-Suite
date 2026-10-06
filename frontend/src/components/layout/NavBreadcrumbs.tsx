import { Breadcrumbs, Link, Typography } from '@mui/material'
import { ChevronRight } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, useLocation } from 'react-router-dom'
import { findNavTrail, type AppNavSection } from './appNavigation'

/** Pages deeper than their sidebar item: the last crumb names them (i18n appShell.crumb.*). */
const SUB_PAGES: [RegExp, string][] = [
  [/^\/admin\/platforms\/new$/, 'new'],
  [/^\/admin\/platforms\/[^/]+\/edit$/, 'editProduct'],
  [/^\/admin\/platforms\/[^/]+$/, 'details'],
  [/^\/admin\/apps\/new$/, 'new'],
  [/^\/admin\/products\/[^/]+\/edit$/, 'editApplication'],
  [/^\/products\/[^/]+$/, 'details'],
  [/^\/catalog\/platforms\/[^/]+$/, 'details'],
  [/^\/admin\/partners\/[^/]+$/, 'details'],
  [/^\/knowledge-management\/content\/new$/, 'new'],
  [/^\/knowledge-management\/content\/[^/]+$/, 'edit'],
  [/^\/knowledge-management\/videos\/new$/, 'new'],
  [/^\/knowledge-management\/videos\/[^/]+$/, 'edit'],
]

/**
 * C80: where the current page sits (Group / Parent / Page), shown for nested
 * pages instead of adding more sidebar entries, for example
 * Platform / Integrations / API keys, or Platform / Products / Edit product.
 * A page that is a top-level sidebar item with no sub-page shows no crumbs.
 */
export function NavBreadcrumbs({ sections }: { sections: AppNavSection[] }) {
  const { t } = useTranslation()
  const { pathname, search } = useLocation()
  const trail = findNavTrail(sections, pathname, search)
  if (!trail) return null
  const sub = SUB_PAGES.find(([re]) => re.test(pathname))?.[1]
  if (!trail.parent && !sub) return null

  const crumbs: { label: string; to?: string }[] = [{ label: t(`appShell.sections.${trail.section.labelKey}`) }]
  if (trail.parent) crumbs.push({ label: t(`appShell.nav.${trail.parent.labelKey}`), to: trail.parent.to })
  crumbs.push({ label: t(`appShell.nav.${trail.item.labelKey}`), to: sub ? trail.item.to : undefined })
  if (sub) crumbs.push({ label: t(`appShell.crumb.${sub}`) })

  return (
    <Breadcrumbs
      aria-label={t('appShell.breadcrumbs')}
      separator={<ChevronRight size={12} aria-hidden />}
      sx={{ mb: 1.5, fontSize: 13 }}
    >
      {crumbs.map((c, i) => {
        const last = i === crumbs.length - 1
        if (c.to && !last) {
          return <Link key={i} component={RouterLink} to={c.to} underline="hover" color="text.secondary" sx={{ fontSize: 13 }}>{c.label}</Link>
        }
        return (
          <Typography key={i} component="span" aria-current={last ? 'page' : undefined}
            sx={{ fontSize: 13, color: last ? 'text.primary' : 'text.secondary', fontWeight: last ? 600 : 400 }}>
            {c.label}
          </Typography>
        )
      })}
    </Breadcrumbs>
  )
}
