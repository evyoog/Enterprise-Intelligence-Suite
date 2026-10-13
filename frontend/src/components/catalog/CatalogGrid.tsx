import type { ReactNode } from 'react'
import { Box } from '@mui/material'

/** C95 (REQ-CAT-003.13): the card grid. Cards are narrow (about 240 px), so as many as fit go on a row; the list view is one per row. */
export const CARD_MIN_WIDTH = 240

export function CatalogGrid({ list = false, children }: { list?: boolean; children: ReactNode }) {
  return (
    <Box sx={{
      display: 'grid', gap: 2,
      gridTemplateColumns: list ? '1fr' : `repeat(auto-fill, minmax(${CARD_MIN_WIDTH}px, 1fr))`,
    }}>
      {children}
    </Box>
  )
}
