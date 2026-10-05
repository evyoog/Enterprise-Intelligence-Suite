import { Alert, Box, Button, LinearProgress, Paper, Typography } from '@mui/material'
import { useTranslation } from 'react-i18next'
import type { UploadProgress } from '../../api/knowledgeApi'
import { formatBytes } from './knowledgeUtils'

/**
 * Upload progress (knowledge-upload-progress screen): file name, size,
 * uploaded amount, percentage, bar, speed and remaining size, Cancel.
 * States: preparing, uploading, completed, failed, cancelled.
 */
export function UploadProgressCard({ fileName, progress, verifying, onCancel }: {
  fileName: string; progress: UploadProgress; verifying?: boolean; onCancel?: () => void
}) {
  const { t } = useTranslation()
  const percent = progress.total > 0 ? Math.round((progress.loaded / progress.total) * 100) : 0
  const remaining = progress.total - progress.loaded
  const active = progress.state === 'preparing' || progress.state === 'uploading'
  return (
    <Paper variant="outlined" sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 1 }} aria-live="polite">
      <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap' }}>
        <Typography sx={{ fontWeight: 600, wordBreak: 'break-all' }}>{fileName}</Typography>
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>{formatBytes(progress.total)}</Typography>
      </Box>
      <LinearProgress variant={progress.state === 'preparing' || verifying ? 'indeterminate' : 'determinate'} value={percent}
        aria-label={t('knowledge.admin.upload.uploading')} color={progress.state === 'failed' ? 'error' : 'primary'} />
      <Box sx={{ display: 'flex', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap' }}>
        <Typography variant="body2" sx={{ color: 'text.secondary' }}>
          {verifying ? t('knowledge.admin.upload.verifying')
            : progress.state === 'uploading'
              ? [`${percent}%`, t('knowledge.admin.upload.of', { loaded: formatBytes(progress.loaded), total: formatBytes(progress.total) }),
                progress.speed ? t('knowledge.admin.upload.speed', { speed: formatBytes(progress.speed) }) : null,
                t('knowledge.admin.upload.remaining', { size: formatBytes(remaining) })].filter(Boolean).join(' · ')
              : t(`knowledge.admin.upload.${progress.state}`)}
        </Typography>
        {active && onCancel && <Button size="small" color="inherit" onClick={onCancel}>{t('knowledge.admin.upload.cancel')}</Button>}
      </Box>
      {progress.state === 'failed' && progress.error && <Alert severity="error">{progress.error}</Alert>}
    </Paper>
  )
}
