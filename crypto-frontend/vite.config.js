import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    host: true,            // allows connections from any host
    port: 3000,            // make sure it matches your dev server
    strictPort: false,     // avoids port conflicts
    allowedHosts: 'all',   // allows ngrok hosts
    proxy: {
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true
      }
    }
  }
})
