import { Accordion, AccordionDetails, AccordionSummary, Alert, Box, Button, CircularProgress, MenuItem, Paper, TextField, Typography } from '@mui/material'
import { ChevronDown } from 'lucide-react'
import { useEffect, useState, type FormEvent } from 'react'
import { useTranslation } from 'react-i18next'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { knowledgeApi, type GlossaryTerm, type Item, type ProductCard, type Summary } from '../../api/knowledgeApi'
import { CardGrid, SummaryCard } from '../../components/knowledge/KnowledgeCards'
import { KnowledgeBlocksView, WorkflowDiagram } from '../../components/knowledge/KnowledgeBlocksView'
import { KnowledgeSupport } from '../../components/knowledge/KnowledgeSupport'
import { SECTIONS } from '../../components/knowledge/knowledgeUtils'
import { EmptyState } from '../../components/ui/EmptyState'
import { useAuth } from '../../auth/AuthProvider'

const PAGE = 24

function Heading({ section }: { section: string }) {
  const { t } = useTranslation()
  return (
    <Box sx={{ mb: 2 }}>
      <Typography component="h1" variant="h4">{t(`knowledge.nav.${section}`)}</Typography>
      <Typography sx={{ color: 'text.secondary' }}>{t(`knowledge.sections.${section}`)}</Typography>
    </Box>
  )
}

function ProductFilter({ value, onChange }: { value: string; onChange: (v: string) => void }) {
  const { t } = useTranslation()
  const [products, setProducts] = useState<ProductCard[]>([])
  useEffect(() => { knowledgeApi.products().then(setProducts).catch(() => setProducts([])) }, [])
  return (
    <TextField select size="small" label={t('knowledge.sections.productFilter')} value={value} onChange={(e) => onChange(e.target.value)} sx={{ minWidth: 200 }}>
      <MenuItem value="">{t('knowledge.sections.allProducts')}</MenuItem>
      {products.map((p) => <MenuItem key={p.id} value={p.slug}>{p.name}</MenuItem>)}
    </TextField>
  )
}

/** A Knowledge Center section listing one group of content types (prompt 5.5). */
export function KnowledgeSectionPage({ section }: { section: string }) {
  const { t } = useTranslation()
  const auth = useAuth()
  const types = SECTIONS.find((s) => s.key === section)?.types ?? []
  const [product, setProduct] = useState('')
  const [items, setItems] = useState<Summary[] | null>(null)
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0)
  const [error, setError] = useState(false)

  useEffect(() => {
    knowledgeApi.list({ type: types, product: product || undefined, page, size: PAGE })
      .then((p) => { setError(false); setItems((prev) => (page === 0 || !prev ? p.items : [...prev, ...p.items])); setTotal(p.totalElements) })
      .catch(() => setError(true))
    // types is derived from section
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [section, product, page])

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Heading section={section} />
      {section === 'troubleshooting' && <ErrorCodeLookup />}
      {section === 'developer' && auth.isAdmin && (
        <Paper variant="outlined" sx={{ p: 2, display: 'flex', gap: 1, alignItems: 'center', flexWrap: 'wrap' }}>
          <Typography sx={{ fontWeight: 600, mr: 1 }}>{t('knowledge.sections.developerLinks')}</Typography>
          <Button component={RouterLink} to="/admin/integrations/api-keys" size="small" variant="outlined">{t('knowledge.sections.apiKeys')}</Button>
          <Button component={RouterLink} to="/admin/integrations/events" size="small" variant="outlined">{t('knowledge.sections.events')}</Button>
        </Paper>
      )}
      <Box><ProductFilter value={product} onChange={(v) => { setProduct(v); setPage(0) }} /></Box>
      {error && <Alert severity="error">{t('knowledge.common.error')}</Alert>}
      {!items && !error && <Box sx={{ display: 'grid', placeItems: 'center', py: 6 }}><CircularProgress aria-label={t('knowledge.common.loading')} /></Box>}
      {items && items.length === 0 && <EmptyState title={t('knowledge.sections.empty')} />}
      {items && items.length > 0 && <CardGrid min={section === 'videos' ? 220 : 240}>{items.map((item) => <SummaryCard key={item.id} item={item} />)}</CardGrid>}
      {items && items.length < total && <Box><Button onClick={() => setPage(page + 1)}>{t('knowledge.sections.loadMore')}</Button></Box>}
      {section === 'troubleshooting' && <KnowledgeSupport />}
    </Box>
  )
}

