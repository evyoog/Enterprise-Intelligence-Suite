import { ApiError } from '../../api/client'
import type { ContentType } from '../../api/knowledgeApi'

/** Block types the editor offers (REQ-KNW-002.2); the backend validates the same list. */
export const BLOCK_TYPES = [
  'heading', 'paragraph', 'bulleted_list', 'numbered_list', 'quote', 'image', 'gallery', 'video', 'audio', 'file', 'pdf', 'table',
  'code', 'callout', 'warning', 'note', 'step', 'accordion', 'faq', 'button', 'link', 'workflow_diagram', 'embed',
] as const

/** Where an item of each type opens in the Knowledge Center. */
export function itemPath(item: { id: number; slug?: string | null }) {
  return `/knowledge/content/${item.slug || item.id}`
}

/** Knowledge Center sections (prompt 5.5) and the content types each lists. */
export const SECTIONS: { key: string; path: string; types: ContentType[] }[] = [
  { key: 'gettingStarted', path: '/knowledge/getting-started', types: ['GETTING_STARTED'] },
  { key: 'guides', path: '/knowledge/guides', types: ['PRODUCT_GUIDE', 'ARTICLE'] },
  { key: 'videos', path: '/knowledge/videos', types: ['VIDEO'] },
  { key: 'troubleshooting', path: '/knowledge/troubleshooting', types: ['TROUBLESHOOTING', 'ERROR_CODE'] },
  { key: 'downloads', path: '/knowledge/downloads', types: ['DOCUMENT', 'TEMPLATE', 'STUDY_MATERIAL'] },
  { key: 'faqs', path: '/knowledge/faqs', types: ['FAQ'] },
  { key: 'workflows', path: '/knowledge/workflows', types: ['WORKFLOW_GUIDE'] },
  { key: 'releaseNotes', path: '/knowledge/release-notes', types: ['RELEASE_NOTE'] },
  { key: 'developer', path: '/knowledge/developer', types: ['DEVELOPER_DOC'] },
  { key: 'glossary', path: '/knowledge/glossary', types: ['GLOSSARY_TERM'] },
]

export function formatDuration(seconds: number | null | undefined) {
  if (seconds == null) return null
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = Math.floor(seconds % 60)
  const mm = h > 0 ? String(m).padStart(2, '0') : String(m)
  return `${h > 0 ? `${h}:` : ''}${mm}:${String(s).padStart(2, '0')}`
}

export function formatBytes(bytes: number) {
  if (bytes >= 1024 ** 3) return `${(bytes / 1024 ** 3).toFixed(1)} GB`
  if (bytes >= 1024 ** 2) return `${(bytes / 1024 ** 2).toFixed(1)} MB`
  if (bytes >= 1024) return `${Math.round(bytes / 1024)} KB`
  return `${bytes} B`
}

export function formatDate(iso: string | null | undefined, locale: string) {
  if (!iso) return ''
  try {
    return new Date(iso).toLocaleDateString(locale, { year: 'numeric', month: 'short', day: 'numeric' })
  } catch {
    return iso.slice(0, 10)
  }
}

/** Only EIS routes are used for direct actions (BR-KCEN-006). */
export function isEisRoute(route: string | null | undefined): route is string {
  return !!route && route.startsWith('/') && !route.startsWith('//')
}

const RECENT_KEY = 'knowledge.recentSearches'

/** Recently searched terms, kept only in this browser (no personal data on the server). */
export function recentSearches(): string[] {
  try {
    return JSON.parse(localStorage.getItem(RECENT_KEY) ?? '[]') as string[]
  } catch {
    return []
  }
}

export function rememberSearch(term: string) {
  const value = term.trim()
  if (!value) return
  try {
    const next = [value, ...recentSearches().filter((t) => t.toLowerCase() !== value.toLowerCase())].slice(0, 8)
    localStorage.setItem(RECENT_KEY, JSON.stringify(next))
  } catch {
    // storage unavailable: nothing to remember
  }
}

/** True only when the server answered "no such content" (404). Anything else is a failed load, not a missing item. */
export function isNotFound(error: unknown): boolean {
  return error instanceof ApiError && error.status === 404
}

/**
 * Runs a load and retries it on failures that are not a 404 (a network blip, a 5xx, or a 401 from a token that is
 * being refreshed), so a momentary problem does not show up as "This content is not available".
 */
export async function loadWithRetry<T>(load: () => Promise<T>, retries = 2, delayMs = 700): Promise<T> {
  for (let attempt = 0; ; attempt++) {
    try {
      return await load()
    } catch (error) {
      if (isNotFound(error) || attempt >= retries) throw error
      await new Promise((resolve) => setTimeout(resolve, delayMs * (attempt + 1)))
    }
  }
}
