import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'path'

export default defineConfig({
  // 用户端使用 Vue 3；自动导入和 Vant 组件解析减少页面里重复 import。
  plugins: [
    vue(),
    AutoImport({
      // 自动导入组合式 API、路由和 Pinia 常用函数，类型声明输出到 src/auto-imports.d.ts。
      resolvers: [VantResolver()],
      imports: ['vue', 'vue-router', 'pinia'],
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      // VantResolver 让 <van-*> 组件按需引入，避免手动注册和全量打包。
      resolvers: [VantResolver()],
      dts: 'src/components.d.ts',
    }),
  ],
  resolve: {
    alias: {
      // 保持 @ 指向 src，和 tsconfig paths 一致，方便跨目录引用业务模块。
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    // 允许局域网设备访问开发服务，手机调试使用 http://本机IP:5173。
    host: true,
    // 用户端固定 5173；管理端使用 5174，避免两个 Vite 项目冲突。
    port: 5173,
    proxy: {
      '/api': {
        // 开发环境将前端 /api 请求代理到 Spring Boot，规避浏览器跨域问题。
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
