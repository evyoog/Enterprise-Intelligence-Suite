import type { ReactNode } from 'react'
import { Box, Button, CircularProgress } from '@mui/material'
import { UserX } from 'lucide-react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink } from 'react-router-dom'
import { useCustomerWorkspace } from '../../auth/useCustomerWorkspace'
import { EmptyState } from '../ui/EmptyState'

/**
 * Route guard for pages about "my" data (cart, wishlist, my applications, ...). The platform admin has no customer account, so those pages
 * would only fail with "No Vyoog account is linked to this login"; this says so plainly and points to the admin area instead. Use it inside
 * RequireAuth. A UX convenience, not the security boundary: the backend answers 404 for such an account either way.
 */
export function RequireCustomer({ children }: { children: ReactNode }) {
  const { t } = useTranslation()
  const workspace = useCustomerWorkspace()
  if (workspace === null) {
    return <Box sx={{ display: 'grid', placeItems: 'center', py: 6 }}><CircularProgress aria-label={t('knowledge.common.loading')} /></Box>
  }
  if (!workspace) {
    return (
      <EmptyState icon={UserX} title={t('customerOnly.title')} description={t('customerOnly.description')}
        action={<Button variant="contained" component={RouterLink} to="/admin">{t('customerOnly.toAdmin')}</Button>} />
    )
  }
  return children
}
