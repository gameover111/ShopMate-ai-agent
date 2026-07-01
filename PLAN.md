# 🗺️ 后续开发计划

## 目标

为 Shopmate 店小二 / HManus 项目增加 **用户系统 + 会话持久化 + 权限管理**，形成完整的 SaaS 应用闭环。

---

## 一、用户系统（注册 / 登录 / 账户管理）

### 1.1 后端 — 用户实体与认证

| 步骤 | 内容 | 文件 |
|------|------|------|
| 1.1.1 | 创建 User 实体：id, username, password(BCrypt), email, avatar, role(USER/ADMIN), createdAt, updatedAt | `entity/User.java` |
| 1.1.2 | 创建 UserRepository（JPA） | `repository/UserRepository.java` |
| 1.1.3 | 创建 UserService：register / login / updateProfile / changePassword | `service/UserService.java` |
| 1.1.4 | 集成 JWT：生成 token、校验 token、从 token 提取用户信息 | `util/JwtUtil.java` |
| 1.1.5 | 创建 JWT 认证过滤器（OncePerRequestFilter）：拦截除 `/auth/**` 外的请求 | `config/JwtAuthFilter.java` |
| 1.1.6 | 创建 AuthController：POST `/auth/register`, POST `/auth/login`, POST `/auth/logout` | `controller/AuthController.java` |
| 1.1.7 | 创建 UserController：GET `/user/profile`, PUT `/user/profile`, PUT `/user/password` | `controller/UserController.java` |

### 1.2 后端 — 管理员

| 步骤 | 内容 | 文件 |
|------|------|------|
| 1.2.1 | 创建 AdminController：GET `/admin/users`(分页), PUT `/admin/users/{id}/role`, DELETE `/admin/users/{id}` | `controller/AdminController.java` |
| 1.2.2 | 添加 Admin 权限校验注解/切面 | `config/AdminAspect.java` |

### 1.3 前端 — 认证页面

| 步骤 | 内容 | 文件 |
|------|------|------|
| 1.3.1 | 创建认证状态 store（reactive，localStorage 持久化 token） | `stores/auth.js` |
| 1.3.2 | 创建 Login 页面（表单 + 调用 `/auth/login` + 存储 token） | `views/LoginView.vue` |
| 1.3.3 | 创建 Register 页面（表单 + 调用 `/auth/register` + 跳转登录） | `views/RegisterView.vue` |
| 1.3.4 | 更新路由：添加 `/login`, `/register`，添加导航守卫（未登录→登录页） | `router/index.js` |
| 1.3.5 | 更新 Axios 拦截器：自动携带 Authorization header，401 时跳转登录 | `api/config.js` |
| 1.3.6 | 首页添加登录/用户状态显示 | `views/HomeView.vue` |

### 1.4 前端 — 账户管理

| 步骤 | 内容 | 文件 |
|------|------|------|
| 1.4.1 | 创建 Profile 页面（查看/编辑用户名、邮箱、头像，修改密码） | `views/ProfileView.vue` |
| 1.4.2 | 路由添加 `/profile`（需登录） | `router/index.js` |

### 1.5 前端 — 管理员页面

| 步骤 | 内容 | 文件 |
|------|------|------|
| 1.5.1 | 创建 Admin 用户管理页面（表格展示用户、搜索、改角色、删除） | `views/AdminUsersView.vue` |
| 1.5.2 | 路由添加 `/admin/users`（需 ADMIN 角色） | `router/index.js` |
| 1.5.3 | 管理员专属导航入口 | `components/AdminNav.vue` |

---

## 二、会话持久化

### 2.1 问题
当前聊天记忆使用 `InMemoryChatMemoryRepository`，应用重启后丢失。

### 2.2 方案：基于 JDBC 的持久化

| 步骤 | 内容 | 文件 |
|------|------|------|
| 2.1.1 | 启用 Spring AI 的 JDBC ChatMemory（创建 `ai_chat_memory` 表） | `pom.xml` + `application.yml` |
| 2.1.2 | 配置数据源（已有 PostgreSQL）自动建表 | `application.yml` |
| 2.1.3 | 替换 ShopMateApp 中 `InMemoryChatMemoryRepository` 为 JDBC 实现 | `app/ShopMateApp.java` |
| 2.1.4 | 用户登录后，chatId 与 userId 绑定 → 会话漫游 | `service/ChatSessionService.java` |

### 2.3 可选增强

- 会话列表（查看历史会话，选择恢复）
- 会话标题自动生成（基于首条消息）

---

## 三、数据表结构总览

```sql
-- 用户表
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,  -- BCrypt
    email       VARCHAR(100),
    avatar      VARCHAR(255),
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',  -- USER / ADMIN
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- 聊天会话（可选的会话管理增强）
CREATE TABLE chat_sessions (
    id          VARCHAR(64) PRIMARY KEY,
    user_id     BIGINT       REFERENCES users(id),
    title       VARCHAR(200),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Spring AI Chat Memory（框架自动建表）
-- 表名: ai_chat_memory
```

---

## 四、执行顺序

```
第一轮 ──────────────────────────────────
  ✅ Harness 架构改造（已完成）
  
第二轮 ─── 用户系统基础 ─────────────────
  □ 1.1.1  User 实体
  □ 1.1.2  UserRepository
  □ 1.1.3  UserService（register/login）
  □ 1.1.4  JwtUtil
  □ 1.1.5  JwtAuthFilter
  □ 1.1.6  AuthController（login/register）
  □ 1.3.1  auth store
  □ 1.3.2  LoginView
  □ 1.3.3  RegisterView
  □ 1.3.4  路由 + 导航守卫
  □ 1.3.5  Axios 拦截器

第三轮 ─── 账户管理 + 管理员 ────────────
  □ 1.1.7  UserController（profile）
  □ 1.2.1  AdminController
  □ 1.2.2  Admin 权限校验
  □ 1.4.1  ProfileView
  □ 1.5.1  AdminUsersView
  □ 1.5.2  管理员路由

第四轮 ─── 会话持久化 ───────────────────
  □ 2.1.1  JDBC ChatMemory 配置
  □ 2.1.2  数据源配置
  □ 2.1.3  替换 ShopMateApp 存储
  □ 2.1.4  会话与用户绑定
```

---

## 五、依赖变更

```xml
<!-- pom.xml 新增 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<!-- Spring AI JDBC ChatMemory（启用后自动建表） -->
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-model-chat-memory-repository-jdbc</artifactId>
</dependency>
```

---

## 六、接口一览

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | `/auth/register` | 注册 | ❌ |
| POST | `/auth/login` | 登录，返回 JWT | ❌ |
| GET | `/user/profile` | 获取用户信息 | ✅ |
| PUT | `/user/profile` | 修改用户名/邮箱/头像 | ✅ |
| PUT | `/user/password` | 修改密码 | ✅ |
| GET | `/admin/users` | 用户列表（分页） | ✅ ADMIN |
| PUT | `/admin/users/{id}/role` | 修改用户角色 | ✅ ADMIN |
| DELETE | `/admin/users/{id}` | 删除用户 | ✅ ADMIN |

---

> 💡 **提示**：按轮次逐步开发，每轮完成后验证编译 + 手动测试，再进入下一轮。
