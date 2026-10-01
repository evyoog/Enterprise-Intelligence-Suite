import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { Badge, IconButton, Tooltip } from '@mui/material'
import { ShoppingCart } from 'lucide-react'
import { useCart } from './CartContext'

/** C59 (REQ-MKT-003.7, application-layout.md "Top bar: cart icon"). */
export function CartButton() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const { count } = useCart()
  const label = count > 0 ? t('cart.badge', { count }) : t('cart.badgeEmpty')
  return (
    <Tooltip title={label}>
      <IconButton aria-label={label} onClick={() => navigate('/cart')}>
        <Badge badgeContent={count} color="primary" max={99} invisible={count === 0}
          slotProps={{ badge: { 'aria-hidden': true } as Record<string, unknown> }}>
          <ShoppingCart size={19} />
        </Badge>
      </IconButton>
    </Tooltip>
  )
}
