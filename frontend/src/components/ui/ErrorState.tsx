import { Alert, Button } from '@mui/material'
import { useTranslation } from 'react-i18next'

/** C66: a clear error with Retry. The message is the backend's own
 * user-facing message, never a stack trace. */
export function ErrorState({ title, message, onRetry }: { title: string; message?: string; onRetry?: () => void }) {
  const { t } = useTranslation()
  return (
    <Alert severity="error" action={onRetry && <Button color="inherit" onClick={onRetry}>{t('ui.retry')}</Button>}>
      <strong>{title}</strong>{message ? ` — ${message}` : ''}
    </Alert>
  )
}
