import { Layers as PHLayers } from 'lucide-react'
import { PageHeader } from '../../components/layout/PageHeader'
import { useEffect, useState } from 'react'
import { Box, Button, CircularProgress, Typography } from '@mui/material'
import { ArrowLeft } from 'lucide-react'
import { useParams, Link as RouterLink } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { platformsApi, type Platform } from '../../api/platformsApi'
import { PlatformForm } from '../../components/admin/PlatformForm'

/** "/admin/platforms/:id/edit" — reached from the Edit button on a platform's dashboard. */
export function EditPlatformPage() {
  const { id } = useParams<{ id: string }>()
  const [platform, setPlatform] = useState<Platform | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!id) return
    platformsApi.get(Number(id))
      .then(setPlatform)
      .catch((e) => setError(e instanceof ApiError ? e.message : 'Could not load this platform.'))
  }, [id])

  return (
    <Box sx={{ maxWidth: 560 }}>
      <Button component={RouterLink} to={`/admin/platforms/${id}`} startIcon={<ArrowLeft size={16} />} sx={{ mb: 2 }}>
        Back to Platform
      </Button>

      <PageHeader icon={PHLayers} accent="indigo" area="catalog" title="Edit platform"
        subtitle="Update this platform's name, description, or logo." />

      {error && <Typography color="error" role="alert">{error}</Typography>}

      {!error && !platform && (
        <Box sx={{ display: 'flex', justifyContent: 'center', py: 6 }}>
          <CircularProgress size={28} />
        </Box>
      )}

      {platform && (
        <PlatformForm
          initialPlatform={platform}
          onSubmit={(payload) => platformsApi.update(platform.id, payload)}
          submitLabel="Save Changes"
          submittingLabel="Saving…"
          successMessage="Platform updated."
        />
      )}
    </Box>
  )
}
