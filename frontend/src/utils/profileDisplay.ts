/** "AR" from "Asha" + "Rao"; the first letter of the e-mail when there is no name. */
export function initialsOf(first: string | null, last: string | null, email: string): string {
  const letters = [first, last].map((n) => Array.from((n ?? '').trim())[0] ?? '').join('')
  return (letters || Array.from(email)[0] || '?').toLocaleUpperCase()
}

/** The preferred name when set, otherwise the full name, otherwise the e-mail. */
export function displayNameOf(
  p: { preferredName: string | null; firstName: string | null; lastName: string | null }, email: string,
): string {
  return p.preferredName || [p.firstName, p.lastName].filter(Boolean).join(' ') || email
}
