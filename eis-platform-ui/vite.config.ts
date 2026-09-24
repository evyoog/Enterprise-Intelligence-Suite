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
  },
})
