import type { ReactNode } from 'react'
import { Box, Divider, Typography } from '@mui/material'

/** C68: width of the right-hand control column; every control starts at its left edge. */
export const CONTROL_COLUMN = 220

/**
 * C68: the content of one Preferences panel — title, short description, a
 * divider, then its rows. The panel surface itself is drawn by the page.
 */
export function PreferenceSection({ id, title, description, children }: {
  id: string; title: string; description: string; children: ReactNode
}) {
  return (
    <Box component="section" aria-labelledby={`${id}-title`} id={id}>
      <Typography id={`${id}-title`} component="h2" sx={{ fontWeight: 600, fontSize: 18, lineHeight: 1.4 }}>{title}</Typography>
      <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.5 }}>{description}</Typography>
      <Divider sx={{ mt: 2.5, mb: 0.5 }} />
      <Box>{children}</Box>
    </Box>
  )
}

/**
 * C68: a full-width row — label and hint on the left, the control in a
 * fixed-width column on the right, so all controls on a panel line up.
 * Stacked when the panel is narrow (container query on the page's panel). `labelFor` ties the label to a native input, `labelId`
 * names a control labelled by id (selects, switches). `children` render
 * under the label across the full width (e.g. the accent swatches).
 */
export function PreferenceRow({ label, hint, labelId, labelFor, control, children }: {
  label: string; hint?: ReactNode; labelId?: string; labelFor?: string; control?: ReactNode; children?: ReactNode
}) {
  return (
    <Box sx={{ py: 2.25, '& + &': { borderTop: '1px solid', borderColor: 'divider' } }}>
      <Box sx={{
        display: 'grid', columnGap: 4, rowGap: 1, alignItems: 'center', gridTemplateColumns: '1fr',
        // Two columns once the panel (not the window) is wide enough — the
        // panel's width depends on the app sidebar and the preferences nav.
        [`@container prefpanel (min-width: 560px)`]: { gridTemplateColumns: `minmax(0, 1fr) ${CONTROL_COLUMN}px` },
      }}>
        <Box sx={{ minWidth: 0, maxWidth: 640 }}>
          <Typography id={labelId} component={labelFor ? 'label' : 'div'} htmlFor={labelFor} variant="body2" sx={{ fontWeight: 600, display: 'block' }}>
            {label}
          </Typography>
          {hint && <Typography variant="body2" sx={{ color: 'text.secondary', mt: 0.25 }}>{hint}</Typography>}
        </Box>
        {control && (
          // The switch's built-in padding is pulled back so its track starts on the column edge like the other controls.
          <Box sx={{ display: 'flex', justifyContent: 'flex-start', alignItems: 'center', minWidth: 0, '& > .MuiSwitch-root': { ml: '-12px' } }}>{control}</Box>
        )}
      </Box>
      {children}
    </Box>
  )
}
