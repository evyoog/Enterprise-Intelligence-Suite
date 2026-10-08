import { useEffect, useMemo, useState, type ReactElement } from 'react'
import { useTranslation } from 'react-i18next'
import {
  AppBar, Avatar, Box, Button, Collapse, Divider, Drawer, IconButton, List, ListItemButton, ListItemIcon,
  ListItemText, ListSubheader, Menu, MenuItem, Toolbar, Tooltip, Typography, useMediaQuery, useTheme,
} from '@mui/material'
import { alpha } from '@mui/material/styles'
import { ChevronDown, ChevronRight, Home as HomeIcon, LogOut, Menu as MenuIcon, Moon, Palette, Search as SearchIcon, Sun, UserCog } from 'lucide-react'
import { Link as RouterLink, Navigate, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/AuthProvider'
import { useSignOut } from '../../auth/useSignOut'
import { myPermissionsApi, type MyPermissions } from '../../api/myPermissionsApi'
import { useThemeMode } from '../../theming/ThemeModeProvider'
import { NotificationBell } from './NotificationBell'
import { CartButton } from '../cart/CartButton'
import { TopBarSearch } from './TopBarSearch'
import { AppShellContext, NavAccessContext } from './appShellContext'
import { NavBreadcrumbs } from './NavBreadcrumbs'
import { appHomePath, buildAppNavigation, isNavBranchActive, isNavItemActive, wantsWebsite, WEBSITE_HOME_STATE, type AppNavItem } from './appNavigation'

const DRAWER_WIDTH = 240
const LOGO = 'https://www.vyoog.com/wp-content/uploads/2022/03/evyoog-logonew1.png'

/**
 * Route element for pages reachable both signed out and signed in (the
 * product catalog, preferences) and for every signed-in page: a visitor gets
 * the page as it is (with the public website header the page renders
 * itself), a signed-in user gets it inside the AppShell.
 */
export function AuthAwareLayout() {
  const auth = useAuth()
  return auth.isAuthenticated ? <AppShell /> : <Outlet />
}

/** "/" — the public website. Visitors always see it; signed-in users see it
 * when they chose Home (C79), and otherwise go to the tool. */
export function PublicOnly({ children }: { children: React.ReactNode }) {
  const auth = useAuth()
  const location = useLocation()
  if (auth.isAuthenticated && !wantsWebsite(location.state)) return <Navigate to={appHomePath(auth.isAdmin)} replace />
  return <>{children}</>
}

/**
 * The signed-in software tool: a sidebar filtered to the user's role and
 * permissions (GET /me/permissions), a slim top bar with notifications,
 * theme and the user menu, and the page in the main area. No website
 * navigation or marketing content appears here.
 */
export function AppShell() {
  const { t } = useTranslation()
  const auth = useAuth()
  const location = useLocation()
  const navigate = useNavigate()
  const signOut = useSignOut()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('md'))
  // Tablet: the sidebar stays beside the page but can be folded away.
  const isTablet = useMediaQuery(theme.breakpoints.between('md', 'lg'))
  const [tabletOpen, setTabletOpen] = useState(false)
  const { resolvedMode, setMode } = useThemeMode()
  const [permissions, setPermissions] = useState<MyPermissions | null>(null)
  const [mobileOpen, setMobileOpen] = useState(false)
  const [openGroups, setOpenGroups] = useState<Record<string, boolean>>({})
  const [userAnchor, setUserAnchor] = useState<HTMLElement | null>(null)

  useEffect(() => {
    let active = true
    myPermissionsApi.get()
      .then((p) => { if (active) setPermissions(p) })
      .catch(() => { if (active) setPermissions(null) })
    return () => { active = false }
  }, [auth.user?.username])

  const sections = useMemo(
    () => buildAppNavigation({ isAdmin: auth.isAdmin, permissions }),
    [auth.isAdmin, permissions],
  )

  const username = auth.user?.username ?? ''
  const home = appHomePath(auth.isAdmin)
  const closeMobile = () => setMobileOpen(false)
  const drawerOpen = isMobile ? mobileOpen : isTablet ? tabletOpen : true
  const navAccess = useMemo(() => ({ isAdmin: auth.isAdmin, permissions }), [auth.isAdmin, permissions])

  // C66: plain icons; the current page gets a light indigo background, an
  // indigo icon and text, and a small indicator on the left.
  const navIcon = (item: AppNavItem, nested: boolean, active: boolean) => {
    const Icon = item.icon
    return (
      <ListItemIcon sx={{ minWidth: 32, color: active ? 'primary.main' : 'text.secondary' }}>
        <Icon size={nested ? 16 : 18} />
      </ListItemIcon>
    )
  }
  const activeSx = () => ({
    position: 'relative',
    '&.Mui-selected, &.Mui-selected:hover': { bgcolor: alpha(theme.palette.primary.main, theme.palette.mode === 'dark' ? 0.16 : 0.08) },
    '&.Mui-selected .MuiListItemText-primary': { fontWeight: 600, color: 'primary.main' },
    '&.Mui-selected::before': {
      content: '""', position: 'absolute', left: -8, top: 8, bottom: 8, width: 3, borderRadius: 2, bgcolor: 'primary.main',
    },
  })

  const labelOf = (item: AppNavItem) => t(`appShell.nav.${item.labelKey}`)
  const withHint = (item: AppNavItem, node: ReactElement) => (
    item.hintKey
      ? <Tooltip describeChild title={t(`appShell.navHint.${item.hintKey}`)} placement="right" enterDelay={600}>{node}</Tooltip>
      : node
  )

  const renderItem = (item: AppNavItem, nested = false) => {
    const branchActive = isNavBranchActive(item, location.pathname, location.search)
    if (item.children) {
      // Only the group holding the current page opens by itself; clicking the
      // chevron opens or closes any group and the choice is kept.
      const open = openGroups[item.key] ?? branchActive
      const childActive = item.children.some((c) => isNavBranchActive(c, location.pathname, location.search))
      const toggle = () => setOpenGroups((g) => ({ ...g, [item.key]: !open }))
      return (
        <Box key={item.key}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            {item.to ? (
              // The parent has a page of its own (Knowledge Center): the label opens it,
              // the chevron beside it opens or closes the children.
              <ListItemButton
                component={RouterLink}
                to={item.to}
                selected={!childActive && isNavItemActive(item, location.pathname, location.search)}
                aria-current={!childActive && isNavItemActive(item, location.pathname, location.search) ? 'page' : undefined}
                onClick={closeMobile}
                sx={{ borderRadius: 2, ml: 1, flexGrow: 1, my: 0.25, '& .MuiListItemText-primary': { fontSize: 14 }, ...activeSx() }}
              >
                {navIcon(item, false, branchActive)}
                <ListItemText primary={labelOf(item)} />
              </ListItemButton>
            ) : (
              <ListItemButton
                onClick={toggle}
                aria-expanded={open}
                sx={{ borderRadius: 2, mx: 1, flexGrow: 1, my: 0.25, color: branchActive ? 'primary.main' : 'text.secondary', '& .MuiListItemText-primary': { fontSize: 14, fontWeight: branchActive ? 600 : 400 } }}
              >
                {navIcon(item, false, branchActive)}
                <ListItemText primary={labelOf(item)} />
                {open ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
              </ListItemButton>
            )}
            {item.to && (
              <IconButton
                size="small"
                onClick={toggle}
                aria-expanded={open}
                aria-label={t(open ? 'appShell.collapseGroup' : 'appShell.expandGroup', { name: labelOf(item) })}
                sx={{ mr: 1 }}
              >
                {open ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
              </IconButton>
            )}
          </Box>
          <Collapse in={open} timeout="auto" unmountOnExit>
            <List component="div" disablePadding>
              {item.children.map((child) => renderItem(child, true))}
            </List>
          </Collapse>
        </Box>
      )
    }
    const active = isNavItemActive(item, location.pathname, location.search)
    return withHint(item, (
      <ListItemButton
        key={item.key}
        component={RouterLink}
        to={item.to ?? '/'}
        selected={active}
        aria-current={active ? 'page' : undefined}
        onClick={closeMobile}
        sx={{ borderRadius: 2, mx: 1, pl: nested ? 4.5 : 2, my: 0.25, '& .MuiListItemText-primary': { fontSize: 14 }, ...activeSx() }}
      >
        {navIcon(item, nested, active)}
        <ListItemText primary={labelOf(item)} />
      </ListItemButton>
    ))
  }

  const sidebar = (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <Toolbar disableGutters sx={{ px: 2, gap: 1 }}>
        <Box
          component={RouterLink}
          to={home}
          onClick={closeMobile}
          sx={{ display: 'flex', alignItems: 'center', gap: 1, color: 'text.primary', textDecoration: 'none' }}
        >
          <Box component="img" src={LOGO} alt="" sx={{ height: 22, width: 'auto' }} />
          <Typography sx={{ fontWeight: 700, fontSize: 16, whiteSpace: 'nowrap' }}>
            {t('appShell.productName')}
          </Typography>
        </Box>
      </Toolbar>
      <Box component="nav" aria-label={t('appShell.sidebarLabel')} sx={{ flexGrow: 1, overflowY: 'auto', py: 1 }}>
        {sections.map((section) => (
          <List
            key={section.key}
            component="div"
            dense
            subheader={(
              <ListSubheader
                component="div"
                disableSticky
                sx={{ bgcolor: 'transparent', lineHeight: '32px', fontSize: 11, fontWeight: 600, letterSpacing: '.08em', textTransform: 'uppercase', color: 'text.secondary', mt: 1 }}
              >
                {t(`appShell.sections.${section.labelKey}`)}
              </ListSubheader>
            )}
          >
            {section.items.map((item) => renderItem(item))}
          </List>
        ))}
      </Box>
      <Divider />
      <List component="div" dense>
        <ListItemButton onClick={signOut} sx={{ borderRadius: 1.5, mx: 1 }}>
          <ListItemIcon sx={{ minWidth: 34 }}><LogOut size={18} /></ListItemIcon>
          <ListItemText primary={t('nav.signOut')} />
        </ListItemButton>
      </List>
    </Box>
  )

  return (
    <AppShellContext.Provider value={true}>
      <NavAccessContext.Provider value={navAccess}>
      <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
        <Box
          component="a"
          href="#main-content"
          sx={{
            position: 'fixed', top: -100, left: 8, zIndex: 2000,
            bgcolor: 'primary.main', color: 'primary.contrastText', px: 2, py: 1, borderRadius: 1,
            fontSize: 13, fontWeight: 600, textDecoration: 'none', '&:focus': { top: 8 },
          }}
        >
          {t('nav.skipToContent')}
        </Box>

        <Drawer
          variant={isMobile ? 'temporary' : isTablet ? 'persistent' : 'permanent'}
          open={drawerOpen}
          onClose={closeMobile}
          ModalProps={{ keepMounted: true }}
          sx={{
            width: isTablet && !tabletOpen ? 0 : DRAWER_WIDTH,
            flexShrink: 0,
            '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box', borderRight: '1px solid', borderColor: 'divider', bgcolor: 'background.paper' },
          }}
        >
          {sidebar}
        </Drawer>

        <Box sx={{ flexGrow: 1, minWidth: 0, display: 'flex', flexDirection: 'column' }}>
          <AppBar
            position="sticky"
            color="inherit"
            elevation={0}
            sx={{ borderBottom: '1px solid', borderColor: 'divider', bgcolor: 'background.paper', backdropFilter: 'none' }}
          >
            <Toolbar sx={{ gap: 1 }}>
              {(isMobile || isTablet) && (
                <IconButton
                  edge="start"
                  aria-label={t(isTablet && tabletOpen ? 'nav.closeMenu' : 'nav.openMenu')}
                  aria-expanded={isTablet ? tabletOpen : mobileOpen}
                  onClick={() => (isTablet ? setTabletOpen((v) => !v) : setMobileOpen(true))}
                >
                  <MenuIcon size={22} />
                </IconButton>
              )}
              {!isMobile && <TopBarSearch />}
              <Box sx={{ flexGrow: 1 }} />
              {isMobile ? (
                <Tooltip title={t('nav.home')}>
                  <IconButton component={RouterLink} to="/" state={WEBSITE_HOME_STATE} aria-label={t('nav.home')} sx={{ color: 'inherit' }}>
                    <HomeIcon size={19} />
                  </IconButton>
                </Tooltip>
              ) : (
                <Button
                  component={RouterLink}
                  to="/"
                  state={WEBSITE_HOME_STATE}
                  color="inherit"
                  startIcon={<HomeIcon size={17} />}
                  sx={{ fontWeight: 600, textTransform: 'none' }}
                >
                  {t('nav.home')}
                </Button>
              )}
              {isMobile && (
                <IconButton aria-label={t('search.title')} onClick={() => navigate('/search')} sx={{ color: 'inherit' }}>
                  <SearchIcon size={19} />
                </IconButton>
              )}
              <CartButton />
              <NotificationBell />
              <Tooltip title={t('theme.toggle')}>
                <IconButton
                  aria-label={t('theme.toggle')}
                  onClick={() => setMode(resolvedMode === 'dark' ? 'light' : 'dark')}
                >
                  {resolvedMode === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
                </IconButton>
              </Tooltip>
              <Tooltip title={username}>
                <IconButton
                  aria-label={t('appShell.userMenu', { name: username })}
                  aria-haspopup="menu"
                  onClick={(e) => setUserAnchor(e.currentTarget)}
                >
                  <Avatar sx={{ width: 30, height: 30, fontSize: 14, bgcolor: 'primary.main' }}>
                    {username.slice(0, 1).toUpperCase()}
                  </Avatar>
                  {!isMobile && (
                    <Typography component="span" sx={{ ml: 1, fontSize: 14, fontWeight: 600, color: 'text.primary', maxWidth: 180, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                      {username}
                    </Typography>
                  )}
                </IconButton>
              </Tooltip>
              <Menu anchorEl={userAnchor} open={Boolean(userAnchor)} onClose={() => setUserAnchor(null)}>
                <Box sx={{ px: 2, py: 1 }}>
                  <Typography variant="body2" sx={{ fontWeight: 700 }}>{username}</Typography>
                  <Typography variant="caption" sx={{ color: 'text.secondary' }}>
                    {auth.isAdmin ? t('appShell.rolePlatformAdmin') : t('appShell.roleUser')}
                  </Typography>
                </Box>
                <Divider />
                <MenuItem onClick={() => { setUserAnchor(null); navigate('/account/security') }}>
                  <ListItemIcon><UserCog size={16} /></ListItemIcon>
                  {t('appShell.nav.security')}
                </MenuItem>
                <MenuItem onClick={() => { setUserAnchor(null); navigate('/account/preferences') }}>
                  <ListItemIcon><Palette size={16} /></ListItemIcon>
                  {t('appShell.nav.preferences')}
                </MenuItem>
                <Divider />
                <MenuItem onClick={() => { setUserAnchor(null); signOut() }}>
                  <ListItemIcon><LogOut size={16} /></ListItemIcon>
                  {t('nav.signOut')}
                </MenuItem>
              </Menu>
            </Toolbar>
          </AppBar>

          <Box component="main" id="main-content" tabIndex={-1} sx={{ flexGrow: 1, p: { xs: 2, sm: 3, md: 4 }, outline: 'none' }}>
            <NavBreadcrumbs sections={sections} />
            <Outlet />
          </Box>
        </Box>
      </Box>
      </NavAccessContext.Provider>
    </AppShellContext.Provider>
  )
}
