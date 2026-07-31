import react from '@vitejs/plugin-react'
import { resolve } from 'path'
import { defineConfig } from 'vite'

export default defineConfig({
  // 管理端是独立 React 应用，使用 Vite React 插件提供 JSX 和 Fast Refresh。
  plugins: [react()],
  resolve: {
    alias: {
      // 保持 @ 指向 src，和 tsconfig paths 一致，方便管理端模块引用。
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    // 管理端默认端口，用户端使用 5173；两端可同时启动。
    port: 5174,
    proxy: {
      '/api': {
        // 开发环境把 /api 转发给后端，管理端和用户端共享同一个 Spring Boot 服务。
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
