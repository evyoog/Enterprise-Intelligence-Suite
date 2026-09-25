import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // Binds to all network interfaces, not just localhost — otherwise the
    // dev server refuses connections on the machine's LAN IP entirely (e.g.
    // http://192.168.1.4:5173), even once CORS/API-base-URL are correct.
    host: true,
    // Same-origin API in dev: the browser calls /api on this server and Vite
    // forwards it to the backend, so no CORS, no mixed content, and no need
    // to expose port 8081 (e.g. in Codespaces). Override the target with
    // VITE_API_PROXY_TARGET if the backend runs elsewhere.
    proxy: {
      '/api': {
        target: process.env.VITE_API_PROXY_TARGET ?? 'http://localhost:8081',
      },
    },
  },
})
