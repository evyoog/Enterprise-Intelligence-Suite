import type { TFunction } from 'i18next'
import type { ServiceStatusValue } from '../../api/serviceStatusApi'

/** "10 minutes ago", "yesterday", "3 days ago" in the UI language; older dates use `formatDate`. */
export function relativeTime(iso: string, language: string, formatDate: (iso: string) => string, now = Date.now()): string {
  const seconds = Math.round((new Date(iso).getTime() - now) / 1000)
  const abs = Math.abs(seconds)
  try {
    const rtf = new Intl.RelativeTimeFormat(language, { numeric: 'auto' })
    if (abs < 60) return rtf.format(seconds, 'second')
    if (abs < 3600) return rtf.format(Math.round(seconds / 60), 'minute')
    if (abs < 86400) return rtf.format(Math.round(seconds / 3600), 'hour')
    if (abs < 7 * 86400) return rtf.format(Math.round(seconds / 86400), 'day')
  } catch {
    // fall through to an absolute date
  }
  return formatDate(iso)
}

/** "SUBSCRIPTION_RENEWED" → the translated label when one exists, else "Subscription renewed". */
export function activityLabel(action: string, t: TFunction): string {
  const key = `bizDash.activity.action.${action}`
  const translated = t(key)
  if (translated !== key) return translated
  const words = action.toLowerCase().replace(/_/g, ' ')
  return words.charAt(0).toUpperCase() + words.slice(1)
}

/** "MANAGE_ORGANIZATION" → "Manage organization". */
export function permissionLabel(name: string): string {
  const words = name.toLowerCase().replace(/_/g, ' ')
  return words.charAt(0).toUpperCase() + words.slice(1)
}

export type Tone = 'success' | 'warning' | 'error' | 'info' | 'neutral'

export const SERVICE_TONE: Record<ServiceStatusValue, Tone> = {
  OPERATIONAL: 'success', DEGRADED: 'warning', PARTIAL_OUTAGE: 'warning', MAJOR_OUTAGE: 'error', MAINTENANCE: 'info',
}

const SEVERITY: ServiceStatusValue[] = ['OPERATIONAL', 'MAINTENANCE', 'DEGRADED', 'PARTIAL_OUTAGE', 'MAJOR_OUTAGE']

/** The worst of several statuses (MAJOR_OUTAGE beats everything). */
export function worstStatus(statuses: ServiceStatusValue[]): ServiceStatusValue {
  return statuses.reduce<ServiceStatusValue>((worst, s) => (SEVERITY.indexOf(s) > SEVERITY.indexOf(worst) ? s : worst), 'OPERATIONAL')
}

/** Where each backend dashboard alert type is resolved. */
export const ALERT_ROUTES: Record<string, string> = {
  SERVICE_STATUS: '/status',
  SEAT_LIMIT_REACHED: '/organization/members',
  UNDERUTILIZED_SEATS: '/organization/members',
  UNUSED_PRODUCT_ACCESS: '/organization/members',
  ORGANIZATION_MFA_REQUIRED: '/account/security',
  SUBSCRIPTION_EXPIRING_SOON: '/organization/billing',
  PRIVILEGED_ACCESS_PENDING: '/organization/privileged-access',
  PRIVILEGED_ACCESS_ACTIVE: '/organization/privileged-access',
}
