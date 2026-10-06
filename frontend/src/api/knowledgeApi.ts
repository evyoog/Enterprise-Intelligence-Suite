import { apiRequest } from './client'

// Knowledge Center (REQ-KNW-005) and Knowledge Management (REQ-KNW-002–008).
// The browser never talks to AWS: it asks the backend for a short-lived
// presigned URL and uploads/downloads with it (BR-MED-001). No credentials,
// storage keys or permanent URLs are kept here or in browser storage.

export type ContentType =
  | 'ARTICLE' | 'GETTING_STARTED' | 'PRODUCT_GUIDE' | 'VIDEO' | 'DOCUMENT' | 'TEMPLATE' | 'STUDY_MATERIAL'
  | 'RELEASE_NOTE' | 'FAQ' | 'TROUBLESHOOTING' | 'ERROR_CODE' | 'GLOSSARY_TERM' | 'WORKFLOW_GUIDE' | 'DEVELOPER_DOC' | 'COURSE'
export type WorkflowState = 'DRAFT' | 'IN_REVIEW' | 'APPROVED' | 'SCHEDULED' | 'PUBLISHED' | 'DEPRECATED' | 'ARCHIVED'
export type Audience = 'PUBLIC' | 'CUSTOMER' | 'ORGANIZATION' | 'ADMIN'
export type VideoSource = 'YOUTUBE' | 'AWS_S3' | 'EXTERNAL_URL'
export type MediaKind = 'IMAGE' | 'DOCUMENT' | 'AUDIO' | 'TEMPLATE' | 'VIDEO_FILE' | 'THUMBNAIL' | 'TRANSCRIPT' | 'SUBTITLE'

export const CONTENT_TYPES: ContentType[] = [
  'ARTICLE', 'GETTING_STARTED', 'PRODUCT_GUIDE', 'VIDEO', 'DOCUMENT', 'TEMPLATE', 'STUDY_MATERIAL', 'RELEASE_NOTE',
  'FAQ', 'TROUBLESHOOTING', 'ERROR_CODE', 'GLOSSARY_TERM', 'WORKFLOW_GUIDE', 'DEVELOPER_DOC',
]

/** One content block (REQ-KNW-002.2). Text is always rendered as text, never HTML. */
export interface Block {
  type: string
  id?: string
  text?: string
  title?: string
  level?: number
  items?: (string | { title: string; text: string })[]
  mediaId?: number
  mediaIds?: number[]
  contentId?: number
  caption?: string
  alt?: string
  label?: string
  url?: string
  language?: string
  rows?: string[][]
  question?: string
  answer?: string
  steps?: { title: string; contentId?: number; route?: string }[]
}

export type TypeFields = Record<string, string | string[]>

export interface VideoInfo {
  sourceType: VideoSource
  videoUrl: string | null
  youtubeId: string | null
  mediaId: number | null
  thumbnailMediaId: number | null
  thumbnailUrl: string | null
  durationSeconds: number | null
  channel: string | null
  transcript: string | null
  chapters: { seconds: number; title: string }[] | null
  subtitleLanguages: string[]
}

export interface Summary {
  id: number
  contentType: ContentType
  slug: string | null
  title: string
  shortDescription: string | null
  productId: number | null
  productName: string | null
  productSlug: string | null
  moduleId: number | null
  moduleName: string | null
  categoryName: string | null
  tags: string[]
  readingMinutes: number
  views: number
  versionLabel: string
  updatedAt: string
  featured: boolean
  deprecated: boolean
  difficulty: string | null
  videoSource: VideoSource | null
  durationSeconds: number | null
  thumbnailUrl: string | null
}

export interface Item {
  id: number
  contentType: ContentType
  slug: string | null
  title: string
  shortDescription: string | null
  blocks: Block[]
  typeFields: TypeFields | null
  productId: number | null
  productName: string | null
  productSlug: string | null
  moduleId: number | null
  moduleName: string | null
  categoryName: string | null
  feature: string | null
  tags: string[]
  versionLabel: string
  productVersion: string | null
  documentationVersion: string | null
  publishedAt: string | null
  updatedAt: string | null
  readingMinutes: number
  views: number
  deprecated: boolean
  difficulty: string | null
  directActionRoute: string | null
  directActionLabel: string | null
  related: Summary[]
  video: VideoInfo | null
  bookmarked: boolean
  preview: boolean
}

