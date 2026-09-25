import { Box, CircularProgress } from '@mui/material'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { HomePage } from './pages/HomePage'
import { ProductsPage } from './pages/ProductsPage'
import { MyProductsPage } from './pages/MyProductsPage'
import { BusinessDashboardPage } from './pages/BusinessDashboardPage'
import { SecuritySettingsPage } from './pages/SecuritySettingsPage'
import { PreferencesPage } from './pages/PreferencesPage'
import { OrganizationSamlProvidersPage } from './pages/OrganizationSamlProvidersPage'
import { OrganizationRegisterPage } from './pages/register/OrganizationRegisterPage'
import { CheckEmailPage } from './pages/register/CheckEmailPage'
import { VerifyEmailPage } from './pages/register/VerifyEmailPage'
import { ForgotPasswordPage } from './pages/ForgotPasswordPage'
import { ResetPasswordPage } from './pages/ResetPasswordPage'
import { AdminLayout } from './pages/admin/AdminLayout'
import { AdminProductsPage } from './pages/admin/AdminProductsPage'
import { EditProductPage } from './pages/admin/EditProductPage'
import { PlatformsListPage } from './pages/admin/PlatformsListPage'
import { PlatformDashboardPage } from './pages/admin/PlatformDashboardPage'
import { EditPlatformPage } from './pages/admin/EditPlatformPage'
import { ProductSettingsPage } from './pages/admin/settings/ProductSettingsPage'
import { PlatformSettingsPage } from './pages/admin/settings/PlatformSettingsPage'
import { CommonSettingsPage } from './pages/admin/settings/CommonSettingsPage'
import { RegistrationsAdminPage } from './pages/admin/RegistrationsAdminPage'
import { AdminAuditLogPage } from './pages/admin/AdminAuditLogPage'
import { AdminPrivilegedAccessPage } from './pages/admin/AdminPrivilegedAccessPage'
import { RolesAdminPage } from './pages/admin/RolesAdminPage'
import { PermissionsAdminPage } from './pages/admin/PermissionsAdminPage'
import { RequireAdmin } from './components/routing/RequireAdmin'
import { RequireAuth } from './components/routing/RequireAuth'
import { useAuth } from './auth/AuthProvider'
import { AuthModalProvider } from './auth/AuthModalContext'
import { PreferenceSync } from './theming/PreferenceSync'

function App() {
  return (
    <BrowserRouter>
      <MainApp />
    </BrowserRouter>
  )
}

function MainApp() {
  const auth = useAuth()

  // Wait for the one silent session check on load before rendering any real
  // route — otherwise a signed-in user reloading the page would flash the
  // "Login" nav link for a moment before it corrects itself.
  if (auth.isBootstrapping) {
    return (
      <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <CircularProgress size={28} />
      </Box>
    )
  }

  return (
    <AuthModalProvider>
      <PreferenceSync />
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/products" element={<ProductsPage />} />
        <Route path="/my/products" element={<RequireAuth><MyProductsPage /></RequireAuth>} />
        <Route path="/organization/business-dashboard" element={<RequireAuth><BusinessDashboardPage /></RequireAuth>} />
        <Route path="/account/security" element={<RequireAuth><SecuritySettingsPage /></RequireAuth>} />
        {/* Deliberately NOT behind RequireAuth — see that component's own
            doc on why preferences stay reachable signed out. */}
        <Route path="/account/preferences" element={<PreferencesPage />} />
        <Route path="/organization/identity-federation" element={<RequireAuth><OrganizationSamlProvidersPage /></RequireAuth>} />

        {/* Login stays a modal (see AuthModalProvider) — this bare path just
            sends anyone with an old bookmark back to "/" where the Navbar's
            Login button is. Register is a real, routed page (the emailed
            verify link needs a real URL to open, and check-email/verify
            already are). Organization registration is the ONLY registration
            path now — individual self-registration was removed — so "/register"
            itself goes straight there instead of a type-select screen. */}
        <Route path="/login" element={<Navigate to="/" replace />} />
        <Route path="/register" element={<Navigate to="/register/organization" replace />} />
        <Route path="/register/organization" element={<OrganizationRegisterPage />} />
        <Route path="/register/check-email" element={<CheckEmailPage />} />
        <Route path="/register/verify" element={<VerifyEmailPage />} />

        {/* Phase 8 — real routed pages, not modal state, since the emailed
            reset link must open directly to a URL carrying its own token. */}
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />

        {/* Nested routes: AdminLayout (sidebar) renders once, the child pages
            swap in and out of its <Outlet/> as the path changes. "settings" is
            a category (see AdminLayout's expandable Settings menu), so a bare
            /admin/settings visit redirects to its one current sub-page. */}
        <Route
          path="/admin"
          element={
            <RequireAdmin>
              <AdminLayout />
            </RequireAdmin>
          }
        >
          {/* Platforms, not the flat app list, is the admin landing page —
              grouping is the primary mental model here. The flat cross-platform
              list still exists at its own path for when that's actually needed. */}
          <Route index element={<PlatformsListPage />} />
          <Route path="apps" element={<AdminProductsPage />} />
          <Route path="products/:id/edit" element={<EditProductPage />} />
          <Route path="platforms" element={<PlatformsListPage />} />
          <Route path="platforms/:id" element={<PlatformDashboardPage />} />
          <Route path="platforms/:id/edit" element={<EditPlatformPage />} />
          <Route path="settings" element={<Navigate to="/admin/settings/product" replace />} />
          <Route path="settings/product" element={<ProductSettingsPage />} />
          <Route path="settings/platform" element={<PlatformSettingsPage />} />
          <Route path="settings/common" element={<CommonSettingsPage />} />
          <Route path="registrations" element={<RegistrationsAdminPage />} />
          <Route path="audit-log" element={<AdminAuditLogPage />} />
          <Route path="privileged-access" element={<AdminPrivilegedAccessPage />} />
          <Route path="roles" element={<RolesAdminPage />} />
          <Route path="permissions" element={<PermissionsAdminPage />} />
        </Route>
      </Routes>
    </AuthModalProvider>
  )
}

export default App
