/** C100 RR-1 B: true while a subscription's read-only grace period (after the term ended with auto-renew off) is still running. */
export function isInGrace(graceEndsAt?: string | null, now: number = Date.now()): boolean {
  if (!graceEndsAt) return false
  const end = new Date(graceEndsAt).getTime()
  return Number.isFinite(end) && end > now
}
