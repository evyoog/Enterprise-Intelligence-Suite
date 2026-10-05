import { Navigate, useSearchParams } from 'react-router-dom'

/** REQ-KNW-001.9: /knowledge-base → Knowledge Center; ?article={id} opens that article. */
export function LegacyKnowledgeBaseRedirect() {
  const [params] = useSearchParams()
  const article = params.get('article')
  return <Navigate to={article && /^\d+$/.test(article) ? `/knowledge/content/${article}` : '/knowledge'} replace />
}
