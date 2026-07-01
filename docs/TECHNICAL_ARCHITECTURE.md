# 🏗️ 技术架构文档

> **Shopmate 店小二** — AI 电商客服 & 超级智能体平台  
> 版本：1.0.0 | 最后更新：2026-07

---

## 目录

- [整体架构](#整体架构)
- [技术栈](#技术栈)
- [后端架构详解](#后端架构详解)
  - [Agent 体系](#agent-体系)
  - [Harness 执行层](#harness-执行层)
  - [多 Agent 编排](#多-agent-编排)
  - [Advisor 链](#advisor-链)
  - [工具系统](#工具系统)
  - [RAG 知识库](#rag-知识库)
  - [聊天记忆](#聊天记忆)
- [前端架构详解](#前端架构详解)
  - [Vue 3 组件体系](#vue-3-组件体系)
  - [SSE 流式通信](#sse-流式通信)
  - [路由与 SEO](#路由与-seo)
- [通信与部署](#通信与部署)
- [技术选型说明](#技术选型说明)

---

## 整体架构

```
┌─────────────┐     ┌──────────────┐     ┌──────────────┐
│  浏览器      │────▶│  Nginx       │────▶│ Spring Boot  │
│  Vue 3 SPA   │     │  (80)        │     │  (8123/api)  │
│             │◀────│  /api 反代    │◀────│              │
└─────────────┘     └──────────────┘     └──────┬───────┘
       │                                         │
       │ SSE (text/event-stream)                 │
       │                                         ▼
       │                                  ┌──────────────┐
       │                                  │  PostgreSQL  │
       │                                  │  + pgvector  │
       │                                  └──────────────┘
       │                                         │
       │                                         ▼
       │                                  ┌──────────────┐
       │                                  │  阿里云       │
       │                                  │  DashScope    │
       │                                  │  (GLM-4-Flash)│
       │                                  └──────────────┘
```

---

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 21 | 开发语言 |
| Spring Boot | 3.5.14 | 应用框架 |
| Spring AI | 1.1.2 | AI 集成框架（ChatModel、Tool、Advisor、RAG） |
| Spring AI Alibaba | 1.1.2.0 | 阿里云 AI 扩展（Agent 框架） |
| Spring AI MCP | — | MCP 协议客户端支持 |
| PostgreSQL | 16+ | 主数据库 |
| pgvector | — | 向量存储（RAG 知识库） |
| Spring Mail | — | 邮件发送 |
| Lombok | 1.18.36 | 代码简化 |
| Hutool | 5.8.37 | Java 工具库 |
| Kryo | 5.6.2 | 对象序列化（文件级聊天记忆） |
| iText | 9.1.0 | PDF 生成 |
| jsoup | 1.19.1 | 网页内容抓取 |
| Knife4j | 4.4.0 | API 文档（Swagger UI） |
| DashScope SDK | 2.22.17 | 阿里云灵积/百练大模型服务 |
| LangChain4j | 1.15.0 | LangChain4j 集成 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.5.13 | 前端框架 |
| Vite | 6.0.7 | 构建工具 |
| Vue Router | 4.5.0 | 路由管理 |
| Axios | 1.7.9 | HTTP 客户端 |
| Fetch API (SSE) | — | 原生流式读取 |

### 部署

| 技术 | 用途 |
|------|------|
| Nginx | 反向代理 + 静态文件服务 |
| Docker | 前端容器化 |
| Maven | 后端构建 |
| certbot | HTTPS 证书（可选） |

---

## 后端架构详解

### Agent 体系

```
Agent (接口)                    ← Agent 契约
  ↑
BaseAgent (抽象类)              ← 身份/状态/消息列表/工具持有
  ↑
ReActAgent (抽象类)             ← think() + act() → step()
  ↑
ToolCallAgent                  ← 工具调用：think 判断 → act 执行
  ↑                  ↑
HManus           MultiAgentOrchestrator
(通用超级智能体)    (多 Agent 编排器)
```

**Agent 接口 (`harness/Agent.java`)**：
- `getName()` / `setName()` — 身份
- `getSystemPrompt()` / `setSystemPrompt()` — 系统提示词
- `getState()` / `setState()` — 运行状态（IDLE / RUNNING / FINISHED / ERROR）
- `getMessageList()` / `setMessageList()` — 会话上下文
- `getChatClient()` / `setChatClient()` — LLM 客户端
- `setToolCallbacks()` / `getToolCallbacks()` — 工具注入
- `step()` — 单个步骤执行
- `cleanup()` — 资源清理

**状态流转**：

```
IDLE → RUNNING → [step循环] → FINISHED
                     ↓
                   ERROR
```

---

### Harness 执行层

**`AgentHarness` 接口** — 标准化执行环境：

```java
public interface AgentHarness {
    String execute(Agent agent, String userPrompt);
    SseEmitter executeStream(Agent agent, String userPrompt);
    void registerAgent(Agent agent);
    Agent getAgent(String name);
    List<Agent> getAllAgents();
    SseEmitter orchestrateStream(String userPrompt);
}
```

**职责分离**：

| 层面 | 职责 | 所属 |
|------|------|------|
| **Agent** | think / act 逻辑，"做什么" | `agent/` 包 |
| **Harness** | 执行循环、工具注入、生命周期，"怎么运行" | `harness/` 包 |
| **Controller** | HTTP 入口，参数解析 | `controller/` 包 |

**`DefaultAgentHarness.executeStream()` 执行流程**：

```
1. 校验 Agent 状态（必须 IDLE）
2. 初始化 Agent
   ├── 创建 ChatClient（systemPrompt + MyLoggerAdvisor）
   ├── 注入工具（如果 Agent 未自设工具）
   └── 添加用户消息到 messageList
3. 进入执行循环
   ├── 调用 agent.step()
   ├── 通过 SseEmitter 推送结果
   └── 检查 maxSteps / FINISHED 状态
4. 完成或异常 → emitter.complete() / emitter.completeWithError()
5. finally: agent.cleanup()
```

---

### 多 Agent 编排

**架构**：

```
用户请求
    │
    ▼
MultiAgentOrchestrator (ToolCallAgent)
    │  systemPrompt: 描述所有已注册子 Agent
    │  工具: 仅有 DelegateTool
    │
    ├── delegateToAgent("HManus", task)   → 通用任务
    └── delegateToAgent("ShopMate", task) → 电商客服任务
```

**DelegateTool**：
- 接收 `agentName` + `task`
- 通过 `AgentHarness.getAgent()` 获取子 Agent
- 调用 `AgentHarness.execute()` 同步执行
- 返回子 Agent 的执行结果

**Orchestrator 限制**：
- `maxSteps=5` — 防止无限循环
- 只有 `DelegateTool` 一个工具 — 防止绕过委派直接执行

---

### Advisor 链

Spring AI 的 Advisor 机制在 ChatClient 调用前后介入，实现横切关注点。

```
请求 → PermissionAdvisor → SensitiveWordAdvisor → MyLoggerAdvisor → LLM
       (权限校验)          (敏感词过滤)           (日志记录)

响应 ← PermissionAdvisor ← SensitiveWordAdvisor ← MyLoggerAdvisor ← LLM
```

| Advisor | 顺序 | 职责 | 启用 |
|---------|------|------|------|
| `PermissionAdvisor` | 0 | 校验 `user_permissions` 参数中是否包含 `AI_CHAT` | ✅ 默认 |
| `SensitiveWordAdvisor` | 1 | 检测违禁词（暴力/色情/赌博等） | ✅ 默认 |
| `MyLoggerAdvisor` | -1 | 打印请求/响应的日志 | ✅ 默认 |
| `ReReadingAdvisor` | — | 重复阅读用户问题（Re2 策略）提升推理 | ❌ 可选 |
| `MessageChatMemoryAdvisor` | — | 注入历史消息实现多轮对话 | ✅ 默认 |

---

### 工具系统

所有工具通过 `ToolRegistration` 集中注册为 `ToolCallback[]` Bean：

```java
@Configuration
public class ToolRegistration {
    @Bean
    public ToolCallback[] allTools() { ... }
}
```

| 工具 | 类 | 说明 | 依赖 |
|------|-----|------|------|
| 网页搜索 | `WebSearchTool` | Tavily RAG 搜索引擎 | `app.tavily.api-key` |
| 网页抓取 | `WebScrapingTool` | jsoup 解析 HTML | — |
| 文件读写 | `FileOperationTool` | 读写 `tmp/file/` 目录 | — |
| 资源下载 | `ResourceDownloadTool` | URL → `tmp/download/` | — |
| 终端命令 | `TerminalOperationTool` | `cmd.exe /c command` | Windows 环境 |
| PDF 生成 | `PDFGenerationTool` | iText → `tmp/pdf/` | 内置字体 |
| 邮件发送 | `MailSenderTool` | Spring Mail → QQ邮箱 | `spring.mail.*` |
| 时间查询 | `TimeOperationTool` | 本地时间格式化 | — |
| 终止 | `TerminateTool` | 设置 AgentState.FINISHED | — |
| 委托 | `DelegateTool` | 编排器委派任务给子 Agent | — |

---

### RAG 知识库

**整体流程**：

```
加载 → 切分 → 增强 → 向量化 → 存储 → 检索 → 增强 → 生成
                                           ↑
                                       用户查询
                                           ↓
                                      查询翻译
                                     (百度API)
```

| 阶段 | 组件 | 说明 |
|------|------|------|
| **加载** | `ShopMateAppDocumentLoader` | 读取 `classpath:document/*.md` 电商知识文档 |
| **切分** | `MyTokenTextSplitter` | 200 Token 切片 |
| **增强** | `MyKeywordEnricher` | LLM 自动提取 5 个关键词作为元数据 |
| **向量化** | `dashscopeEmbeddingModel` | `text-embedding-v2` 模型，1536 维 |
| **存储** | `PgVectorStore` | PostgreSQL + pgvector，HNSW 索引 |
| **检索** | `VectorStoreDocumentRetriever` | 相似度阈值 0.5，TopK 3 |
| **查询重写** | `QueryRewriter` | (当前被替换) LLM 重写用户查询 |
| **查询翻译** | `QueryTransformer` + `TranslationGateway` | **百度翻译 API** 中→英（免费，每月100万字） |
| **上下文增强** | `ContextualQueryAugmenter` | 空结果时返回"只能回答商列相关问题" |

**知识库文档**（`src/main/resources/document/`）：

| 文件 | 内容 |
|------|------|
| `店小二电商知识问答 - 售前篇.md` | 商品推荐、比价、优惠券等 售前场景 |
| `店小二电商知识问答 - 售后篇.md` | 退换货、退款、物流等 售后场景 |
| `店小二电商知识问答 - 其他篇.md` | 其他常见问题 |
| `商品列表.md` | 商品数据 |

---

### 聊天记忆

**当前实现**：`InMemoryChatMemoryRepository` + `MessageWindowChatMemory`（滑动窗口）

```
ChatMemoryRepository (InMemory)        → 消息存储层
    ↓
MessageWindowChatMemory                → 窗口管理（无上限）
    ↓
MessageChatMemoryAdvisor              → Spring AI 集成到 ChatClient
```

**可选的文件持久化**：`FileBasedChatMemory`（Kryo 序列化到 `tmp/chat-memory/*.kryo`），当前未启用。

---

## 前端架构详解

### Vue 3 组件体系

```
App.vue
  └─ <router-view>
       ├─ HomeView.vue            首页（应用卡片列表）
       ├─ ShopMateChatView.vue    电商客服
       ├─ ManusChatView.vue       HManus 智能体
       ├─ OrchestratorChatView.vue 编排器
       └─ (后续) LoginView / RegisterView / ProfileView / AdminUsersView
```

**核心组件**：

| 组件 | 说明 |
|------|------|
| `ChatRoom.vue` | 通用聊天室：消息列表 + 输入框 + SSE 流式展示 |
| `AiAvatar.vue` | AI 头像：店小二/Manus/用户三种风格 |
| `SiteFooter.vue` | 页脚：极客风格 |

**ChatRoom 组件 Props**：

| Prop | 类型 | 说明 |
|------|------|------|
| `streamMode` | `'accumulate' \| 'step'` | 积累模式（店小二）/ 步骤模式（HManus） |
| `theme` | `'shop-mate' \| 'manus'` | 主题色 |
| `chatId` | String | 会话 ID（多轮对话） |

---

### SSE 流式通信

**前端 `fetchSSE()`** (`utils/sse.js`)：

```javascript
// 使用原生 fetch + ReadableStream 读取 SSE
const response = await fetch(url, { headers: { Accept: 'text/event-stream' } });
const reader = response.body.getReader();
// 逐块解码 → 按行解析 → 过滤 data: 前缀 → 回调 onMessage
```

**兼容两种后端输出格式**：
- Spring WebFlux `Flux<String>` — 纯文本块
- `SseEmitter` — 标准 SSE 格式（`data:` 前缀）

**`useChat()` 组合函数** (`composables/useChat.js`)：

```javascript
const { messages, inputText, loading, sendMessage, stopGeneration } = useChat({
  ssePath: '/ai/manus/chat',
  getParams: (msg) => ({ message: msg }),
  streamMode: 'step',
});
```

---

### 路由与 SEO

```javascript
const routes = [
  { path: '/',            name: 'home',          component: HomeView },
  { path: '/shop-mate',   name: 'shop-mate',     component: ShopMateChatView },
  { path: '/manus',       name: 'manus',         component: ManusChatView },
  { path: '/orchestrator', name: 'orchestrator',  component: OrchestratorChatView },
];
```

每个路由的 `meta` 包含 `title` / `description` / `keywords`，通过 `router.afterEach` 动态更新 `<title>` 和 `<meta>` 标签，支持 SEO + Open Graph。

---

## 通信与部署

### 请求流程

```
浏览器                    Nginx                    Spring Boot
  │                        │                         │
  ├─ GET / ───────────────▶│ 返回 dist/index.html     │
  │◀──────────────────────│                         │
  │                        │                         │
  ├─ GET /api/ai/manus/chat?message=xxx ────────────▶│
  │                        │ (proxy_pass)            │
  │                        │                         ├─ AgentHarness.executeStream()
  │◀── SSE (text/event-stream) ◀────────────────────│
  │    chunk by chunk                                │
```

### Nginx 配置要点

| 配置 | 说明 |
|------|------|
| `proxy_buffering off;` | 关闭缓冲，SSE 必须 |
| `proxy_cache off;` | 关闭缓存 |
| `proxy_read_timeout 300s;` | AI 思考时间长，防止断连 |
| `chunked_transfer_encoding on;` | 分块传输 |

---

## 技术选型说明

### 为什么选 Spring AI 而不是直接调 API？

- **统一抽象**：`ChatModel` 接口屏蔽了各厂商差异，切换模型只需改配置
- **内置 Advisor**：权限/敏感词/日志等横切关注点无需重复实现
- **Tool 机制**：`@Tool` 注解声明式注册，自动生成 JSON Schema
- **RAG 集成**：`VectorStoreDocumentRetriever` + `RetrievalAugmentationAdvisor` 开箱即用

### 为什么用 Tavily 而不是 Google/Bing 搜索？

- **专为 RAG 优化**：返回结构化摘要而非原始网页
- **免费额度充足**：开发阶段 1000 次/月免费
- **API 简洁**：单 POST 请求即可

### 为什么用百度翻译 API 做查询翻译？

- **成本趋零**：每月 100 万字免费额度
- **效果足够**：RAG 检索只需中→英关键词翻译，不需 LLM 重写
- **延迟极低**：HTTP 请求 < 500ms，相比 LLM 重写快 10 倍

### 为什么用 SSE 而不是 WebSocket？

- **单向推送即可**：AI 回复是服务器→客户端单向流
- **HTTP 兼容**：无需升级协议，Nginx 天然支持
- **简单可靠**：`SseEmitter` + `fetch` ReadableStream 实现，无需额外依赖

### 为什么设计 Harness 层？

- **关注点分离**：Agent 专注 think/act ，Harness 负责执行管理
- **统一生命周期**：所有 Agent 走相同初始化/执行/清理流程
- **可扩展**：新增 Agent 只需实现 Agent 接口 + 注册到 Harness
- **可观测性**：Harness 层集中埋点，便于监控和日志
