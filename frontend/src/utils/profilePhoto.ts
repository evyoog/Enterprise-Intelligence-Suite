/**
 * "My profile" photo (C107): the browser checks the file, crops it to a centred square, scales it
 * down to at most 512 x 512 on a canvas and re-encodes it as WebP (JPEG where the browser cannot
 * write WebP), so what reaches the server is small and always a type it accepts. The server checks
 * type, size and the file's real content again.
 */

export const PHOTO_MAX_BYTES = 1024 * 1024
export const PHOTO_TARGET_SIZE = 512
/** Phone cameras produce large files; the limit that matters (1 MB) applies to the scaled result. */
export const PHOTO_MAX_SOURCE_BYTES = 20 * 1024 * 1024
export const PHOTO_ACCEPTED_TYPES = ['image/jpeg', 'image/png', 'image/webp'] as const

export type PhotoErrorCode = 'type' | 'sourceTooLarge' | 'tooLarge' | 'unreadable'

export class PhotoError extends Error {
  code: PhotoErrorCode
  constructor(code: PhotoErrorCode) {
    super(code)
    this.code = code
  }
}

/** The error code for a file that cannot be used at all, or null. SVG and every other type are refused. */
export function checkPhotoFile(file: { type: string; size: number }): PhotoErrorCode | null {
  if (!(PHOTO_ACCEPTED_TYPES as readonly string[]).includes(file.type)) return 'type'
  if (file.size > PHOTO_MAX_SOURCE_BYTES) return 'sourceTooLarge'
  return null
}

/** The centred square to cut from a width x height image, and the output side (never upscaled). */
export function squareCrop(width: number, height: number, target = PHOTO_TARGET_SIZE) {
  const side = Math.min(width, height)
  return { sx: Math.floor((width - side) / 2), sy: Math.floor((height - side) / 2), side, out: Math.min(side, target) }
}

async function decode(file: Blob): Promise<{ source: CanvasImageSource; width: number; height: number; close: () => void }> {
  if (typeof createImageBitmap === 'function') {
    try {
      const bitmap = await createImageBitmap(file, { imageOrientation: 'from-image' })
      return { source: bitmap, width: bitmap.width, height: bitmap.height, close: () => bitmap.close() }
    } catch {
      // fall through to the <img> decoder
    }
  }
  const url = URL.createObjectURL(file)
  try {
    const img = new Image()
    img.src = url
    await img.decode()
    return { source: img, width: img.naturalWidth, height: img.naturalHeight, close: () => {} }
  } catch {
    throw new PhotoError('unreadable')
  } finally {
    URL.revokeObjectURL(url)
  }
}

function toBlob(canvas: HTMLCanvasElement, type: string, quality: number): Promise<Blob | null> {
  return new Promise((resolve) => canvas.toBlob(resolve, type, quality))
}

/** Validates, crops, scales and re-encodes. Throws {@link PhotoError}. */
export async function prepareProfilePhoto(file: File): Promise<{ blob: Blob; filename: string }> {
  const invalid = checkPhotoFile(file)
  if (invalid) throw new PhotoError(invalid)

  const image = await decode(file)
  try {
    if (!image.width || !image.height) throw new PhotoError('unreadable')
    const { sx, sy, side, out } = squareCrop(image.width, image.height)
    const canvas = document.createElement('canvas')
    canvas.width = out
    canvas.height = out
    const ctx = canvas.getContext('2d')
    if (!ctx) throw new PhotoError('unreadable')
    // Transparent PNGs would turn black in JPEG; a white background reads as "no photo" edges.
    ctx.fillStyle = '#ffffff'
    ctx.fillRect(0, 0, out, out)
    ctx.imageSmoothingQuality = 'high'
    ctx.drawImage(image.source, sx, sy, side, side, 0, 0, out, out)

    for (const quality of [0.86, 0.72, 0.55]) {
      let blob = await toBlob(canvas, 'image/webp', quality)
      // A browser that cannot write WebP silently returns PNG.
      if (!blob || blob.type !== 'image/webp') blob = await toBlob(canvas, 'image/jpeg', quality)
      if (!blob) throw new PhotoError('unreadable')
      if (blob.size <= PHOTO_MAX_BYTES) {
        return { blob, filename: blob.type === 'image/webp' ? 'photo.webp' : 'photo.jpg' }
      }
    }
    throw new PhotoError('tooLarge')
  } finally {
    image.close()
  }
}
