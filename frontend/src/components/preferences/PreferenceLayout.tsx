import type { ReactNode } from 'react'
import { Box, Typography } from '@mui/material'

/**
 * C67: one section of the Preferences page — a heading and a short
 * description above its rows. No card or icon: sections sit in one bordered
 * container and are separated by dividers.
 */
export function PreferenceSection({ id, title, description, children }: {
  id: string; title: string; description: string; children: ReactNode
}) {
  return (
    <Box component="section" aria-labelledby={`${id}-title`} id={id} sx={{ px: { xs: 2, sm: 4 }, py: { xs: 3, sm: 3.5 } }}>
      <Typography id={`${id}-title`} component="h2" sx={{ fontWeight: 600, fontSize: 17, lineHeight: 1.4 }}>{title}</Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.25, mb: 1 }}>{description}</Typography>
      <Box>{children}</Box>
    </Box>
  )
}

/**
 * C67: a label (and optional hint) on the left, its control on the right;
 * stacked on narrow screens. `labelFor` ties the label to a native input,
 * `labelId` to a control labelled by id (selects, switches).
 */
export function PreferenceRow({ label, hint, labelId, labelFor, control, children }: {
  label: string; hint?: ReactNode; labelId?: string; labelFor?: string; control?: ReactNode; children?: ReactNode
}) {
  return (
    <Box sx={{ py: 1.75, '& + &': { borderTop: '1px solid', borderColor: 'divider' } }}>
      <Box sx={{
        display: 'grid', gap: { xs: 1, sm: 3 }, alignItems: 'center',
        gridTemplateColumns: { xs: '1fr', sm: 'minmax(0, 1fr) 260px' },
      }}>
        <Box sx={{ minWidth: 0 }}>
          <Typography id={labelId} component={labelFor ? 'label' : 'div'} htmlFor={labelFor} variant="body2" sx={{ fontWeight: 600, display: 'block' }}>
            {label}
          </Typography>
          {hint && <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.25 }}>{hint}</Typography>}
        </Box>
        {control && <Box sx={{ display: 'flex', justifyContent: { xs: 'flex-start', sm: 'flex-end' }, minWidth: 0 }}>{control}</Box>}
      </Box>
      {children}
    </Box>
  )
}