export interface Page<T> { items: T[]; totalElements: number; page: number; size: number }
export interface ModuleCard { id: number; name: string; slug: string; contentCount: number }
export interface ProductCard {
  id: number; name: string; slug: string; description: string | null; catalogProductId: number | null
  modules: ModuleCard[]; contentCount: number; hasAccess: boolean
}
export interface Home {
  sectionCounts: Record<string, number>
  recommended: Summary[]
  personalized: boolean
  popularGuides: Summary[]
  featuredVideos: Summary[]
  popularSearches: string[]
  products: ProductCard[]
}
export interface ProductHub { product: ProductCard; modules: { module: ModuleCard; items: Summary[] }[]; general: Summary[] }
export interface GlossaryTerm { id: number; slug: string; term: string; definition: string; synonyms: string[] }
export interface Personal {
  continueLearning: { item: Summary; percent: number; positionSeconds: number | null; lastViewedAt: string; completed: boolean }[]
  recentlyViewed: Summary[]
  bookmarks: Summary[]
}
export interface SearchResult {
  query: string
  results: Summary[]
  byType: Partial<Record<ContentType, Summary[]>>
  products: ProductCard[]
  modules: { id: number; name: string; slug: string; productName: string; productSlug: string }[]
  didYouMean: string | null
  semanticStatus: string
  tookMs: number
}
export interface PlayInfo {
  sourceType: VideoSource; embedId: string | null; url: string | null; expiresAt: string | null
  subtitles: Record<string, string>
}
export interface TemporaryUrl { url: string; expiresAt: string; fileName: string; size: number }

const q = (params: Record<string, string | number | undefined | null | (string | number)[]>) => {
  const s = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v === undefined || v === null || v === '') return
    if (Array.isArray(v)) v.forEach((x) => s.append(k, String(x)))
    else s.set(k, String(v))
  })
  const out = s.toString()
  return out ? `?${out}` : ''
}

export const knowledgeApi = {
  home: () => apiRequest<Home>('/knowledge/home'),
  list: (p: { type?: ContentType[]; product?: string; module?: number; category?: number; tag?: string; q?: string; sort?: string; page?: number; size?: number }) =>
    apiRequest<Page<Summary>>(`/knowledge/content${q(p)}`),
  get: (idOrSlug: string | number) => apiRequest<Item>(`/knowledge/content/${encodeURIComponent(String(idOrSlug))}`),
  products: () => apiRequest<ProductCard[]>('/knowledge/products'),
  hub: (slug: string) => apiRequest<ProductHub>(`/knowledge/products/${encodeURIComponent(slug)}`),
  workflows: () => apiRequest<Item[]>('/knowledge/workflows'),
  faqs: (product?: string) => apiRequest<Item[]>(`/knowledge/faqs${q({ product })}`),
  errorCode: (code: string) => apiRequest<Item>(`/knowledge/error-codes/${encodeURIComponent(code)}`),
  glossary: () => apiRequest<GlossaryTerm[]>('/knowledge/glossary'),
  releaseNotes: (product?: string) => apiRequest<Item[]>(`/knowledge/release-notes${q({ product })}`),
  search: (text: string, type?: ContentType) => apiRequest<SearchResult>(`/knowledge/search${q({ q: text, type })}`),
  playUrl: (contentId: number) => apiRequest<PlayInfo>(`/knowledge/videos/${contentId}/play-url`),
  downloadUrl: (mediaId: number) => apiRequest<TemporaryUrl>(`/knowledge/media/${mediaId}/download-url`),
  feedback: (contentId: number, body: { kind: 'VOTE' | 'OUTDATED' | 'SUGGESTION'; helpful?: boolean; reason?: string; comment?: string }) =>
    apiRequest<undefined>(`/knowledge/content/${contentId}/feedback`, { method: 'POST', body: JSON.stringify(body) }),
  event: (body: { type: 'VIDEO_PLAYED' | 'VIDEO_PROGRESS' | 'TICKET_CREATED_FROM_KNOWLEDGE'; contentId: number; percent?: number; seconds?: number }) =>
    apiRequest<undefined>('/knowledge/events', { method: 'POST', body: JSON.stringify(body) }),
  assistantStatus: () => apiRequest<{ configured: boolean; message: string }>('/knowledge/assistant/status'),
  personal: () => apiRequest<Personal>('/me/knowledge'),
  bookmark: (contentId: number) => apiRequest<undefined>(`/me/knowledge/bookmarks/${contentId}`, { method: 'PUT' }),
  unbookmark: (contentId: number) => apiRequest<undefined>(`/me/knowledge/bookmarks/${contentId}`, { method: 'DELETE' }),
  progress: (contentId: number, percent: number, positionSeconds?: number) =>
    apiRequest<undefined>(`/me/knowledge/progress/${contentId}`, { method: 'PUT', body: JSON.stringify({ percent, positionSeconds }) }),
}

