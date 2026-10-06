import { useRef, useState } from 'react'
import { contentTypeOf, uploadToStorage, type Media, type UploadProgress, type UploadRequest, type UploadTicket } from '../../api/knowledgeApi'

/**
 * The mandatory upload flow (prompt 7.3): send metadata → get a presigned
 * URL for a new key → upload straight to storage with progress → report
 * completion → the backend verifies the object. Cancel aborts the transfer
 * and the upload record. The UI never freezes: everything is async.
 */
export function useDirectUpload(requestTicket: (r: UploadRequest) => Promise<UploadTicket>,
                                complete: (mediaId: number, partEtags?: string[]) => Promise<Media>,
                                abort: (mediaId: number) => Promise<unknown>) {
  const [fileName, setFileName] = useState<string | null>(null)
  const [progress, setProgress] = useState<UploadProgress | null>(null)
  const [verifying, setVerifying] = useState(false)
  const cancelRef = useRef<() => void>(() => undefined)

  const start = async (file: File, meta: Omit<UploadRequest, 'fileName' | 'contentType' | 'size'>): Promise<Media | null> => {
    const contentType = contentTypeOf(file)
    setFileName(file.name)
    setProgress({ state: 'preparing', loaded: 0, total: file.size, speed: null })
    let ticket: UploadTicket
    try {
      ticket = await requestTicket({ ...meta, fileName: file.name, contentType, size: file.size })
    } catch (e) {
      setProgress({ state: 'failed', loaded: 0, total: file.size, speed: null, error: (e as Error).message })
      return null
    }
    let etags: string[] | undefined
    const handle = uploadToStorage(ticket, file, contentType, setProgress, (tags) => { etags = tags })
    let cancelled = false
    cancelRef.current = () => { cancelled = true; handle.cancel() }
    try {
      await handle.promise
    } catch (e) {
      abort(ticket.mediaId).catch(() => undefined)
      setProgress({ state: cancelled ? 'cancelled' : 'failed', loaded: 0, total: file.size, speed: null,
        error: cancelled ? undefined : (e as Error).message })
      return null
    }
    setVerifying(true)
    try {
      const media = await complete(ticket.mediaId, etags)
      setProgress({ state: 'completed', loaded: file.size, total: file.size, speed: null })
      return media
    } catch (e) {
      setProgress({ state: 'failed', loaded: file.size, total: file.size, speed: null, error: (e as Error).message })
      return null
    } finally {
      setVerifying(false)
    }
  }

  return { start, cancel: () => cancelRef.current(), fileName, progress, verifying, reset: () => { setProgress(null); setFileName(null) } }
}
