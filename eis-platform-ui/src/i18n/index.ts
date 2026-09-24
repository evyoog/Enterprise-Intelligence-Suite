import i18n from 'i18next'
import LanguageDetector from 'i18next-browser-languagedetector'
import { initReactI18next } from 'react-i18next'
import en from './locales/en.json'
import es from './locales/es.json'

export const SUPPORTED_LANGUAGES = [
  { code: 'en', label: 'EN' },
  { code: 'es', label: 'ES' },
] as const

/**
 * Phase 21: a real i18n architecture (react-i18next), replacing the
 * previous state of "every string is hardcoded English inline in JSX." Only
 * the navbar and login flow are fully migrated so far (see this phase's own
 * report on scope) — that's still the highest-traffic, most universal path
 * (every page shows the navbar; every unauthenticated user goes through the
 * login modal), and proves the mechanism genuinely works end to end with a
 * second real language, not just an English-only scaffold.
 *
 * English stays the default and behaves exactly as before for anyone who
 * never touches the language switcher — LanguageDetector only picks a
 * different starting language from a previously *saved* choice
 * (localStorage key "vyoog-language", set by the switcher itself), never
 * from the browser's Accept-Language header, so a fresh visitor's default
 * experience doesn't silently change based on browser locale.
 */
i18n
  .use(LanguageDetector)
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      es: { translation: es },
    },
    fallbackLng: 'en',
    interpolation: { escapeValue: false },
    detection: {
      order: ['localStorage'],
      lookupLocalStorage: 'vyoog-language',
      caches: ['localStorage'],
    },
  })

export default i18n
