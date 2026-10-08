import type { ContentKind, PublicContent } from '../../api/productContentApi'

export const CONTENT_KINDS: ContentKind[] = ['DATASHEET', 'DOCUMENTATION', 'IMAGE', 'VIDEO', 'CASE_STUDY']

export function formatSize(bytes: number) {
  if (bytes >= 1024 ** 2) return `${Math.round((bytes / 1024 ** 2) * 10) / 10} MB`
  if (bytes >= 1024) return `${Math.round(bytes / 1024)} KB`
  return `${bytes} B`
}

export function formatDay(iso: string, locale: string) {
  try {
    return new Intl.DateTimeFormat(locale, { dateStyle: 'medium' }).format(new Date(iso))
  } catch {
    return iso.slice(0, 10)
  }
}

/** The extension of a file name, lower case. */
export function extensionOf(name: string) {
  const dot = name.lastIndexOf('.')
  return dot < 0 ? '' : name.slice(dot + 1).toLowerCase()
}

/** Whether the product page has anything to show on its Resources tab. */
export function hasPublishedContent(content: PublicContent | null) {
  return !!content && (content.datasheets.length + content.documentation.length + content.images.length
    + content.videos.length + content.caseStudies.length) > 0
}

