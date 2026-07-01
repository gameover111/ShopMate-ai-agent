# 📖 操作手册

> **Shopmate 店小二** — AI 电商客服 & 超级智能体平台  
> 版本：1.0.0 | 最后更新：2026-07

---

## 目录

- [用户指南](#用户指南)
  - [快速开始](#快速开始)
  - [AI 智能客服 · 店小二](#ai-智能客服--店小二)
  - [AI 超级智能体 · HManus](#ai-超级智能体--hmanus)
  - [多 Agent 编排器 · Orchestrator](#多-agent-编排器--orchestrator)
- [管理员指南](#管理员指南)
  - [系统管理](#系统管理)
  - [用户管理](#用户管理)
  - [运维建议](#运维建议)
- [常见问题](#常见问题)

---

## 用户指南

### 快速开始

1. **打开首页**：访问部署地址（如 `http://47.118.84.108`）
2. **选择应用**：首页展示三个 AI 应用入口，点击卡片进入
3. **开始对话**：在输入框输入内容，按 Enter 或点击"发送"
4. **SSE 流式回复**：AI 会实时流式输出回复内容

> 💡 **提示**：所有对话均走 SSE 流式输出，无需等待完整回复，AI 思考结果会逐字展示。

---

### AI 智能客服 · 店小二

**适用场景**：电商客服话术优化

**功能**：
- 售前咨询话术优化（比价应对、产品追问、犹豫转化）
- 售后纠纷话术优化（质量问题、物流延迟、少发错发）
- 差评投诉话术优化（安抚顾客、规则解释、改评价争取）

**使用方法**：
1. 首页点击"AI 智能客服 · 店小二"进入
2. 描述你的客服场景，例如：
   - _"顾客说我家比别家贵30块，怎么回？"_
   - _"买家收到货说开胶了，很生气要投诉，帮我写回复"_
   - _"有个差评说发货慢，怎么解释比较好？"_
3. 店小二会基于电商经验 + RAG 知识库给出优化话术
4. 支持多轮对话，可以持续追问优化

---

### AI 超级智能体 · HManus

**适用场景**：复杂自动化任务，需要自主规划和工具调用

**功能**：
- 网页搜索与内容抓取
- 文件操作（读/写）
- PDF 文档生成
- 网络资源下载
- 终端命令执行
- 邮件发送
- 时间日期查询

**使用方法**：
1. 首页点击"AI 超级智能体 · HManus"进入
2. 描述你的复杂任务，例如：
   - _"搜索2026年热门的运动鞋款式，整理成报告"_
   - _"帮我搜索最新的AI新闻，生成PDF发到我邮箱"_
   - _"下载一张风景图片保存到本地"_
3. HManus 会自主规划步骤，逐步调用工具执行
4. 每个步骤独立展示，清晰可追踪

**可用工具一览**：

| 工具 | 说明 | 用法示例 |
|------|------|----------|
| 网页搜索 | 从 Tavily 搜索引擎获取结果 | `searchWeb("2025 AI trends")` |
| 网页抓取 | 爬取指定 URL 内容 | `scrapeWebPage("https://...")` |
| 文件读写 | 读取/写入本地文件 | `readFile("report.txt")` |
| 资源下载 | 从 URL 下载文件 | `downloadResource(url, name)` |
| PDF 生成 | 将文本转为 PDF | `generatePDF("report.pdf", content)` |
| 终端命令 | 执行系统命令 | `executeTerminalCommand("dir")` |
| 邮件发送 | 发送文本邮件 | `sendTextMessage(to, sub, body)` |
| 时间查询 | 获取当前日期时间 | `getCurrentDateTime("yyyy-MM-dd")` |
| 终止 | 结束当前任务 | `doTerminate()` |

---

### 多 Agent 编排器 · Orchestrator

**适用场景**：不确定应该用哪个 Agent，统一入口自动分派

**功能**：
- 自动分析用户意图
- 选择最合适的 Agent（HManus / 店小二）执行
- 返回执行结果

**使用方法**：
1. 首页点击"多 Agent 编排器 · Orchestrator"进入
2. 输入你的需求，编排器会自动判断：
   - 电商客服类 → 委派给店小二
   - 复杂任务类 → 委派给 HManus
3. 例如：
   - _"帮我写一个差评回复"_ → 自动走店小二
   - _"搜索2026年最佳跑步鞋并生成报告"_ → 自动走 HManus

---

## 管理员指南

### 系统管理

**部署架构**：

```
用户 → Nginx (80)
         ├── / → 前端静态文件 (Vue 3)
         └── /api/ → 反向代理 → Spring Boot (8123)
                                   └── PostgreSQL (5432)
```

**启动命令**：

```bash
# 后端
cd harness_h-ai-agent
./mvnw spring-boot:run

# 前端（开发）
cd h-ai-agent-frontend
npm run dev

# 前端（构建）
npm run build
```

**环境变量**（`application-local.yml` 配置）：

| 配置项 | 说明 |
|--------|------|
| `spring.ai.openai.api-key` | 大模型 API Key |
| `spring.ai.openai.base-url` | 大模型 API 网关地址 |
| `spring.datasource.url` | PostgreSQL 连接地址 |
| `app.tavily.api-key` | 网页搜索 API Key |
| `app.baidu.appid` | 百度翻译 APP ID |
| `app.baidu.secret-key` | 百度翻译密钥 |
| `spring.mail.username` | QQ邮箱（发件） |
| `spring.mail.password` | QQ邮箱授权码 |

---

### 用户管理

待开发 — 见 [开发计划](../PLAN.md)

---

### 运维建议

1. **日志查看**：`tail -f logs/spring.log`
2. **健康检查**：`GET /api/health` → 返回 `ok`
3. **数据库备份**：`pg_dump shopmateagent > backup.sql`
4. **更新部署**：
   ```bash
   git pull
   cd harness_h-ai-agent && ./mvnw package -DskipTests
   systemctl restart h-ai-agent
   ```

---

## 常见问题

**Q: 对话没有响应？**  
A: 检查网络连接，SSE 接口需要长连接。确认后端服务正常：`GET /api/health`

**Q: 搜索没有结果？**  
A: 检查 `app.tavily.api-key` 配置是否正确

**Q: 邮件发送失败？**  
A: 检查 QQ 邮箱授权码和 SMTP 配置

**Q: 数据库连接失败？**  
A: 检查 `spring.datasource.url` 和 PostgreSQL 服务状态
