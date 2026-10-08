import { apiRequest } from './client'
import { uploadToStorage, type UploadHandle, type UploadProgress, type UploadTicket } from './knowledgeApi'

/** REQ-CAT-004 product content (02.04): what an administrator configures and what a visitor sees. */
export type ContentKind = 'DATASHEET' | 'DOCUMENTATION' | 'IMAGE' | 'VIDEO' | 'CASE_STUDY'
export type ContentStatus = 'DRAFT' | 'PUBLISHED'

export interface FileInfo { fileName: string; size: number; mimeType: string }

export interface AdminItem {
  id: number; kind: ContentKind; title: string; description: string | null; status: ContentStatus
  displayOrder: number; version: number; updatedAt: string; publishedAt: string | null
  file: FileInfo | null; fileUrl: string | null; altText: string | null; logo: FileInfo | null; logoUrl: string | null
  videoProvider: 'YOUTUBE' | 'VIMEO' | 'EXTERNAL' | null; videoUrl: string | null; embedUrl: string | null; thumbnailUrl: string | null
  customerName: string | null; problem: string | null; result: string | null
  articleId: number | null; articleTitle: string | null; articleAvailable: boolean
}

export interface StorageInfo {
  configured: boolean; imageMaxSize: number; documentMaxSize: number; imageTypes: string[]; documentTypes: string[]
}
export interface AdminContent { items: AdminItem[]; storage: StorageInfo }

export interface UploadedFile { uploadKey: string; fileName: string; contentType: string; size: number }

export interface ItemRequest {
  kind: ContentKind; title: string; description?: string | null; altText?: string | null; videoUrl?: string | null
  customerName?: string | null; problem?: string | null; result?: string | null; articleId?: number | null
  file?: UploadedFile | null; logo?: UploadedFile | null
}

export interface UploadUrl { uploadKey: string; uploadUrl: string; expiresAt: string; maxSize: number }
export interface TemporaryUrl { url: string; expiresAt: string; fileName: string | null; size: number | null }
export interface DocumentationOption { id: number; title: string; type: string; forThisProduct: boolean }

export interface PublicItem {
  id: number; kind: ContentKind; title: string; description: string | null; version: number; updatedAt: string
  file: FileInfo | null; imageUrl: string | null; altText: string | null; logoUrl: string | null
  videoProvider: 'YOUTUBE' | 'VIMEO' | 'EXTERNAL' | null; videoUrl: string | null; embedUrl: string | null; thumbnailUrl: string | null
  customerName: string | null; problem: string | null; result: string | null; articleRef: string | null; articleType: string | null
}
export interface PublicContent {
  datasheets: PublicItem[]; documentation: PublicItem[]; images: PublicItem[]; videos: PublicItem[]; caseStudies: PublicItem[]
}

const json = (body: unknown) => ({ body: JSON.stringify(body) })

export const productContentApi = {
  /** Public: published content of an Active application. */
  get: (productId: number) => apiRequest<PublicContent>(`/products/${productId}/content`),
  /** Public: a 5-minute download link for a published datasheet or case-study PDF. */
  download: (productId: number, itemId: number) => apiRequest<TemporaryUrl>(`/products/${productId}/content/${itemId}/download`),
}

export const productContentAdminApi = {
  list: (productId: number) => apiRequest<AdminContent>(`/admin/products/${productId}/content`),
  uploadUrl: (productId: number, r: { kind: ContentKind; purpose?: 'file' | 'logo'; fileName: string; contentType: string; size: number }) =>
    apiRequest<UploadUrl>(`/admin/products/${productId}/content/upload-url`, { method: 'POST', ...json(r) }),
  create: (productId: number, r: ItemRequest) =>
    apiRequest<AdminItem>(`/admin/products/${productId}/content`, { method: 'POST', ...json(r) }),
  update: (productId: number, itemId: number, r: ItemRequest) =>
    apiRequest<AdminItem>(`/admin/products/${productId}/content/${itemId}`, { method: 'PUT', ...json(r) }),
  remove: (productId: number, itemId: number) =>
    apiRequest<undefined>(`/admin/products/${productId}/content/${itemId}`, { method: 'DELETE' }),
  publish: (productId: number, itemId: number) =>
    apiRequest<AdminItem>(`/admin/products/${productId}/content/${itemId}/publish`, { method: 'POST' }),
  unpublish: (productId: number, itemId: number) =>
    apiRequest<AdminItem>(`/admin/products/${productId}/content/${itemId}/unpublish`, { method: 'POST' }),
  reorder: (productId: number, ids: number[]) =>
    apiRequest<AdminContent>(`/admin/products/${productId}/content/order`, { method: 'PUT', ...json({ ids }) }),
  previewUrl: (productId: number, itemId: number, logo = false) =>
    apiRequest<TemporaryUrl>(`/admin/products/${productId}/content/${itemId}/preview-url?logo=${logo}`),
  documentationOptions: (productId: number) =>
    apiRequest<DocumentationOption[]>(`/admin/products/${productId}/content/documentation-options`),
}

/** PUTs the file to the presigned URL (browser → S3, never through EIS), with progress and cancel. */
export function uploadProductFile(url: UploadUrl, file: File, contentType: string, onProgress: (p: UploadProgress) => void): UploadHandle {
  const ticket: UploadTicket = { mediaId: 0, objectKey: url.uploadKey, uploadUrl: url.uploadUrl, expiresAt: url.expiresAt, multipart: null, maxSize: url.maxSize }
  return uploadToStorage(ticket, file, contentType, onProgress)
}
