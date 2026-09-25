import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import {
  AppBar, Box, Collapse, Divider, Drawer, IconButton, List, ListItemButton,
  ListItemIcon, ListItemText, Toolbar, Typography, useMediaQuery, useTheme,
} from '@mui/material'
import {
  ChevronDown, ChevronRight, KeyRound, LayoutGrid, Layers, LogOut, Menu as MenuIcon,
  Package, ScrollText, Settings, ShieldCheck, SlidersHorizontal, UserCheck, UsersRound,
} from 'lucide-react'
import { Outlet, useLocation, Link as RouterLink } from 'react-router-dom'
import { useAuth } from '../../auth/AuthProvider'

const DRAWER_WIDTH = 240

// "Settings" is a category with sub-pages, not a page itself — App, Platform
// and Common are the first three; more are expected to land here later.
const SETTINGS_ITEMS = [
  { to: '/admin/settings/product', label: 'App', icon: Package },
  { to: '/admin/settings/platform', label: 'Products', icon: Layers },
  { to: '/admin/settings/common', label: 'Common', icon: SlidersHorizontal },
]

/**
 * Sidebar shell for everything under /admin. Nested routes (AdminProductsPage,
 * settings/ProductSettingsPage) render into the <Outlet/> below — this component
 * itself never changes when you navigate between them, only its content area does.
 *
 * Phase 20: the drawer used to be permanently 240px wide regardless of
 * viewport — on a phone-width screen that left almost nothing for content.
 * It's now a real MUI responsive drawer: permanent (always visible, content
 * pushed over) at desktop widths, temporary (overlay, toggled by a hamburger
 * button in a small top AppBar) below that.
 */