// ---- Knowledge Management ------------------------------------------------------

export interface ContentInput {
  contentType: ContentType
  title: string
  shortDescription?: string | null
  slug?: string | null
  productId?: number | null
  moduleId?: number | null
  categoryId?: number | null
  feature?: string | null
  tags?: string[]
  keywords?: string | null
  audience?: Audience
  audienceOrganizationIds?: number[]
  requireProductAccess?: boolean
  productVersion?: string | null
  documentationVersion?: string | null
  effectiveAt?: string | null
  reviewAt?: string | null
  expiresAt?: string | null
  featured?: boolean
  difficulty?: string | null
  directActionRoute?: string | null
  directActionLabel?: string | null
  relatedContentIds?: number[]
  blocks?: Block[]
  typeFields?: TypeFields | null
}

export interface Content extends Required<Pick<ContentInput, 'contentType' | 'title'>> {
  id: number
  shortDescription: string | null
  slug: string | null
  productId: number | null
  moduleId: number | null
  categoryId: number | null
  feature: string | null
  tags: string[]
  keywords: string | null
  audience: Audience
  audienceOrganizationIds: number[]
  requireProductAccess: boolean
  productVersion: string | null
  documentationVersion: string | null
  effectiveAt: string | null
  reviewAt: string | null
  expiresAt: string | null
  scheduledAt: string | null
  featured: boolean
  difficulty: string | null
  directActionRoute: string | null
  directActionLabel: string | null
  relatedContentIds: number[]
  blocks: Block[]
  typeFields: TypeFields
  workflowState: WorkflowState
  live: boolean
  liveVersion: string | null
  expired: boolean
  requiresReview: boolean
  reviewComment: string | null
  authorSub: string | null
  reviewerSub: string | null
  approverSub: string | null
  createdAt: string | null
  updatedAt: string | null
  publishedAt: string | null
  video: VideoInfo | null
}

export interface ContentRow {
  id: number; contentType: ContentType; title: string; shortDescription: string | null; productId: number | null
  moduleId: number | null; categoryId: number | null; tags: string[]; audience: Audience; workflowState: WorkflowState
  live: boolean; liveVersion: string | null; expired: boolean; requiresReview: boolean; featured: boolean; views: number
  updatedAt: string | null; videoSource: VideoSource | null; durationSeconds: number | null; thumbnailUrl: string | null
}

export interface Version {
  id: number; versionLabel: string; title: string; shortDescription: string | null; blocks: Block[]
  typeFields: TypeFields | null; audience: Audience; live: boolean; publishedBySub: string | null; publishedAt: string
}
export interface Compare {
  from: string; to: string; titleChanged: boolean; fromTitle: string; toTitle: string
  changes: { index: number; kind: 'SAME' | 'CHANGED' | 'ADDED' | 'REMOVED'; before: Block | null; after: Block | null }[]
}
export interface TaxonomyModule { id: number; productId: number; name: string; slug: string; displayOrder: number; active: boolean; contentCount: number }
export interface TaxonomyProduct {
  id: number; name: string; slug: string; description: string | null; catalogProductId: number | null; displayOrder: number
  active: boolean; modules: TaxonomyModule[]; contentCount: number
}
export interface TaxonomyCategory { id: number; name: string; slug: string; scope: ContentType | null; displayOrder: number; active: boolean }
export interface Taxonomy { products: TaxonomyProduct[]; categories: TaxonomyCategory[] }
export interface Me { contributor: boolean; publisher: boolean; contentTypes: ContentType[] }