function ErrorCodeLookup() {
  const { t } = useTranslation()
  const navigate = useNavigate()
  const [code, setCode] = useState('')
  const [notFound, setNotFound] = useState(false)
  const submit = (e: FormEvent) => {
    e.preventDefault()
    if (!code.trim()) return
    knowledgeApi.errorCode(code.trim()).then((item) => navigate(`/knowledge/content/${item.slug ?? item.id}`)).catch(() => setNotFound(true))
  }
  return (
    <Paper variant="outlined" component="form" onSubmit={submit} aria-labelledby="kc-error-lookup" sx={{ p: 2, display: 'flex', gap: 1, alignItems: 'center', flexWrap: 'wrap' }}>
      <Typography id="kc-error-lookup" sx={{ fontWeight: 600, mr: 1 }}>{t('knowledge.sections.errorLookup')}</Typography>
      <TextField size="small" label={t('knowledge.sections.errorCodeLabel')} value={code} onChange={(e) => { setCode(e.target.value); setNotFound(false) }} sx={{ minWidth: 280 }} />
      <Button type="submit" variant="contained">{t('knowledge.sections.lookup')}</Button>
      {notFound && <Typography variant="body2" sx={{ color: 'text.secondary' }}>{t('knowledge.sections.errorNotFound')}</Typography>}
    </Paper>
  )
}

/** FAQs as accordions, filterable by product (REQ-KNW-005.16). */
export function KnowledgeFaqsPage() {
  const { t } = useTranslation()
  const [product, setProduct] = useState('')
  const [loaded, setLoaded] = useState<{ product: string; items: Item[] } | null>(null)
  useEffect(() => { knowledgeApi.faqs(product || undefined).then((items) => setLoaded({ product, items })).catch(() => setLoaded({ product, items: [] })) }, [product])
  const faqs = loaded?.product === product ? loaded.items : null
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Heading section="faqs" />
      <Box><ProductFilter value={product} onChange={setProduct} /></Box>
      {!faqs && <CircularProgress aria-label={t('knowledge.common.loading')} />}
      {faqs && faqs.length === 0 && <EmptyState title={t('knowledge.sections.empty')} />}
      <Box>
        {faqs?.map((faq) => {
          const answer = typeof faq.typeFields?.answer === 'string' ? faq.typeFields.answer : null
          return (
            <Accordion key={faq.id} disableGutters variant="outlined" slotProps={{ heading: { component: 'h2' } }}>
              <AccordionSummary expandIcon={<ChevronDown size={18} />}>
                <Typography sx={{ fontWeight: 600 }}>{typeof faq.typeFields?.question === 'string' ? faq.typeFields.question : faq.title}</Typography>
              </AccordionSummary>
              <AccordionDetails>
                {answer && <Typography sx={{ whiteSpace: 'pre-wrap', mb: 1 }}>{answer}</Typography>}
                <KnowledgeBlocksView blocks={faq.blocks} />
                <Button size="small" component={RouterLink} to={`/knowledge/content/${faq.slug ?? faq.id}`} sx={{ mt: 1 }}>{t('knowledge.item.view')}</Button>
              </AccordionDetails>
            </Accordion>
          )
        })}
      </Box>
      <KnowledgeSupport />
    </Box>
  )
}

