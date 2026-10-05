import { createContext, useContext } from 'react'
import type { Content, ContentInput, Me, Taxonomy } from '../../api/knowledgeApi'

export interface KnowledgeAdminContextValue {
  me: Me
  taxonomy: Taxonomy
  reloadTaxonomy: () => void
}

export const KnowledgeAdminContext = createContext<KnowledgeAdminContextValue | null>(null)

/** The signed-in staff member's knowledge permissions and the taxonomy. */
export function useKnowledgeAdmin() {
  const value = useContext(KnowledgeAdminContext)
  if (!value) throw new Error('useKnowledgeAdmin outside KnowledgeAdminLayout')
  return value
}

/** The editable fields of a content item. */
export function toInput(c: Content): ContentInput {
  return {
    contentType: c.contentType, title: c.title, shortDescription: c.shortDescription, slug: c.slug, productId: c.productId, moduleId: c.moduleId,
    categoryId: c.categoryId, feature: c.feature, tags: c.tags, keywords: c.keywords, audience: c.audience, audienceOrganizationIds: c.audienceOrganizationIds,
    requireProductAccess: c.requireProductAccess, productVersion: c.productVersion, documentationVersion: c.documentationVersion,
    effectiveAt: c.effectiveAt, reviewAt: c.reviewAt, expiresAt: c.expiresAt, featured: c.featured, difficulty: c.difficulty,
    directActionRoute: c.directActionRoute, directActionLabel: c.directActionLabel, relatedContentIds: c.relatedContentIds,
    blocks: c.blocks, typeFields: c.typeFields,
  }
}
