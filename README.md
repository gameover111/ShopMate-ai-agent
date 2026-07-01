<div align="center">

# 🧠 Shopmate 店小二 · AI Agent 平台

**电商智能客服 × 超级智能体 × 多 Agent 编排 · 三引擎驱动 · SSE 流式对话**

</div>

---

## 📋 项目简介

基于 **Spring AI + Vue 3** 的全栈 AI Agent 平台，提供三种 AI 能力：

| 模块 | 说明 |
|------|------|
| 🛒 **店小二** | 电商客服 AI，RAG 知识库问答，专注售前/售后/差评话术优化 |
| 🤖 **HManus** | 全能超级智能体，自主规划、调用工具逐步执行复杂任务 |
| 🔗 **Orchestrator** | 多 Agent 编排器，自动分析意图并路由到最合适的子 Agent |

---

## 🏗️ 技术栈

### 后端

| 技术 | 用途 |
|------|------|
| **Java 21** + **Spring Boot 3.5** | 应用框架 |
| **Spring AI 1.1** | AI 集成（ChatModel、Tool、Advisor、RAG） |
| **Spring AI Alibaba** | Agent 框架 |
| **PostgreSQL + pgvector** | 关系数据库 + 向量存储 |
| **DashScope / GLM-4-Flash** | 大语言模型 |
| **Tavily** | RAG 搜索引擎 |
| **百度翻译 API** | 查询翻译（中→英 RAG 检索） |
| **JWT** + **Spring Security** | 认证授权 |
| **iText** | PDF 生成 |
| **jsoup** | 网页抓取 |

### 前端

| 技术 | 用途 |
|------|------|
| **Vue 3** + **Vite 6** | 前端框架 |
| **Vue Router** | 路由管理 |
| **Axios** | HTTP 客户端 |
| **Fetch SSE** | 原生流式读取 |

### 部署

| 技术 | 用途 |
|------|------|
| **Nginx** | 反向代理 + 静态文件 |
| **Docker** | 前端容器化 |
| **Maven** | 后端构建 |

---

## ✨ 核心功能

### 1. Agent Harness 架构

```
Agent (接口)                    ← 标准化契约
  ↑
BaseAgent (抽象类)              ← 身份/状态/消息列表
  ↑
ReActAgent                     ← think() + act() → step()
  ↑
ToolCallAgent                  ← 工具调用：思考 → 执行
  ↑                  ↑
HManus           MultiAgentOrchestrator
(超级智能体)      (多 Agent 编排器)
```

**Harness 执行层**（`AgentHarness`）统一管理：
- 执行循环（最大步数、状态流转）
- ChatClient 注入（systemPrompt + tools）
- 工具管理和生命周期
- 多 Agent 编排路由

### 2. 多 Agent 编排

编排器通过 `DelegateTool` 将请求路由到子 Agent：
- 电商客服 → 路由到 **ShopMate**
- 通用任务 → 路由到 **HManus**
- 不修改子 Agent 返回结果，直接透传

### 3. RAG 知识库

```
文档加载 → Token 切分 → 关键词增强 → 向量化 → pgvector 存储
                                                      ↑
用户提问 → 百度翻译(中→英) → 向量检索 → 上下文增强 → LLM
```

### 4. 工具系统（9 个）

| 工具 | 说明 |
|------|------|
| WebSearch | Tavily RAG 搜索引擎 |
| WebScraping | jsoup 网页内容抓取 |
| FileOperation | 文件读写（`tmp/file/`） |
| ResourceDownload | URL 资源下载 |
| TerminalOperation | 终端命令执行 |
| PDFGeneration | iText PDF 生成 |
| MailSender | Spring Mail 邮件发送 |
| TimeOperation | 时间日期查询 |
| Terminate | 结束任务 |

### 5. Advisor 链

```
请求 → PermissionAdvisor → SensitiveWordAdvisor → MyLoggerAdvisor → LLM
       (权限校验)           (敏感词过滤)           (日志记录)
```

### 6. 用户系统

- 邮箱注册/登录
- JWT 无状态认证
- 个人信息管理（修改邮箱/密码）
- 管理员后台（用户管理/改角色/重置密码/删除）
- 会话管理（新建/切换/重命名/删除，按模块隔离）

### 7. 会话持久化

- 店小二：消息通过 `MessageChatMemoryAdvisor` 持久化到 `spring_ai_chat_memory` 表
- HManus / Orchestrator：通过 `ConversationStore` 持久化，`manus_`/`orch_` 前缀隔离

### 8. 前端特性

- 暗色主题，极客风格 UI
- SSE 流式输出（逐 token 显示）
- 响应式设计（桌面 + 移动端）
- 侧边栏会话管理
- 首页 SEO / Open Graph 优化

---

## 🚀 快速启动

### 环境要求

- JDK 21
- Node.js 18+
- PostgreSQL 16+（含 pgvector 扩展）
- 大模型 API Key（DashScope / OpenAI 兼容）

### 启动步骤

```bash
# 1. 配置数据库
# 修改 src/main/resources/application-local.yml 中的数据库连接

# 2. 启动后端
mvnw spring-boot:run

# 3. 启动前端
npm --prefix h-ai-agent-frontend run dev
```

浏览器访问 `http://localhost:5173`

### 默认管理员

首次启动自动创建：
- 账号：`admin`
- 密码：`init123456`

---

## 📁 项目结构

```
├── src/main/java/com/hsc/haiagent/
│   ├── agent/          # Agent 体系（BaseAgent → ToolCallAgent → HManus）
│   ├── harness/        # Harness 执行层
│   ├── app/            # ShopMateApp 电商客服
│   ├── advisor/        # Advisor 链（权限/敏感词/日志）
│   ├── tools/          # 工具系统（9 个工具 + DelegateTool）
│   ├── rag/            # RAG 知识库
│   ├── controller/     # REST 控制器
│   ├── service/        # 业务服务
│   ├── entity/         # JPA 实体
│   ├── repository/     # 数据仓库
│   ├── config/         # 配置（Security/CORS/Harness/GlobalException）
│   └── util/           # JWT 工具
├── h-ai-agent-frontend/
│   ├── src/
│   │   ├── views/      # 页面（Home/ShopMate/Manus/Orchestrator/Login/Register/Profile/Admin）
│   │   ├── components/ # 组件（ChatRoom/AiAvatar/SiteFooter）
│   │   ├── composables/# useChat 组合函数
│   │   ├── stores/     # 认证状态 store
│   │   ├── api/        # API 封装
│   │   └── utils/      # SSE/SEO/chatId 工具
│   └── vite.config.js
├── docs/               # 文档
└── pom.xml
```

---

## 📚 文档

| 文档 | 说明 |
|------|------|
| [操作手册](docs/OPERATION_MANUAL.md) | 用户指南 + 管理员指南 |
| [技术架构](docs/TECHNICAL_ARCHITECTURE.md) | 完整技术架构说明 |
| [测试方案](docs/TEST_PLAN.md) | 功能测试清单 |
| [开发计划](PLAN.md) | 后续开发路线图 |

---

## 🧪 测试

详见 [测试方案](docs/TEST_PLAN.md)

---

## 📄 许可证

MIT License
