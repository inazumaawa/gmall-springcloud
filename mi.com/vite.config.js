import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vitejs.dev/config/
export default defineConfig({
  base: "./",
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)) 
    }
  },
  server: {
    proxy: {
      
      // '/auth': { target: 'http://ptu-mall.local:30008', changeOrigin: true },
      // '/goods': { target: 'http://ptu-mall.local:30008', changeOrigin: true },
      // '/cart': { target: 'http://ptu-mall.local:30008', changeOrigin: true },
      // '/order': { target: 'http://ptu-mall.local:30008', changeOrigin: true },
      // '/address': { target: 'http://ptu-mall.local:30008', changeOrigin: true },
      // '/coupon': { target: 'http://ptu-mall.local:30008', changeOrigin: true },
      // '/review': { target: 'http://ptu-mall.local:30008', changeOrigin: true },
      // '/favorite': { target: 'http://ptu-mall.local:30008', changeOrigin: true },
      // '/usercenter': { target: 'http://ptu-mall.local:30008', changeOrigin: true },
      // '/admin': { target: 'http://ptu-mall.local:30008', changeOrigin: true },
      // '/alipay': { target: 'http://ptu-mall.local:30008', changeOrigin: true },
      // '/ai': { target: 'http://ptu-mall.local:30008', changeOrigin: true },
      // '/obs': { target: 'http://ptu-mall.local:30008', changeOrigin: true }

      '/auth': { target: 'http://localhost:8081', changeOrigin: true },
      '/goods': { target: 'http://localhost:8082', changeOrigin: true },
      '/cart': { target: 'http://localhost:8083', changeOrigin: true },
      '/order': { target: 'http://localhost:8084', changeOrigin: true },
      '/address': { target: 'http://localhost:8086', changeOrigin: true },
      '/coupon': { target: 'http://localhost:8091', changeOrigin: true },
      '/review': { target: 'http://localhost:8088', changeOrigin: true },
      '/favorite': { target: 'http://localhost:8087', changeOrigin: true },
      '/usercenter': { target: 'http://localhost:8089', changeOrigin: true },
      '/admin': { target: 'http://localhost:8090', changeOrigin: true },
      '/alipay': { target: 'http://localhost:8085', changeOrigin: true },
      '/ai': { target: 'http://localhost:8092', changeOrigin: true },
      '/obs': { target: 'http://localhost:8093', changeOrigin: true },
      '/chat': { target: 'http://localhost:8094', changeOrigin: true },
      '/ws': { target: 'http://localhost:8094', changeOrigin: true, ws: true }


    }
  }
})
