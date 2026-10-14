import { Avatar } from '@mui/material'
import { PaymentLogo } from './PaymentLogos'
import type { PaymentBrand } from './paymentBrands'
import { BRAND_NAMES } from './paymentBrands'

/** C98: a provider's logo when EIS has the official file (see components/payments/PaymentLogos), otherwise a neutral monogram. Never a traced or
 * remote logo. The backend supplies only a logo key; the screen needs no change for a new provider. */
export function GatewayLogo({ logo, name, size = 36, dark = false }: { logo: string; name: string; size?: number; dark?: boolean }) {
  if (logo in BRAND_NAMES) {
    return <PaymentLogo brand={logo as PaymentBrand} height={Math.round(size * 0.6)} dark={dark} decorative />
  }
  return (
    <Avatar variant="rounded" aria-hidden sx={{ width: size, height: size, fontSize: size * 0.4, fontWeight: 700, bgcolor: 'action.selected', color: 'text.primary' }}>
      {name.slice(0, 2).toUpperCase()}
    </Avatar>
  )
}
