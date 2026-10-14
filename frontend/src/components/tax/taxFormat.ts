import type { TFunction } from 'i18next'
import type { TaxRule } from '../../api/taxApi'

/** A rule's components as text, for example "CGST 9% + SGST 9%". A non-taxable rule shows its treatment. */
export function componentsSummary(rule: TaxRule, t: TFunction): string {
  if (rule.treatment !== 'TAXABLE') return t(`admin.tax.treatment.${rule.treatment}`)
  return rule.components.map((c) => `${c.code} ${trimRate(c.rate)}%`).join(' + ')
}

/** 9, 9.5, 12.25 — no trailing zeros. */
export function trimRate(rate: number): string {
  return String(Number(rate))
}

export function formatDate(iso: string | undefined, language: string): string {
  if (!iso) return ''
  const d = new Date(iso)
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleDateString(language, { year: 'numeric', month: 'short', day: 'numeric' })
}

export function formatDateTime(iso: string | undefined, language: string): string {
  if (!iso) return ''
  const d = new Date(iso)
  return Number.isNaN(d.getTime()) ? iso : d.toLocaleString(language, { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })
}

/** A message for a failed call: the API's own message when there is one. */
export function errorText(e: unknown, fallback: string): string {
  return e instanceof Error && e.message ? e.message : fallback
}
