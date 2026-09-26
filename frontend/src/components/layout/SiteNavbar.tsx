import { useEffect, useMemo, useRef, useState, type ComponentType, type MouseEvent as ReactMouseEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, Drawer, IconButton, List, ListItemButton, ListItemText, Menu, MenuItem, Tooltip } from '@mui/material'
import {
  ChevronDown, ChevronRight, FileText, LayoutGrid, LifeBuoy, Menu as MenuIcon,
  MessageSquare, Moon, Plug, ShieldCheck, Sun, X,
} from 'lucide-react'
import { Link as RouterLink, useLocation } from 'react-router-dom'
import { productsApi, type Product } from '../../api/productsApi'
import { accentFor, iconFor } from '../../utils/accentColor'
import { useAuth } from '../../auth/AuthProvider'
import { useAuthModal } from '../../auth/AuthModalContext'
import { useThemeMode } from '../../theming/ThemeModeProvider'
import { useLocalePreference, SUPPORTED_TIMEZONES } from '../../theming/LocalePreferenceProvider'
import { SUPPORTED_LANGUAGES } from '../../i18n'
import '../../styles/landing.css'

interface SubLink {
  label: string
  desc?: string
  targetId: string
  icon?: ComponentType<{ size?: number }>
}

// Real content pulled from this same page's own sections (HomePage.tsx) —
// each targetId is an actual element id on the Home page, not an invented
// destination, so "open Solutions" always lands somewhere real.
const SOLUTIONS_LINKS: SubLink[] = [
  { label: 'Single Sign-On', desc: 'Log in once, everywhere.', targetId: 'solutions-sso', icon: ShieldCheck },
  { label: 'Product Catalog', desc: 'Everything you own, in one place.', targetId: 'solutions-catalog', icon: LayoutGrid },
]

const COMPANY_LINKS: SubLink[] = [
  { label: 'About Vyoog', desc: 'Our vision for connected business software.', targetId: 'company' },
  { label: 'Pricing', desc: 'Real pricing from the product catalog.', targetId: 'pricing' },
  { label: 'Resources', desc: 'Docs, stories, and support.', targetId: 'resources' },
]

const RESOURCES_LINKS: SubLink[] = [
  { label: 'Documentation', desc: 'Guides for setup, team access, and roles.', targetId: 'resources', icon: FileText },
  { label: 'Customer stories', desc: 'How organizations connect teams around Vyoog.', targetId: 'resources', icon: MessageSquare },
  { label: 'Support', desc: 'A simple path to technical assistance.', targetId: 'contact', icon: LifeBuoy },
  { label: 'Integrations', desc: 'SSO, Keycloak, and the APIs behind the catalog.', targetId: 'resources', icon: Plug },
]

/** A product with no platform assigned still needs somewhere to show up in
 * the Products menu — this synthetic bucket is never a real Platform row
 * from the backend, just a fallback grouping label for the nav only. */
const UNASSIGNED_PLATFORM_ID = -1

interface PlatformGroup {
  id: number
  name: string
  products: Product[]
}

/**
 * The one navbar for every public page — originally built inline on the Home
 * page (styles/landing.css, scoped under .vyoog-landing) and pulled out here
 * so Home, Products, and any page added later all share the exact same
 * brand/nav/auth-state markup instead of drifting apart. It's `position:
 * fixed` (baked into .navbar in landing.css), so any page using it needs
 * ~72px of top padding to keep its own content from sliding under it — Home's
 * .hero already accounts for this; other pages need to add it themselves.
 *
 * Each top-level item with real sub-content (Products, Solutions, Company,
 * Resources) is a button that opens a dropdown/mega-menu of its actual inner
 * links on click — it never itself jumps to a section, only toggles the
 * panel, since clicking a category header shouldn't also count as picking
 * one of its items. Pricing has no decomposable content of its own, so it
 * stays a plain link. Every sub-link targets a real element id on the Home
 * page: smooth-scrolls there when already on Home, otherwise it's a normal
 * link back to Home's anchor — and either way, picking one closes the panel.
 *
 * Products is the one exception to the flat sub-link list: it mirrors the
 * admin side's own Platform → Apps hierarchy (Thittam/Thiran grouping real
 * apps) as a two-column picker — real data throughout, via the same public
 * /products response the storefront already uses (each product already
 * carries its assigned platforms, so no extra backend call is needed).
 */