export interface Ranked { contentId: number; title: string; contentType: ContentType; count: number }
export interface Rated { contentId: number; title: string; contentType: ContentType; votes: number; helpfulPercent: number }
export interface Gap { query: string; searches: number; results: number }
export interface Dashboard {
  total: number; byState: Record<string, number>; byType: Record<string, number>; expired: number; requiresReview: number
  scheduled: number; mostViewed: Ranked[]; gaps: Gap[]; lowestRated: Rated[]
}
export interface Analytics {
  days: number; contentViews: number; videoPlays: number; downloads: number; knowledgeSearches: number; noResultSearches: number
  helpfulPercent: number | null; ticketsFromKnowledge: number; videoCompletions: number; averageWatchSeconds: number | null
  playsBySource: Record<string, number>; mostViewed: Ranked[]; mostWatched: Ranked[]; mostDownloaded: Ranked[]
  mostSearched: string[]; gaps: Gap[]; lowestRated: Rated[]
  recentFeedback: { contentId: number; title: string | null; kind: string; reason: string | null; comment: string | null; createdAt: string }[]
}
export interface Media {
  id: number; kind: MediaKind; fileName: string; mimeType: string; size: number; status: string; version: number
  productId: number | null; moduleId: number | null; createdAt: string; uploadedAt: string | null
  usedBy: { contentId: number; title: string; contentType: string }[]
  storage: { provider: string; bucket: string | null; region: string | null; objectKey: string; size: number; format: string; status: string } | null
}
export interface StorageStatus {
  configured: boolean; provider: string; videoMaxSize: number; documentMaxSize: number; imageMaxSize: number; audioMaxSize: number
  multipartThreshold: number; videoTypes: string[]; documentTypes: string[]; imageTypes: string[]; audioTypes: string[]
}
export interface UploadTicket {
  mediaId: number; objectKey: string; uploadUrl: string | null; expiresAt: string
  multipart: { uploadId: string; partUrls: string[]; partSize: number } | null; maxSize: number
}
export interface UploadRequest {
  fileName: string; contentType: string; size: number; kind?: MediaKind; productId?: number | null; moduleId?: number | null; folder?: string
}
export interface VideoRequest {
  content: ContentInput; sourceType: VideoSource; youtubeUrl?: string | null; url?: string | null; mediaId?: number | null
  durationSeconds?: number | null; channel?: string | null; thumbnailUrl?: string | null; thumbnailMediaId?: number | null
  transcript?: string | null; chapters?: string | null
}
export interface YouTubeDetails {
  videoId: string; title: string | null; description: string | null; durationSeconds: number | null; channel: string | null
  thumbnailUrl: string | null; source: 'DATA_API' | 'OEMBED'
}
export interface SearchIndexStatus {
  totalContent: number; indexable: number; indexed: number; indexedByType: Record<string, number>; videosWithTranscript: number
  withChapters: number; notIndexed: { id: number; title: string; contentType: string; reason: string }[]
  search: { engine: string; semanticInstalled: boolean; embeddedChunks: number; pendingChunks: number; chunks: number
    embedding: { configured: boolean; available: boolean } | null; lastRun: { status: string; finishedAt: string | null } | null }
}

const body = (v: unknown) => ({ body: JSON.stringify(v) })

