import { Alert, Box, Button, CircularProgress, Paper, Typography } from '@mui/material'
import { useTranslation } from 'react-i18next'

/** C60: the sticky save bar under a settings form — unsaved warning, last
 * updated time, Cancel and Save (Save disabled until something changed). */
export function SaveBar({ dirty, busy, updatedAt, onSave, onCancel, error }: {
  dirty: boolean; busy: boolean; updatedAt?: string; onSave: () => void; onCancel: () => void; error?: string | null
}) {
  const { t } = useTranslation()
  return (
    <Paper elevation={dirty ? 6 : 0} variant={dirty ? 'elevation' : 'outlined'}
      sx={{ position: 'sticky', bottom: 16, zIndex: 5, mt: 3, p: 1.5, px: 2, borderRadius: 3, display: 'flex', flexDirection: 'column', gap: 1 }}>
      {error && <Alert severity="error">{error}</Alert>}
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, flexWrap: 'wrap' }}>
        <Typography variant="body2" role="status" sx={{ color: dirty ? 'warning.main' : 'text.secondary', fontWeight: dirty ? 600 : 400, flex: 1, minWidth: 180 }}>
          {dirty ? t('admin.billingSettings.unsaved')
            : updatedAt ? t('admin.billingSettings.lastUpdated', { date: new Date(updatedAt).toLocaleString() })
              : t('admin.billingSettings.notSavedYet')}
        </Typography>
        <Button disabled={!dirty || busy} onClick={onCancel}>{t('admin.billingSettings.cancel')}</Button>
        <Button variant="contained" disabled={!dirty || busy} onClick={onSave}
          startIcon={busy ? <CircularProgress size={16} color="inherit" /> : undefined}>
          {t('admin.billingSettings.save')}
        </Button>
      </Box>
    </Paper>
  )
}
