import type { Content, Home, Item, Summary } from '../api/knowledgeApi'

export const summary = (over: Partial<Summary> = {}): Summary => ({
  id: 1, contentType: 'PRODUCT_GUIDE', slug: 'create-a-purchase-order', title: 'Create a purchase order', shortDescription: 'Raise a PO in Varthan.ai',
  productId: 2, productName: 'Varthan.ai', productSlug: 'varthan', moduleId: 21, moduleName: 'Purchase', categoryName: null, tags: ['purchase'],
  readingMinutes: 3, views: 42, versionLabel: '1.0', updatedAt: '2026-10-01T10:00:00Z', featured: false, deprecated: false, difficulty: null,
  videoSource: null, durationSeconds: null, thumbnailUrl: null, ...over,
})

export const home = (): Home => ({
  sectionCounts: { gettingStarted: 2, productGuides: 5, videos: 1, troubleshooting: 3, downloads: 4, faqs: 6 },
  recommended: [summary()],
  personalized: false,
  popularGuides: [summary({ id: 3, title: 'Inventory basics' })],
  featuredVideos: [summary({ id: 4, contentType: 'VIDEO', title: 'Getting started tour', videoSource: 'YOUTUBE', durationSeconds: 212, thumbnailUrl: 'https://i.ytimg.com/vi/x/hqdefault.jpg' })],
  popularSearches: ['purchase order'],
  products: [{ id: 2, name: 'Varthan.ai', slug: 'varthan', description: null, catalogProductId: 7, contentCount: 5, hasAccess: false,
    modules: [{ id: 21, name: 'Purchase', slug: 'purchase', contentCount: 3 }] }],
})

export const item = (over: Partial<Item> = {}): Item => ({
  id: 1, contentType: 'ARTICLE', slug: 'reset', title: 'Reset your password', shortDescription: null,
  blocks: [{ type: 'heading', text: 'Steps', level: 2 }, { type: 'paragraph', text: 'Open <script>alert(1)</script> the BOM screen.' },
    { type: 'code', text: 'curl /api', language: 'bash' }],
  typeFields: null, productId: null, productName: null, productSlug: null, moduleId: null, moduleName: null, categoryName: null, feature: null,
  tags: [], versionLabel: '1.1', productVersion: null, documentationVersion: null, publishedAt: '2026-10-01T10:00:00Z', updatedAt: null,
  readingMinutes: 1, views: 3, deprecated: false, difficulty: null, directActionRoute: '/account/security', directActionLabel: 'Open security',
  related: [], video: null, bookmarked: false, preview: false, ...over,
})

export const content = (over: Partial<Content> = {}): Content => ({
  id: 9, contentType: 'ARTICLE', title: 'Draft guide', shortDescription: null, slug: 'draft-guide', productId: null, moduleId: null, categoryId: null,
  feature: null, tags: [], keywords: null, audience: 'PUBLIC', audienceOrganizationIds: [], requireProductAccess: false, productVersion: null,
  documentationVersion: null, effectiveAt: null, reviewAt: null, expiresAt: null, scheduledAt: null, featured: false, difficulty: null,
  directActionRoute: null, directActionLabel: null, relatedContentIds: [], blocks: [{ type: 'paragraph', text: 'Hello' }], typeFields: {},
  workflowState: 'DRAFT', live: false, liveVersion: null, expired: false, requiresReview: false, reviewComment: null, authorSub: 's',
  reviewerSub: null, approverSub: null, createdAt: null, updatedAt: null, publishedAt: null, video: null, ...over,
})