export function SiteNavbar() {
  const auth = useAuth()
  const authModal = useAuthModal()
  const { t, i18n } = useTranslation()
  const { resolvedMode, setMode } = useThemeMode()
  const { timeZone, setTimeZone } = useLocalePreference()
  const onHome = useLocation().pathname === '/'
  const [openMenu, setOpenMenu] = useState<string | null>(null)
  const [products, setProducts] = useState<Product[]>([])
  const [selectedPlatformId, setSelectedPlatformId] = useState<number | null>(null)
  const [mobileOpen, setMobileOpen] = useState(false)
  const [langAnchor, setLangAnchor] = useState<HTMLElement | null>(null)
  const navRef = useRef<HTMLElement>(null)

  useEffect(() => {
    productsApi.list().then(setProducts).catch(() => setProducts([]))
  }, [])

  const platformGroups = useMemo<PlatformGroup[]>(() => {
    const groups = new Map<number, PlatformGroup>()
    for (const product of products) {
      const platforms = product.platforms.length > 0
        ? product.platforms
        : [{ id: UNASSIGNED_PLATFORM_ID, name: 'Other Apps' }]
      for (const platform of platforms) {
        const group = groups.get(platform.id)
        if (group) {
          group.products.push(product)
        } else {
          groups.set(platform.id, { id: platform.id, name: platform.name, products: [product] })
        }
      }
    }
    // Real platforms first (in first-seen/catalog order), the fallback bucket last.
    return Array.from(groups.values()).sort((a, b) =>
      a.id === UNASSIGNED_PLATFORM_ID ? 1 : b.id === UNASSIGNED_PLATFORM_ID ? -1 : 0
    )
  }, [products])

  // Keeps a valid platform selected as soon as the catalog loads, and if the
  // menu is reopened later after the catalog changed underneath it.
  useEffect(() => {
    if (platformGroups.length === 0) return
    if (!platformGroups.some((g) => g.id === selectedPlatformId)) {
      setSelectedPlatformId(platformGroups[0].id)
    }
  }, [platformGroups, selectedPlatformId])

  const selectedPlatform = platformGroups.find((g) => g.id === selectedPlatformId) ?? platformGroups[0]

  // Menus open/close on click (toggleMenu below), not hover — so clicking
  // anywhere outside the nav, or pressing Escape, is what closes an open one.
  useEffect(() => {
    function handleOutside(e: MouseEvent) {
      if (navRef.current && !navRef.current.contains(e.target as Node)) setOpenMenu(null)
    }
    function handleEscape(e: KeyboardEvent) {
      if (e.key === 'Escape') setOpenMenu(null)
    }
    document.addEventListener('mousedown', handleOutside)
    document.addEventListener('keydown', handleEscape)
    return () => {
      document.removeEventListener('mousedown', handleOutside)
      document.removeEventListener('keydown', handleEscape)
    }
  }, [])

  const goToSection = (targetId: string) => (e: ReactMouseEvent) => {
    setOpenMenu(null)
    if (!onHome) return
    e.preventDefault()
    document.getElementById(targetId)?.scrollIntoView({ behavior: 'smooth' })
  }

  const toggleMenu = (name: string) => (e: ReactMouseEvent) => {
    e.preventDefault()
    setOpenMenu((prev) => (prev === name ? null : name))
  }

  return (
    <div className="vyoog-landing">
      {/* Phase 22: visually hidden until keyboard-focused — the first thing a
          screen-reader or keyboard user reaches, letting them jump straight
          past all the nav/mega-menu markup above into the page's own content. */}
      <a href="#main-content" className="skip-link">{t('nav.skipToContent')}</a>

      <header className="navbar" ref={navRef}>
        <div className="container nav-inner">
          <RouterLink className="brand" to="/">
            <img src="https://www.vyoog.com/wp-content/uploads/2022/03/evyoog-logonew1.png" alt="" className="brand-logo" />
            EIS Platform
          </RouterLink>

          <IconButton
            className="mobile-menu-toggle"
            aria-label={t('nav.openMenu')}
            onClick={() => setMobileOpen(true)}
          >
            <MenuIcon size={22} />
          </IconButton>

          <nav className="nav-links" aria-label="Primary">
            <div className="nav-item">
              <button
                type="button"
                className="nav-trigger"
                aria-expanded={openMenu === 'products'}
                onClick={toggleMenu('products')}
              >
                Products <ChevronDown size={12} className={openMenu === 'products' ? 'nav-chevron open' : 'nav-chevron'} />
              </button>
              <div className={openMenu === 'products' ? 'mega-panel mega-panel-platforms open' : 'mega-panel mega-panel-platforms'}>
                {platformGroups.length === 0 ? (
                  <p className="mega-empty">No products yet — check back soon.</p>
                ) : (
                  <div className="mega-two-col">
                    <div className="mega-platform-list">
                      {platformGroups.map((group) => (
                        <button
                          key={group.id}
                          type="button"
                          className={group.id === selectedPlatform?.id ? 'mega-platform-item active' : 'mega-platform-item'}
                          onMouseEnter={() => setSelectedPlatformId(group.id)}
                          onClick={() => setSelectedPlatformId(group.id)}
                        >
                          {group.name}
                          <ChevronRight size={13} />
                        </button>
                      ))}
                    </div>
                    <div className="mega-platform-apps">
                      {selectedPlatform?.products.map((product) => {
                        const accent = accentFor(product.name)
                        const Icon = iconFor(product.name)
                        return (
                          <RouterLink
                            key={product.id}
                            to="/products"
                            className="mega-row"
                            onClick={() => setOpenMenu(null)}
                          >
                            <span className="mega-icon" style={{ background: accent.bg, color: accent.fg }}>
                              <Icon size={16} />
                            </span>
                            <span className="mega-text">
                              <span className="mega-title">{product.name}</span>
                              <span className="mega-desc">{product.category ?? 'Vyoog product'}</span>
                            </span>
                          </RouterLink>
                        )
                      })}
                    </div>
                  </div>
                )}
                <div className="mega-footer">
                  <RouterLink to="/products" className="mega-footer-link" onClick={() => setOpenMenu(null)}>
                    {t('nav.viewAllProducts')}
                  </RouterLink>
                </div>
              </div>
            </div>

            <SubMenu
              label="Solutions"
              links={SOLUTIONS_LINKS}
              isOpen={openMenu === 'solutions'}
              onToggle={toggleMenu('solutions')}
              onNavigate={goToSection}
            />

            <a href="/#pricing" onClick={goToSection('pricing')}>Pricing</a>

            <SubMenu
              label="Company"
              links={COMPANY_LINKS}
              isOpen={openMenu === 'company'}
              onToggle={toggleMenu('company')}
              onNavigate={goToSection}
            />

            <SubMenu
              label="Resources"
              links={RESOURCES_LINKS}
              isOpen={openMenu === 'resources'}
              onToggle={toggleMenu('resources')}
              onNavigate={goToSection}
            />
          </nav>

          <div className="nav-actions">
            <Tooltip title={t('theme.toggle')}>
              <IconButton
                size="small"
                aria-label={t('theme.toggle')}
                onClick={() => setMode(resolvedMode === 'dark' ? 'light' : 'dark')}
                sx={{ color: 'inherit' }}
              >
                {resolvedMode === 'dark' ? <Sun size={16} /> : <Moon size={16} />}
              </IconButton>
            </Tooltip>

            <span
              className="lang"
              onClick={(e) => setLangAnchor(e.currentTarget)}
              role="button"
              tabIndex={0}
              aria-haspopup="menu"
            >
              {i18n.language.slice(0, 2).toUpperCase()} <ChevronDown size={12} />
            </span>
            <Menu anchorEl={langAnchor} open={Boolean(langAnchor)} onClose={() => setLangAnchor(null)}>
              <Box sx={{ px: 2, pt: 0.5, pb: 0.5, fontSize: 11, color: 'text.secondary', fontWeight: 700 }}>
                LANGUAGE
              </Box>
              {SUPPORTED_LANGUAGES.map((lang) => (
                <MenuItem
                  key={lang.code}
                  selected={i18n.language === lang.code}
                  onClick={() => { i18n.changeLanguage(lang.code); setLangAnchor(null) }}
                >
                  {lang.label}
                </MenuItem>
              ))}
              <Box sx={{ px: 2, pt: 1, pb: 0.5, fontSize: 11, color: 'text.secondary', fontWeight: 700, borderTop: '1px solid', borderColor: 'divider' }}>
                TIME ZONE
              </Box>
              {SUPPORTED_TIMEZONES.map((zone) => (
                <MenuItem
                  key={zone.code}
                  selected={timeZone === zone.code}
                  onClick={() => { setTimeZone(zone.code); setLangAnchor(null) }}
                >
                  {zone.label}
                </MenuItem>
              ))}
            </Menu>

            {auth.isAuthenticated ? (
              // Signed-in users normally never see the public website: "/"
              // sends them to the tool (PublicOnly), and every tool page
              // renders in AppShell. This header only shows for them on the
              // public account flows (register, verify, password reset), so
              // it offers just the way into the tool and sign out.
              <>
                {auth.isAdmin ? (
                  <RouterLink className="admin-link" to="/admin">
                    <ShieldCheck size={14} /> {t('nav.adminPanel')}
                  </RouterLink>
                ) : (
                  // Always the business-dashboard route: that page itself sends
                  // members without MANAGE_ORGANIZATION and individuals on to
                  // /my/products (see BusinessDashboardPage).
                  <RouterLink className="admin-link" to="/organization/business-dashboard">
                    {t('nav.myWorkspace')}
                  </RouterLink>
                )}
                <button className="get-started" onClick={auth.logout}>{t('nav.signOut')}</button>
              </>
            ) : (
              <>
                <span className="login" onClick={authModal.openLogin}>{t('nav.login')}</span>
                <button className="get-started" onClick={authModal.openRegister}>{t('nav.getStarted')}</button>
              </>
            )}
          </div>
        </div>
      </header>

      {/* Phase 20: the mega-menu/hover nav above has no mobile equivalent —
          this is a real, separate, flattened nav rather than trying to force
          the desktop hover panels into a touch UI. */}
      <Drawer anchor="right" open={mobileOpen} onClose={() => setMobileOpen(false)}>
        <Box sx={{ width: 280, display: 'flex', flexDirection: 'column', height: '100%' }} role="presentation">
          <Box sx={{ display: 'flex', justifyContent: 'flex-end', p: 1 }}>
            <IconButton aria-label={t('nav.closeMenu')} onClick={() => setMobileOpen(false)}>
              <X size={20} />
            </IconButton>
          </Box>
          <List sx={{ flexGrow: 1 }}>
            <ListItemButton component={RouterLink} to="/products" onClick={() => setMobileOpen(false)}>
              <ListItemText primary="Products" />
            </ListItemButton>
            {[...SOLUTIONS_LINKS, { label: 'Pricing', targetId: 'pricing' }, ...COMPANY_LINKS, ...RESOURCES_LINKS].map((link) => (
              <ListItemButton
                key={link.label}
                component="a"
                href={`/#${link.targetId}`}
                onClick={(e) => { setMobileOpen(false); goToSection(link.targetId)(e as unknown as ReactMouseEvent) }}
              >
                <ListItemText primary={link.label} />
              </ListItemButton>
            ))}
          </List>
          <Box sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 1, borderTop: '1px solid', borderColor: 'divider' }}>
            {auth.isAuthenticated ? (
              <>
                <ListItemButton
                  component={RouterLink}
                  to={auth.isAdmin ? '/admin' : '/organization/business-dashboard'}
                  onClick={() => setMobileOpen(false)}
                >
                  <ListItemText primary={auth.isAdmin ? t('nav.adminPanel') : t('nav.myWorkspace')} />
                </ListItemButton>
                <ListItemButton onClick={() => { setMobileOpen(false); auth.logout() }}>
                  <ListItemText primary={t('nav.signOut')} />
                </ListItemButton>
              </>
            ) : (
              <>
                <ListItemButton onClick={() => { setMobileOpen(false); authModal.openLogin() }}>
                  <ListItemText primary={t('nav.login')} />
                </ListItemButton>
                <ListItemButton onClick={() => { setMobileOpen(false); authModal.openRegister() }}>
                  <ListItemText primary={t('nav.getStarted')} />
                </ListItemButton>
              </>
            )}
          </Box>
        </Box>
      </Drawer>
    </div>
  )
}

interface SubMenuProps {
  label: string
  links: SubLink[]
  isOpen: boolean
  onToggle: (e: ReactMouseEvent) => void
  onNavigate: (targetId: string) => (e: ReactMouseEvent) => void
}

function SubMenu({ label, links, isOpen, onToggle, onNavigate }: SubMenuProps) {
  return (
    <div className="nav-item">
      <button type="button" className="nav-trigger" aria-expanded={isOpen} onClick={onToggle}>
        {label} <ChevronDown size={12} className={isOpen ? 'nav-chevron open' : 'nav-chevron'} />
      </button>
      <div className={isOpen ? 'mega-panel open' : 'mega-panel'}>
        <div className="mega-list">
          {links.map((link) => (
            <a key={link.label} href={`/#${link.targetId}`} className="mega-row" onClick={onNavigate(link.targetId)}>
              {link.icon && (
                <span className="mega-icon mega-icon-plain">
                  <link.icon size={16} />
                </span>
              )}
              <span className="mega-text">
                <span className="mega-title">{link.label}</span>
                {link.desc && <span className="mega-desc">{link.desc}</span>}
              </span>
            </a>
          ))}
        </div>
      </div>
    </div>
  )
}