export function AdminLayout() {
  const { t } = useTranslation()
  const auth = useAuth()
  const location = useLocation()
  const theme = useTheme()
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'))
  const isInSettings = location.pathname.startsWith('/admin/settings')
  const [settingsOpen, setSettingsOpen] = useState(isInSettings)
  const [mobileOpen, setMobileOpen] = useState(false)

  const navItems = (
    <>
      <List sx={{ flexGrow: 1 }}>
        <ListItemButton
          component={RouterLink}
          to="/admin"
          selected={location.pathname === '/admin' || location.pathname.startsWith('/admin/platforms')}
          onClick={() => setMobileOpen(false)}
        >
          <ListItemIcon sx={{ minWidth: 36 }}>
            <Layers size={18} />
          </ListItemIcon>
          <ListItemText primary="Platforms" />
        </ListItemButton>

        <ListItemButton
          component={RouterLink}
          to="/admin/apps"
          selected={location.pathname === '/admin/apps'}
          onClick={() => setMobileOpen(false)}
        >
          <ListItemIcon sx={{ minWidth: 36 }}>
            <LayoutGrid size={18} />
          </ListItemIcon>
          <ListItemText primary="All Apps" />
        </ListItemButton>

        <ListItemButton
          component={RouterLink}
          to="/admin/registrations"
          selected={location.pathname === '/admin/registrations'}
          onClick={() => setMobileOpen(false)}
        >
          <ListItemIcon sx={{ minWidth: 36 }}>
            <UserCheck size={18} />
          </ListItemIcon>
          <ListItemText primary="Registrations" />
        </ListItemButton>

        <ListItemButton
          component={RouterLink}
          to="/admin/privileged-access"
          selected={location.pathname === '/admin/privileged-access'}
          onClick={() => setMobileOpen(false)}
        >
          <ListItemIcon sx={{ minWidth: 36 }}>
            <KeyRound size={18} />
          </ListItemIcon>
          <ListItemText primary="Privileged Access" />
        </ListItemButton>

        {/* Sprint 2026.3.3, REQ-IAM-003 */}
        <ListItemButton
          component={RouterLink}
          to="/admin/roles"
          selected={location.pathname === '/admin/roles'}
          onClick={() => setMobileOpen(false)}
        >
          <ListItemIcon sx={{ minWidth: 36 }}>
            <UsersRound size={18} />
          </ListItemIcon>
          <ListItemText primary={t('adminRbac.navRoles')} />
        </ListItemButton>

        <ListItemButton
          component={RouterLink}
          to="/admin/permissions"
          selected={location.pathname === '/admin/permissions'}
          onClick={() => setMobileOpen(false)}
        >
          <ListItemIcon sx={{ minWidth: 36 }}>
            <ShieldCheck size={18} />
          </ListItemIcon>
          <ListItemText primary={t('adminRbac.navPermissions')} />
        </ListItemButton>

        <ListItemButton
          component={RouterLink}
          to="/admin/audit-log"
          selected={location.pathname === '/admin/audit-log'}
          onClick={() => setMobileOpen(false)}
        >
          <ListItemIcon sx={{ minWidth: 36 }}>
            <ScrollText size={18} />
          </ListItemIcon>
          <ListItemText primary="Audit Log" />
        </ListItemButton>

        <ListItemButton onClick={() => setSettingsOpen((v) => !v)} selected={isInSettings && !settingsOpen}>
          <ListItemIcon sx={{ minWidth: 36 }}>
            <Settings size={18} />
          </ListItemIcon>
          <ListItemText primary="Settings" />
          {settingsOpen ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
        </ListItemButton>
        <Collapse in={settingsOpen} timeout="auto" unmountOnExit>
          <List component="div" disablePadding>
            {SETTINGS_ITEMS.map(({ to, label, icon: Icon }) => (
              <ListItemButton
                key={to}
                component={RouterLink}
                to={to}
                selected={location.pathname === to}
                onClick={() => setMobileOpen(false)}
                sx={{ pl: 4 }}
              >
                <ListItemIcon sx={{ minWidth: 32 }}>
                  <Icon size={16} />
                </ListItemIcon>
                <ListItemText primary={label} />
              </ListItemButton>
            ))}
          </List>
        </Collapse>
      </List>

      <Divider />
      <List>
        <ListItemButton onClick={auth.logout}>
          <ListItemIcon sx={{ minWidth: 36 }}>
            <LogOut size={18} />
          </ListItemIcon>
          <ListItemText primary={`Sign out (${auth.user?.username})`} />
        </ListItemButton>
      </List>
    </>
  )

  const brand = (
    <Toolbar disableGutters sx={{ gap: 0.75, px: 1.5 }}>
      <Box
        component="img"
        src="https://www.vyoog.com/wp-content/uploads/2022/03/evyoog-logonew1.png"
        alt=""
        sx={{ height: 22, width: 'auto', flexShrink: 0 }}
      />
      <Typography sx={{ fontWeight: 700, fontSize: 16, whiteSpace: 'nowrap' }}>
        EIS Platform<Box component="span" sx={{ color: 'primary.main' }}>.</Box>
      </Typography>
    </Toolbar>
  )

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh' }}>
      {/* Phase 22: same skip-link pattern as the public SiteNavbar. */}
      <Box
        component="a"
        href="#main-content"
        className="admin-skip-link"
        sx={{
          position: 'fixed', top: -100, left: 8, zIndex: 2000,
          bgcolor: '#171246', color: '#fff', px: 2, py: 1, borderRadius: 1,
          fontSize: 13, fontWeight: 600, textDecoration: 'none',
          '&:focus': { top: 8 },
        }}
      >
        Skip to content
      </Box>

      {isMobile && (
        <AppBar position="fixed" color="inherit" elevation={1} sx={{ zIndex: (t) => t.zIndex.drawer + 1 }}>
          <Toolbar sx={{ gap: 1 }}>
            <IconButton aria-label="Open menu" onClick={() => setMobileOpen(true)}>
              <MenuIcon size={22} />
            </IconButton>
            <Typography sx={{ fontWeight: 700 }}>EIS Platform Admin</Typography>
          </Toolbar>
        </AppBar>
      )}

      <Drawer
        variant={isMobile ? 'temporary' : 'permanent'}
        open={isMobile ? mobileOpen : true}
        onClose={() => setMobileOpen(false)}
        ModalProps={{ keepMounted: true }}
        sx={{
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': { width: DRAWER_WIDTH, boxSizing: 'border-box' },
        }}
      >
        {!isMobile && brand}
        {!isMobile && <Divider />}
        {navItems}
      </Drawer>

      <Box
        component="main"
        id="main-content"
        sx={{ flexGrow: 1, bgcolor: 'background.default', p: 4, mt: isMobile ? 7 : 0 }}
      >
        <Outlet />
      </Box>
    </Box>
  )
}
