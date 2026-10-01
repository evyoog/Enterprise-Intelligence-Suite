import { Box, Chip } from '@mui/material'
import { BRAND_NAMES, type PaymentBrand } from './paymentBrands'

/** C59, REQ-BIL-001.23 (docs/05-ui/screen-requirements/payment-brand-assets.md).
 *
 * Brand logos are official SVG files bundled in `src/assets/payment-logos/`
 * (named `<brand>.svg`, with `<brand>-dark.svg` for a dark-mode variant),
 * each with its source and licence recorded in that folder's
 * ATTRIBUTION.md. A brand without a recorded file is shown as its name in
 * a neutral chip — never a traced or recoloured logo, and never one loaded
 * from another website. */
const files = import.meta.glob('../../assets/payment-logos/*.svg', { eager: true, query: '?url', import: 'default' }) as Record<string, string>

function logoFile(brand: PaymentBrand, dark: boolean): string | undefined {
  const key = (name: string) => `../../assets/payment-logos/${name}.svg`
  return (dark ? files[key(`${brand}-dark`)] : undefined) ?? files[key(brand)]
}

export function PaymentLogo({ brand, height = 20, dark = false, decorative = false }: {
  brand: PaymentBrand; height?: number; dark?: boolean; decorative?: boolean
}) {
  const src = logoFile(brand, dark)
  const name = BRAND_NAMES[brand]
  if (src) {
    return <Box component="img" src={src} alt={decorative ? '' : name} sx={{ height, width: 'auto', display: 'block' }} />
  }
  return (
    <Chip
      label={name} size="small" variant="outlined" aria-hidden={decorative || undefined}
      sx={{ height: Math.max(height, 20), fontSize: 11, fontWeight: 600, borderRadius: 1, '& .MuiChip-label': { px: 0.75 } }}
    />
  )
}

export function PaymentLogos({ brands, height, dark, label }: {
  brands: PaymentBrand[]; height?: number; dark?: boolean; label?: string
}) {
  return (
    <Box role={label ? 'list' : undefined} aria-label={label} sx={{ display: 'flex', gap: 0.5, flexWrap: 'wrap', alignItems: 'center' }}>
      {brands.map((b) => (
        <Box key={b} role={label ? 'listitem' : undefined}><PaymentLogo brand={b} height={height} dark={dark} /></Box>
      ))}
    </Box>
  )
}