export const knowledgeAdminApi = {
  me: () => apiRequest<Me>('/admin/knowledge/me'),
  list: (p: { type?: ContentType; status?: string; product?: number; q?: string; source?: string; page?: number; size?: number }) =>
    apiRequest<Page<ContentRow>>(`/admin/knowledge/content${q(p)}`),
  get: (id: number) => apiRequest<Content>(`/admin/knowledge/content/${id}`),
  create: (input: ContentInput) => apiRequest<Content>('/admin/knowledge/content', { method: 'POST', ...body(input) }),
  update: (id: number, input: ContentInput) => apiRequest<Content>(`/admin/knowledge/content/${id}`, { method: 'PUT', ...body(input) }),
  remove: (id: number) => apiRequest<undefined>(`/admin/knowledge/content/${id}`, { method: 'DELETE' }),
  action: (id: number, action: 'submit' | 'approve' | 'unschedule' | 'unpublish' | 'deprecate' | 'archive' | 'restore') =>
    apiRequest<Content>(`/admin/knowledge/content/${id}/${action}`, { method: 'POST' }),
  returnToDraft: (id: number, comment: string) =>
    apiRequest<Content>(`/admin/knowledge/content/${id}/return`, { method: 'POST', ...body({ comment }) }),
  publish: (id: number, versionBump: 'MINOR' | 'MAJOR', scheduleAt?: string | null) =>
    apiRequest<Content>(`/admin/knowledge/content/${id}/publish`, { method: 'POST', ...body({ versionBump, scheduleAt: scheduleAt || null }) }),
  preview: (id: number) => apiRequest<Item>(`/admin/knowledge/content/${id}/preview`),
  versions: (id: number) => apiRequest<Version[]>(`/admin/knowledge/content/${id}/versions`),
  compare: (id: number, from: string, to: string) => apiRequest<Compare>(`/admin/knowledge/content/${id}/versions/compare${q({ from, to })}`),
  restoreVersion: (id: number, label: string) =>
    apiRequest<Content>(`/admin/knowledge/content/${id}/versions/${encodeURIComponent(label)}/restore`, { method: 'POST' }),
  taxonomy: () => apiRequest<Taxonomy>('/admin/knowledge/taxonomy'),
  saveProduct: (id: number | null, v: Partial<TaxonomyProduct>) =>
    apiRequest<TaxonomyProduct>(`/admin/knowledge/taxonomy/products${id ? `/${id}` : ''}`, { method: id ? 'PUT' : 'POST', ...body(v) }),
  saveModule: (id: number | null, v: Partial<TaxonomyModule>) =>
    apiRequest<TaxonomyModule>(`/admin/knowledge/taxonomy/modules${id ? `/${id}` : ''}`, { method: id ? 'PUT' : 'POST', ...body(v) }),
  saveCategory: (id: number | null, v: Partial<TaxonomyCategory>) =>
    apiRequest<TaxonomyCategory>(`/admin/knowledge/taxonomy/categories${id ? `/${id}` : ''}`, { method: id ? 'PUT' : 'POST', ...body(v) }),
  deleteTaxonomy: (kind: 'products' | 'modules' | 'categories', id: number) =>
    apiRequest<undefined>(`/admin/knowledge/taxonomy/${kind}/${id}`, { method: 'DELETE' }),
  dashboard: () => apiRequest<Dashboard>('/admin/knowledge/dashboard'),
  analytics: (days: number) => apiRequest<Analytics>(`/admin/knowledge/analytics?days=${days}`),
  searchIndex: () => apiRequest<SearchIndexStatus>('/admin/knowledge/search-index'),
  reindexAll: () => apiRequest<{ items: number }>('/admin/knowledge/search-index/reindex', { method: 'POST' }),
  reindexOne: (id: number) => apiRequest<undefined>(`/admin/knowledge/search-index/reindex/${id}`, { method: 'POST' }),
}

export const knowledgeMediaApi = {
  storage: () => apiRequest<StorageStatus>('/admin/knowledge/media/storage'),
  list: (p: { kind?: MediaKind; q?: string; page?: number; size?: number }) => apiRequest<Page<Media>>(`/admin/knowledge/media${q(p)}`),
  get: (id: number) => apiRequest<Media>(`/admin/knowledge/media/${id}`),
  uploadUrl: (r: UploadRequest) => apiRequest<UploadTicket>('/admin/knowledge/media/upload-url', { method: 'POST', ...body(r) }),
  complete: (id: number, partEtags?: string[]) =>
    apiRequest<Media>(`/admin/knowledge/media/${id}/complete-upload`, { method: 'POST', ...body({ partEtags: partEtags ?? null }) }),
  abort: (id: number) => apiRequest<undefined>(`/admin/knowledge/media/${id}/abort`, { method: 'POST' }),
  previewUrl: (id: number) => apiRequest<TemporaryUrl>(`/admin/knowledge/media/${id}/preview-url`),
  replace: (id: number, r: UploadRequest) => apiRequest<UploadTicket>(`/admin/knowledge/media/${id}/replace`, { method: 'POST', ...body(r) }),
  remove: (id: number, confirm: boolean) => apiRequest<undefined>(`/admin/knowledge/media/${id}?confirm=${confirm}`, { method: 'DELETE' }),
}

export const knowledgeVideoApi = {
  list: (p: { status?: string; product?: number; source?: string; q?: string; page?: number; size?: number }) =>
    apiRequest<Page<ContentRow>>(`/admin/knowledge/videos${q(p)}`),
  summary: () => apiRequest<{ total: number; published: number; draft: number; inReview: number; views: number }>('/admin/knowledge/videos/summary'),
  uploadUrl: (r: UploadRequest) => apiRequest<UploadTicket>('/admin/knowledge/videos/upload-url', { method: 'POST', ...body(r) }),
  complete: (mediaId: number, partEtags?: string[]) =>
    apiRequest<Media>(`/admin/knowledge/videos/uploads/${mediaId}/complete-upload`, { method: 'POST', ...body({ partEtags: partEtags ?? null }) }),
  create: (r: VideoRequest) => apiRequest<Content>('/admin/knowledge/videos', { method: 'POST', ...body(r) }),
  update: (id: number, r: VideoRequest) => apiRequest<Content>(`/admin/knowledge/videos/${id}`, { method: 'PUT', ...body(r) }),
  remove: (id: number) => apiRequest<undefined>(`/admin/knowledge/videos/${id}`, { method: 'DELETE' }),
  replace: (id: number, r: UploadRequest) => apiRequest<UploadTicket>(`/admin/knowledge/videos/${id}/replace`, { method: 'POST', ...body(r) }),
  fetchYouTube: (url: string) => apiRequest<YouTubeDetails>('/admin/knowledge/videos/youtube/fetch-details', { method: 'POST', ...body({ url }) }),
  previewPlay: (id: number) => apiRequest<PlayInfo>(`/admin/knowledge/videos/${id}/preview-play-url`),
}

