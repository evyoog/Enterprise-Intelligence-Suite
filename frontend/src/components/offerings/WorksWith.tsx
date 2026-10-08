import { useEffect, useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Box, Chip, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { offeringsApi, type WorksWith as WorksWithItem } from '../../api/offeringsApi'

/** REQ-CAT-005 (OF-7): the products this one works with, on the product page. Renders nothing when there are none. */
export function WorksWith({ productId }: { productId: number }) {
  const { t } = useTranslation()
  const [items, setItems] = useState<WorksWithItem[]>([])

  useEffect(() => {
    let cancelled = false
    offeringsApi.worksWith(productId).then((r) => { if (!cancelled) setItems(r) }).catch(() => { if (!cancelled) setItems([]) })
    return () => { cancelled = true }
  }, [productId])

  if (items.length === 0) return null
  return (
    <Box sx={{ mb: 4 }}>
      <Typography variant="h6" component="h5" sx={{ fontWeight: 700, mb: 1.5 }}>{t('offerings.worksWith.title')}</Typography>
      <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }} role="list">
        {items.map((i) => (
          <Box key={i.id} role="listitem">
            <Chip component={RouterLink} to={`/products/${i.id}`} clickable label={i.name} />
          </Box>
        ))}
      </Box>
    </Box>
  )
}
