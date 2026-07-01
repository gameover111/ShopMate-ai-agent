import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  // 子路径部署时设置，例如 VITE_BASE_PATH=/shopmate/
  const base = env.VITE_BASE_PATH || '/'

  return {
    base,
    plugins: [vue()],
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    build: {
      outDir: 'dist',
      sourcemap: false,
    },
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: 'http://localhost:8123',
          changeOrigin: true,
        },
      },
    },
  }
})