/** The content type sent for a file: the browser's, or one guessed from the extension. */
export function contentTypeOf(file: File): string {
  if (file.type) return file.type
  const ext = file.name.split('.').pop()?.toLowerCase() ?? ''
  const known: Record<string, string> = {
    pdf: 'application/pdf', csv: 'text/csv', txt: 'text/plain', vtt: 'text/vtt', mp4: 'video/mp4', webm: 'video/webm',
    mov: 'video/quicktime', mp3: 'audio/mpeg', m4a: 'audio/mp4', wav: 'audio/wav', png: 'image/png', jpg: 'image/jpeg',
    jpeg: 'image/jpeg', webp: 'image/webp', gif: 'image/gif',
    docx: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    xlsx: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    pptx: 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
    doc: 'application/msword', xls: 'application/vnd.ms-excel', ppt: 'application/vnd.ms-powerpoint',
  }
  return known[ext] ?? 'application/octet-stream'
}

// ---- direct upload to storage (browser → S3), with progress and cancel -----------

export interface UploadProgress {
  state: 'preparing' | 'uploading' | 'completed' | 'failed' | 'cancelled'
  loaded: number
  total: number
  /** bytes per second, when known */
  speed: number | null
  error?: string
}

export interface UploadHandle {
  promise: Promise<void>
  cancel: () => void
}

/**
 * PUTs a file (or each part of a multipart upload) to the presigned URL(s)
 * from the backend. Nothing but the file and its content type is sent to
 * storage; the browser never holds credentials. Reports progress and can be
 * cancelled at any time.
 */
export function uploadToStorage(ticket: UploadTicket, file: File, contentType: string, onProgress: (p: UploadProgress) => void,
                                onPartEtags?: (etags: string[]) => void): UploadHandle {
  let current: XMLHttpRequest | null = null
  let cancelled = false
  const started = Date.now()
  const total = file.size
  const report = (loaded: number) => {
    const seconds = (Date.now() - started) / 1000
    onProgress({ state: 'uploading', loaded, total, speed: seconds > 0.5 ? loaded / seconds : null })
  }
  const put = (url: string, data: Blob, offset: number, contentType?: string) =>
    new Promise<string | null>((resolve, reject) => {
      const xhr = new XMLHttpRequest()
      current = xhr
      xhr.open('PUT', url)
      if (contentType) xhr.setRequestHeader('Content-Type', contentType)
      xhr.upload.onprogress = (e) => report(offset + e.loaded)
      xhr.onload = () => (xhr.status >= 200 && xhr.status < 300 ? resolve(xhr.getResponseHeader('ETag')) : reject(new Error(`status ${xhr.status}`)))
      xhr.onerror = () => reject(new Error('network'))
      xhr.onabort = () => reject(new Error('cancelled'))
      xhr.send(data)
    })
  const promise = (async () => {
    onProgress({ state: 'uploading', loaded: 0, total, speed: null })
    if (ticket.multipart) {
      const etags: string[] = []
      const size = ticket.multipart.partSize
      for (let i = 0; i < ticket.multipart.partUrls.length; i++) {
        if (cancelled) throw new Error('cancelled')
        const etag = await put(ticket.multipart.partUrls[i], file.slice(i * size, (i + 1) * size), i * size)
        etags.push(etag ?? '')
      }
      onPartEtags?.(etags)
    } else if (ticket.uploadUrl) {
      await put(ticket.uploadUrl, file, 0, contentType)
    }
    onProgress({ state: 'completed', loaded: total, total, speed: null })
  })()
  return {
    promise,
    cancel: () => {
      cancelled = true
      current?.abort()
    },
  }
}
