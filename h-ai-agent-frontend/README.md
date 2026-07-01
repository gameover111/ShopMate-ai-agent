# Shopmate 店小二 · AI 应用前端

基于 Vue 3 的 Shopmate 店小二 AI 应用前端，提供主页应用切换，以及两个基于 SSE 流式对话的功能模块。

## 功能模块

| 模块 | 路由 | 说明 |
|------|------|------|
| 应用主页 | `/` | 切换进入不同 AI 应用 |
| AI 智能客服 · 店小二 | `/shop-mate` | 电商客服场景多轮对话，需传递 `chatId` |
| AI 超级智能体 · HManus | `/manus` | 自主规划与工具调用，仅传递 `message` |

## 技术栈

- [Vue 3](https://vuejs.org/)（Composition API + `<script setup>`）
- [Vue Router 4](https://router.vuejs.org/)
- [Axios](https://axios-http.com/)（普通 HTTP 请求）
- [Vite 6](https://vite.dev/)（构建工具）
- SSE：原生 `fetch` + `ReadableStream` 实时读取流式响应

## 环境要求

- Node.js 18+
- npm 或 pnpm / yarn
- 后端服务 [h-ai-agent](https://github.com/) 运行于 `http://localhost:8123`（`context-path: /api`）

## 快速开始

### 1. 安装依赖

```bash
cd h-ai-agent-frontend
npm install
```

### 2. 启动后端

确保 Spring Boot 后端已启动，默认地址：

```
http://localhost:8123/api
```

### 3. 启动前端开发服务

```bash
npm run dev
```

浏览器访问：<http://localhost:5173>

### 4. 生产构建

```bash
npm run build
npm run preview
```

构建产物输出至 `dist/` 目录。

## 环境变量

| 文件 | 说明 |
|------|------|
| `.env.development` | 开发默认 `VITE_API_BASE_URL=/api`，走 Vite 代理 |
| `.env.production` | 生产构建前创建，参考 `.env.production.example` |

```env
# 推荐：与页面同域，由 Nginx 反代 /api → 后端 8123
VITE_API_BASE_URL=/api
```

未配置生产变量时，`src/api/config.js` 默认也是 `/api`（适合同域部署）。

## 服务器部署

### 方案 A：同域名（推荐）

页面 `http://服务器IP/` 与接口 `http://服务器IP/api/` 在同一域名下，由 Nginx 转发 API 到 Spring Boot。

1. **服务器安装 Node（仅构建机需要）**，在项目目录执行：

```bash
cp .env.production.example .env.production
# 保持 VITE_API_BASE_URL=/api 即可
npm install
npm run build
```

2. **上传 `dist/`** 到服务器，例如 `/var/www/h-ai-agent-frontend/dist`。

3. **后端**在同一台机器运行（端口 8123，`context-path: /api`）。

4. **Nginx** 参考 `deploy/nginx.conf.example`，重点：

- `try_files` 支持 Vue Router history 模式
- `location /api/` 反代到 `http://127.0.0.1:8123/api/`
- SSE 必须：`proxy_buffering off;`、`proxy_read_timeout 300s;`

5. 重载 Nginx：`nginx -t && nginx -s reload`

6. 浏览器访问 `http://你的域名或IP/` 即可。

### 方案 B：前后端不同域

后端例如 `https://api.example.com`，构建前设置：

```env
VITE_API_BASE_URL=https://api.example.com/api
```

需确保后端 `CorsConfig` 已放行前端域名；SSE 跨域需允许 GET 且不要缓存响应体。

### 子路径部署（可选）

前端挂在 `https://域名/shopmate/` 时：

```env
VITE_BASE_PATH=/shopmate/
```

`vite.config.js` 会读取该变量；路由已使用 `import.meta.env.BASE_URL`。

### 部署检查清单

- [ ] `npm run build` 无报错，`dist/index.html` 存在
- [ ] 访问首页能打开，刷新 `/shop-mate`、`/manus` 不 404
- [ ] 浏览器 Network 里 SSE 请求地址为 `当前域名/api/ai/...`
- [ ] 后端 8123 进程正常，安全组/防火墙放行（若直连端口）
- [ ] Nginx 对 `/api` 已关闭 `proxy_buffering`（否则 SSE 可能卡住）

开发环境下，`vite.config.js` 已配置 `/api` 代理到 `localhost:8123`。

## 后端接口

接口前缀：`{VITE_API_BASE_URL}`，默认 `http://localhost:8123/api`

### AI 智能客服（SSE）

```
GET /ai/shop_mate_app/chat/sse
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `message` | string | 是 | 用户输入消息 |
| `chatId` | string | 是 | 会话 ID，进入页面时自动生成 |

### AI 超级智能体（SSE）

```
GET /ai/manus/chat
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `message` | string | 是 | 用户输入消息 |

> 超级智能体接口不传 `chatId`，前端仍会生成会话 ID 用于页面展示与本地会话区分。

响应格式均为标准 `text/event-stream`，支持跨域（后端已配置 CORS）。

## 项目结构

```
src/
├── api/
│   ├── config.js       # Axios 实例、API 基础地址
│   └── chat.js         # SSE 路径与 URL 构建
├── assets/
│   └── main.css        # 全局样式
├── components/
│   └── ChatRoom.vue    # 聊天室组件（消息列表 + 输入框）
├── composables/
│   └── useChat.js      # 聊天逻辑（发送、流式更新、停止）
├── router/
│   └── index.js        # 路由配置
├── utils/
│   ├── chatId.js       # 会话 ID 生成
│   └── sse.js          # SSE 流解析
├── views/
│   ├── HomeView.vue           # 主页
│   ├── ShopMateChatView.vue   # 智能客服页
│   └── ManusChatView.vue      # 超级智能体页
├── App.vue
└── main.js
```

## 使用说明

1. 打开主页，选择要进入的 AI 应用。
2. 进入聊天页后，顶部会显示当前会话 ID（`chatId`）。
3. 在底部输入框输入内容，按 **Enter** 或点击 **发送**。
4. AI 回复以流式方式实时显示在左侧气泡中，用户消息显示在右侧。
5. 生成过程中可点击 **停止** 中断当前请求。

## 常见问题

**Q: 页面提示请求失败或无法连接？**

确认后端已启动且地址与 `VITE_API_BASE_URL` 一致；检查浏览器控制台是否存在跨域或网络错误。

**Q: SSE 无内容或乱码？**

确认后端接口返回 `Content-Type: text/event-stream`；智能客服与超级智能体使用不同的 SSE 实现（WebFlux / SseEmitter），前端 `src/utils/sse.js` 已兼容两种格式。

## 相关项目

- 后端：`h-ai-agent`（Spring Boot + Spring AI）

## License

Private
