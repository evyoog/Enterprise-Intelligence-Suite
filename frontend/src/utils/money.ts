/** Amounts in a currency's smallest unit (paise, cents), formatted with the
 * platform locale (REQ-BIL-001.15). Falls back to "12.34 XYZ" for a
 * currency code the browser does not know. */
export function formatMoney(minorUnits: number, currency: string, locale?: string) {
  try {
    return new Intl.NumberFormat(locale, { style: 'currency', currency }).format(minorUnits / 100)
  } catch {
    return `${(minorUnits / 100).toFixed(2)} ${currency}`
  }
}
