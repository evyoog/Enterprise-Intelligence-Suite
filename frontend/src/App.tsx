import { Box, CircularProgress } from '@mui/material'
import { BrowserRouter, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import { HomePage } from './pages/HomePage'
import { ProductsPage } from './pages/ProductsPage'
import { MyProductsPage } from './pages/MyProductsPage'
import { MySubscriptionsPage } from './pages/MySubscriptionsPage'
import { BusinessDashboardPage } from './pages/BusinessDashboardPage'
import { SecuritySettingsPage } from './pages/SecuritySettingsPage'
import { PreferencesPage } from './pages/PreferencesPage'
import { OrganizationSamlProvidersPage } from './pages/OrganizationSamlProvidersPage'
import { OrganizationRegisterPage } from './pages/register/OrganizationRegisterPage'
import { CheckEmailPage } from './pages/register/CheckEmailPage'
import { VerifyEmailPage } from './pages/register/VerifyEmailPage'
import { ForgotPasswordPage } from './pages/ForgotPasswordPage'
import { ResetPasswordPage } from './pages/ResetPasswordPage'
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
import { ServiceStatusAdminPage } from './pages/admin/ServiceStatusAdminPage'
import { ServiceStatusPage } from './pages/ServiceStatusPage'
import { OrganizationOrdersPage } from './pages/OrganizationOrdersPage'
import { KnowledgeBasePage } from './pages/KnowledgeBasePage'
import { AdminKnowledgeBasePage } from './pages/admin/AdminKnowledgeBasePage'
import { MyTicketsPage } from './pages/MyTicketsPage'
import { AdminSupportTicketsPage } from './pages/admin/AdminSupportTicketsPage'
import { GlobalSearchPage } from './pages/GlobalSearchPage'
import { ProductDetailPage } from './pages/ProductDetailPage'
import { AdminReviewsPage } from './pages/admin/AdminReviewsPage'
import { ProviderApplicationPage } from './pages/ProviderApplicationPage'
import { AdminPartnersPage } from './pages/admin/AdminPartnersPage'
import { BillingPage, OrganizationBillingPage } from './pages/BillingPage'
import { AdminBillingPage } from './pages/admin/AdminBillingPage'
import { AdminPaymentGatewayPage } from './pages/admin/AdminPaymentGatewayPage'
import { AdminPartnerDetailPage } from './pages/admin/AdminPartnerDetailPage'
import { RequireAdmin } from './components/routing/RequireAdmin'
import { RequireAuth } from './components/routing/RequireAuth'
import { AuthAwareLayout, PublicOnly } from './components/layout/AppShell'
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
        {/* Public website: visitors only. A signed-in user opening "/" goes
            straight to the software tool (see PublicOnly / appHomePath). */}
        <Route path="/" element={<PublicOnly><HomePage /></PublicOnly>} />

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

        {/* The software tool. Signed in, every route below renders inside
            AppShell (sidebar filtered by role and permissions, no website
            header). Signed out, the catalog and preferences still render as
            public pages, and the RequireAuth / RequireAdmin guards send
            anyone else back to "/". */}
        <Route element={<AuthAwareLayout />}>
          <Route path="/products" element={<ProductsPage />} />
          {/* 03.04 Reviews & Ratings (sprint 2027.1.3): public product detail. */}
          <Route path="/products/:id" element={<ProductDetailPage />} />
          {/* 01.03 Global Search (sprint 2027.1.3): public; ticket results need a sign-in. */}
          <Route path="/search" element={<GlobalSearchPage />} />
          {/* 14.01.01.01 Register provider (sprint 2027.2.1): public, no Vyoog account required. */}
          <Route path="/partners/apply" element={<ProviderApplicationPage />} />
          {/* Deliberately NOT behind RequireAuth — see that component's own
              doc on why preferences stay reachable signed out. */}
          <Route path="/account/preferences" element={<PreferencesPage />} />
          <Route path="/my/products" element={<RequireAuth><MyProductsPage /></RequireAuth>} />
          <Route path="/my/subscriptions" element={<RequireAuth><MySubscriptionsPage /></RequireAuth>} />
          <Route path="/organization/business-dashboard" element={<RequireAuth><BusinessDashboardPage /></RequireAuth>} />
          <Route path="/account/security" element={<RequireAuth><SecuritySettingsPage /></RequireAuth>} />
          <Route path="/organization/identity-federation" element={<RequireAuth><OrganizationSamlProvidersPage /></RequireAuth>} />
          {/* REQ-PRT-001 (C26): signed-in customers. */}
          <Route path="/status" element={<RequireAuth><ServiceStatusPage /></RequireAuth>} />
          {/* 11.01 Knowledge Base (sprint 2027.1.1): public, same as /products above. */}
          <Route path="/knowledge-base" element={<KnowledgeBasePage />} />
          {/* 09 Order & Provisioning Management (sprint 2027.1.1): organization purchasing only. */}
          <Route path="/organization/orders" element={<RequireAuth><OrganizationOrdersPage /></RequireAuth>} />
          {/* 12.01 Ticket Management (sprint 2027.1.2): any authenticated customer. */}
          <Route path="/support/tickets" element={<RequireAuth><MyTicketsPage /></RequireAuth>} />
          {/* 08 Billing & Payments (sprint 2026.4.3, REQ-BIL-001, C46). */}
          <Route path="/billing" element={<RequireAuth><BillingPage /></RequireAuth>} />
          <Route path="/organization/billing" element={<RequireAuth><OrganizationBillingPage /></RequireAuth>} />

          {/* "settings" is a category (see the sidebar's expandable Settings
              group), so a bare /admin/settings visit redirects to its first
              sub-page. */}
          <Route path="/admin" element={<RequireAdmin><Outlet /></RequireAdmin>}>
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
            <Route path="service-status" element={<ServiceStatusAdminPage />} />
            <Route path="knowledge-base" element={<AdminKnowledgeBasePage />} />
            <Route path="support/tickets" element={<AdminSupportTicketsPage />} />
            <Route path="reviews" element={<AdminReviewsPage />} />
            <Route path="partners" element={<AdminPartnersPage />} />
            <Route path="partners/:id" element={<AdminPartnerDetailPage />} />
            <Route path="billing" element={<AdminBillingPage />} />
            <Route path="billing/payment-gateway" element={<AdminPaymentGatewayPage />} />
          </Route>
        </Route>
      </Routes>
    </AuthModalProvider>
  )
}

export default App
