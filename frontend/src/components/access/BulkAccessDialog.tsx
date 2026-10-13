import { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, List, ListItem, ListItemText, MenuItem, Radio, RadioGroup, TextField, Typography } from '@mui/material'
import { accessApi, type BulkResult, type PermissionCatalogEntry } from '../../api/accessApi'
import { ApiError } from '../../api/client'

interface Props {
  open: boolean
  memberIds: number[]
  products: { productId: number; name: string }[]
  permissions: PermissionCatalogEntry[]
  onClose: () => void
  onDone: () => void
}

/** Bulk change (REQ-TEN-005.8): grant or remove one product or permission for the selected members, with a preview. */
export function BulkAccessDialog({ open, memberIds, products, permissions, onClose, onDone }: Props) {
  const { t } = useTranslation()
  const [action, setAction] = useState<'GRANT' | 'REMOVE'>('GRANT')
  const [item, setItem] = useState('')
  const [result, setResult] = useState<BulkResult | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const reset = () => { setResult(null); setError(null) }
  const run = (preview: boolean) => {
    setBusy(true)
    setError(null)
    accessApi.bulk({ action, item, memberIds, preview })
      .then((r) => { setResult(r); if (!preview) onDone() })
      .catch((e) => setError(e instanceof ApiError ? e.message : t('access.bulk.error')))
      .finally(() => setBusy(false))
  }
  const finished = result !== null && !result.preview

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm" aria-labelledby="bulk-access-title">
      <DialogTitle id="bulk-access-title">{t('access.bulk.title', { count: memberIds.length })}</DialogTitle>
      <DialogContent>
        <RadioGroup row value={action} onChange={(e) => { setAction(e.target.value as 'GRANT' | 'REMOVE'); reset() }} aria-label={t('access.bulk.action')}>
          <FormControlLabel value="GRANT" control={<Radio />} label={t('access.bulk.grant')} />
          <FormControlLabel value="REMOVE" control={<Radio />} label={t('access.bulk.remove')} />
        </RadioGroup>
        <TextField select fullWidth size="small" label={t('access.bulk.item')} value={item} onChange={(e) => { setItem(e.target.value); reset() }} sx={{ mt: 1 }}>
          {products.map((p) => <MenuItem key={`p${p.productId}`} value={`product:${p.productId}`}>{t('access.bulk.productOption', { name: p.name })}</MenuItem>)}
          {permissions.filter((p) => p.callerCanGrant).map((p) => <MenuItem key={p.code} value={`permission:${p.code}`}>{p.name}</MenuItem>)}
        </TextField>
        {error && <Alert severity="error" sx={{ mt: 1.5 }}>{error}</Alert>}
        {result && (
          <Box sx={{ mt: 2 }}>
            <Alert severity={finished ? 'success' : 'info'}>
              {t(finished ? 'access.bulk.done' : 'access.bulk.previewSummary', {
                action: t(`access.bulk.${result.action === 'GRANT' ? 'grantVerb' : 'removeVerb'}`), item: result.itemName,
                applied: result.applied, skipped: result.skipped.length,
              })}
            </Alert>
            {result.skipped.length > 0 && (
              <>
                <Typography variant="subtitle2" component="h3" sx={{ mt: 1.5 }}>{t('access.bulk.skipped')}</Typography>
                <List dense>
                  {result.skipped.map((s) => (
                    <ListItem key={s.memberId} disableGutters><ListItemText primary={s.name || `#${s.memberId}`} secondary={s.reason} /></ListItem>
                  ))}
                </List>
              </>
            )}
          </Box>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>{finished ? t('access.close') : t('access.cancel')}</Button>
        {!finished && <Button disabled={!item || busy} onClick={() => run(true)}>{t('access.bulk.preview')}</Button>}
        {!finished && <Button variant="contained" disabled={!item || busy || !result?.preview || result.applied === 0} onClick={() => run(false)}>{t('access.bulk.apply')}</Button>}
      </DialogActions>
    </Dialog>
  )
}
