import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import CssBaseline from '@mui/material/CssBaseline'
import './index.css'
import App from './App.tsx'
import { ThemeModeProvider } from './theming/ThemeModeProvider'
import { LocalePreferenceProvider } from './theming/LocalePreferenceProvider'
import { AuthProvider } from './auth/AuthProvider'
import './i18n'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeModeProvider>
      <CssBaseline />
      <LocalePreferenceProvider>
        <AuthProvider>
          <App />
        </AuthProvider>
      </LocalePreferenceProvider>
    </ThemeModeProvider>
  </StrictMode>,
)
