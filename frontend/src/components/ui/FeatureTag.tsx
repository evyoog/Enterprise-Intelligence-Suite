import { Box } from '@mui/material'

/** C66: a small neutral feature label on cards and details. */
export function FeatureTag({ label }: { label: string }) {
  return (
    <Box component="span" sx={{
      display: 'inline-block', px: 1, py: 0.25, borderRadius: 1.5, fontSize: 12, fontWeight: 500, lineHeight: 1.6,
      color: 'text.secondary', bgcolor: 'action.hover', border: '1px solid', borderColor: 'divider', whiteSpace: 'nowrap',
    }}>
      {label}
    </Box>
  )
}
