import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import CssBaseline from '@mui/material/CssBaseline'
import './index.css'
import App from './App.tsx'
import { ThemeModeProvider } from './theming/ThemeModeProvider'
import { LocalePreferenceProvider } from './theming/LocalePreferenceProvider'
import { AuthProvider } from './auth/AuthProvider'
import { CartProvider } from './components/cart/CartContext'
import { WishlistProvider } from './components/wishlist/WishlistContext'
import './i18n'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeModeProvider>
      <CssBaseline />
      <LocalePreferenceProvider>
        <AuthProvider>
          <CartProvider>
            <WishlistProvider>
              <App />
            </WishlistProvider>
          </CartProvider>
        </AuthProvider>
      </LocalePreferenceProvider>
    </ThemeModeProvider>
  </StrictMode>,
)