/** Glossary terms A–Z, each anchored for links from articles (REQ-KNW-005.21). */
export function KnowledgeGlossaryPage() {
  const { t } = useTranslation()
  const [terms, setTerms] = useState<GlossaryTerm[] | null>(null)
  useEffect(() => { knowledgeApi.glossary().then(setTerms).catch(() => setTerms([])) }, [])
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Heading section="glossary" />
      {!terms && <CircularProgress aria-label={t('knowledge.common.loading')} />}
      {terms && terms.length === 0 && <EmptyState title={t('knowledge.sections.empty')} />}
      <Box component="dl" sx={{ m: 0, display: 'grid', gap: 1.5 }}>
        {terms?.map((term) => (
          <Paper key={term.id} variant="outlined" id={term.slug} sx={{ p: 2, scrollMarginTop: 80 }}>
            <Typography component="dt" sx={{ fontWeight: 800 }}>{term.term}{term.synonyms.length > 0 && <Box component="span" sx={{ fontWeight: 400, color: 'text.secondary' }}> — {term.synonyms.join(', ')}</Box>}</Typography>
            <Typography component="dd" sx={{ m: 0, mt: 0.5 }}>{term.definition}</Typography>
          </Paper>
        ))}
      </Box>
    </Box>
  )
}

/** Interactive workflow guides (REQ-KNW-005.14). */
export function KnowledgeWorkflowsPage() {
  const { t } = useTranslation()
  const [flows, setFlows] = useState<Item[] | null>(null)
  useEffect(() => { knowledgeApi.workflows().then(setFlows).catch(() => setFlows([])) }, [])
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Heading section="workflows" />
      {!flows && <CircularProgress aria-label={t('knowledge.common.loading')} />}
      {flows && flows.length === 0 && <EmptyState title={t('knowledge.sections.empty')} />}
      {flows?.map((flow) => (
        <Paper key={flow.id} variant="outlined" sx={{ p: 2.5 }}>
          {flow.blocks.filter((b) => b.type === 'workflow_diagram').map((b, i) => <WorkflowDiagram key={i} title={b.title ?? flow.title} steps={b.steps ?? []} />)}
          {flow.blocks.every((b) => b.type !== 'workflow_diagram') && <Typography sx={{ fontWeight: 700 }}>{flow.title}</Typography>}
        </Paper>
      ))}
    </Box>
  )
}

/** Release notes, filterable by product (REQ-KNW-005.19). */
export function KnowledgeReleaseNotesPage() {
  const { t } = useTranslation()
  const [product, setProduct] = useState('')
  const [loaded, setLoaded] = useState<{ product: string; items: Item[] } | null>(null)
  useEffect(() => { knowledgeApi.releaseNotes(product || undefined).then((items) => setLoaded({ product, items })).catch(() => setLoaded({ product, items: [] })) }, [product])
  const notes = loaded?.product === product ? loaded.items : null
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
      <Heading section="releaseNotes" />
      <Box><ProductFilter value={product} onChange={setProduct} /></Box>
      {!notes && <CircularProgress aria-label={t('knowledge.common.loading')} />}
      {notes && notes.length === 0 && <EmptyState title={t('knowledge.sections.empty')} />}
      {notes?.map((note) => (
        <Paper key={note.id} variant="outlined" sx={{ p: 2 }}>
          <Typography component="h2" sx={{ fontWeight: 700 }}>
            <RouterLink to={`/knowledge/content/${note.slug ?? note.id}`}>{note.title}</RouterLink>
          </Typography>
          <Typography variant="body2" sx={{ color: 'text.secondary' }}>
            {[note.productName, typeof note.typeFields?.version === 'string' ? note.typeFields.version : null,
              typeof note.typeFields?.releaseDate === 'string' ? note.typeFields.releaseDate : null].filter(Boolean).join(' · ')}
          </Typography>
          {note.shortDescription && <Typography sx={{ mt: 1 }}>{note.shortDescription}</Typography>}
        </Paper>
      ))}
    </Box>
  )
}
