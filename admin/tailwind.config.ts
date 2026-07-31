import type { Config } from 'tailwindcss'

export default {
  // 通过 class 控制暗色模式，便于后续和管理端主题状态集成。
  darkMode: ['class'],
  // Tailwind 只扫描入口 HTML 和 React 源码，避免把 node_modules 纳入生成范围。
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      // 管理端基础语义色，和 Ant Design 组件搭配时保持克制、偏后台工具感。
      colors: {
        border: 'hsl(214 20% 88%)',
        input: 'hsl(214 20% 88%)',
        ring: 'hsl(222 84% 45%)',
        background: 'hsl(210 30% 98%)',
        foreground: 'hsl(222 47% 11%)',
        primary: {
          DEFAULT: 'hsl(222 84% 45%)',
          foreground: 'hsl(210 40% 98%)',
        },
        muted: {
          DEFAULT: 'hsl(215 20% 94%)',
          foreground: 'hsl(215 16% 47%)',
        },
      },
      // 管理端卡片、按钮、输入框统一使用小圆角，符合后台信息密度要求。
      borderRadius: {
        lg: '8px',
        md: '6px',
        sm: '4px',
      },
    },
  },
  // 暂无额外 Tailwind 插件，保持构建链路简单。
  plugins: [],
} satisfies Config
