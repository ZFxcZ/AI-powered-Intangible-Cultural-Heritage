# Vue 3 非遗文化传承平台 - 项目上下文

## 项目概述

这是一个基于 **Vue 3 + Vite + Ant Design Vue** 构建的**非遗文化传承平台**前端项目。项目采用前后端分离架构，主要功能包括：

- **前台展示**：非遗作品展示、传承人介绍、活动中心、在线课程、非遗手办商城
- **后台管理**：用户管理、非遗作品管理、传承人管理、活动管理、课程管理、商品管理、订单管理
- **AI 智能助手**：集成 AI 聊天功能
- **用户系统**：登录、注册、找回密码、个人中心、订单管理

### 核心技术栈

| 类别        | 技术                                                                          |
| ----------- | ----------------------------------------------------------------------------- |
| 前端框架    | Vue 3.2+                                                                      |
| 构建工具    | Vite 4.5+                                                                     |
| UI 组件库   | Ant Design Vue 4.0+                                                           |
| 状态管理    | Pinia 3.0+ / Vuex 4.1+                                                        |
| 路由管理    | Vue Router 4.0+                                                               |
| HTTP 客户端 | Axios 1.7+                                                                    |
| 图表库      | ECharts 6.0+                                                                  |
| 样式预处理  | SCSS / Less                                                                   |
| 代码规范    | ESLint + eslint-plugin-vue                                                    |
| 其他依赖    | marked (Markdown渲染), qrcode (二维码), @microsoft/fetch-event-source (SSE流) |

## 项目结构

```
vue3/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API 接口封装
│   │   ├── ActivityApi.js
│   │   ├── AddressApi.js
│   │   ├── AiChatApi.js
│   │   ├── CourseApi.js
│   │   ├── dashboard.js
│   │   ├── FileApi.js
│   │   ├── HeritageApi.js
│   │   ├── InheritorApi.js
│   │   ├── OrderApi.js
│   │   ├── ShopCategoryApi.js
│   │   ├── ShopProductApi.js
│   │   ├── user.js
│   │   └── index.js
│   ├── assets/            # 静态资源(图片、字体等)
│   ├── components/        # 公共组件
│   ├── composables/       # 组合式函数
│   ├── config/            # 配置文件
│   ├── layouts/           # 布局组件
│   │   ├── FrontendLayout.vue   # 前台布局
│   │   ├── BackendLayout.vue    # 后台布局
│   │   └── AuthLayout.vue       # 认证布局
│   ├── router/            # 路由配置
│   │   └── index.js
│   ├── store/             # 状态管理 (Pinia)
│   │   ├── app.js
│   │   ├── heritage.js
│   │   └── user.js
│   ├── styles/            # 全局样式
│   ├── utils/             # 工具函数
│   ├── views/             # 页面组件
│   │   ├── auth/          # 认证页面 (登录/注册/找回密码)
│   │   ├── backend/       # 后台管理页面
│   │   ├── frontend/      # 前台展示页面
│   │   ├── profile/       # 个人中心
│   │   └── error/         # 错误页面 (404等)
│   ├── App.vue
│   └── main.js
├── .env                   # 通用环境变量
├── .env.development       # 开发环境变量
├── .env.production        # 生产环境变量
├── vite.config.js         # Vite 配置
├── .eslintrc.js           # ESLint 配置
├── jsconfig.json          # JS/TS 配置
├── index.html             # HTML 模板
└── package.json
```

## 开发指南

### 环境要求

- Node.js >= 16 (推荐 18+)
- npm >= 8 或 pnpm >= 7

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

- 默认访问地址：`http://localhost:8800`
- 开发服务器会自动打开浏览器

### 构建生产版本

```bash
# 生产环境构建
npm run build

# 预发环境构建
npm run build:staging
```

### 预览生产构建

```bash
npm run preview
```

### 代码检查

```bash
npm run lint
```

## 环境配置

### 环境变量说明

| 变量名              | 说明          | 开发环境值                           | 生产环境值                   |
| ------------------- | ------------- | ------------------------------------ | ---------------------------- |
| `VITE_API_BASE_URL` | API 基础地址  | `http://7e22aae6.r15.cpolar.top/api` | -                            |
| `VUE_APP_BASE_API`  | 旧版 API 前缀 | `/api`                               | `https://api.yourdomain.com` |
| `VUE_APP_TITLE`     | 应用标题      | `你的应用名称`                       | -                            |

### 代理配置

开发服务器配置了以下代理规则（见 `vite.config.js`）：

- `/api` → `https://63d2907b.r15.cpolar.top`
- `/files` → `https://63d2907b.r15.cpolar.top`

## 路由结构

### 前台路由

| 路径             | 说明         | 需要登录 |
| ---------------- | ------------ | -------- |
| `/home`          | 首页         | 否       |
| `/heritage`      | 非遗作品列表 | 否       |
| `/heritage/:id`  | 作品详情     | 否       |
| `/inheritor`     | 传承人列表   | 否       |
| `/inheritor/:id` | 传承人详情   | 否       |
| `/activity`      | 活动中心     | 否       |
| `/activity/:id`  | 活动详情     | 否       |
| `/course`        | 在线课程     | 否       |
| `/course/:id`    | 课程详情     | 否       |
| `/shop`          | 非遗手办商城 | 否       |
| `/shop/:id`      | 商品详情     | 否       |
| `/ai-chat`       | AI智能助手   | **是**   |
| `/orders`        | 我的订单     | **是**   |
| `/profile`       | 个人中心     | **是**   |

### 后台路由 (`/back/*`)

| 路径                  | 说明         |
| --------------------- | ------------ |
| `/back/dashboard`     | 控制台首页   |
| `/back/user`          | 用户管理     |
| `/back/heritage`      | 非遗作品管理 |
| `/back/inheritor`     | 传承人管理   |
| `/back/activity`      | 活动管理     |
| `/back/course`        | 课程管理     |
| `/back/shop/category` | 商品分类管理 |
| `/back/shop/product`  | 商品管理     |
| `/back/shop/orders`   | 订单管理     |
| `/back/profile`       | 个人信息     |

### 认证路由

| 路径                    | 说明     |
| ----------------------- | -------- |
| `/auth/login`           | 登录     |
| `/auth/register`        | 注册     |
| `/auth/forgot-password` | 找回密码 |

## 编码规范

- 使用 ESLint 进行代码检查，遵循 `plugin:vue/vue3-recommended`
- 允许单字组件名称（`vue/multi-word-component-names: off`）
- 使用 Path Alias `@/` 指向 `src/` 目录
- 推荐使用 Composition API（Vue 3 风格）

## 重要说明

1. **状态管理**：项目同时使用了 Pinia 和 Vuex，推荐在新代码中使用 Pinia
2. **API 调用**：所有 API 调用封装在 `src/api/` 目录下
3. **路由守卫**：实现了基于用户角色的路由访问控制（普通用户 vs 管理员）
4. **AI 聊天**：使用 `@microsoft/fetch-event-source` 实现 SSE 流式响应
5. **Markdown 渲染**：使用 `marked` 库进行 Markdown 解析
