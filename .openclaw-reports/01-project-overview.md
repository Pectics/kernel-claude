# OpenClaw 项目整体架构概览

## 1. 项目简介

OpenClaw 是一个**个人 AI 助手**，用户可以在自己的设备上运行。它通过用户已经使用的渠道（WhatsApp、Telegram、Slack、Discord、Google Chat、Signal、iMessage、BlueBubbles、IRC、Microsoft Teams、Matrix、Feishu、LINE、Mattermost、Nextcloud Talk、Nostr、Synology Chat、Tlon、Twitch、Zalo、Zalo Personal、WebChat）与用户交互。可以在 macOS/iOS/Android 上进行语音对话，并可以渲染用户控制的实时 Canvas。

## 2. 技术栈

| 类别 | 技术 |
|------|------|
| **运行时** | Node.js 22.12.0+ |
| **包管理** | pnpm (workspace) |
| **构建工具** | tsdown (自定义 TypeScript 构建器) |
| **测试框架** | Vitest (多配置文件) |
| **UI 框架** | Lit (用于 TUI 和 Canvas) |
| **CLI 框架** | Commander.js |
| **容器化** | Docker (多阶段构建) |
| **移动端** | Kotlin (Android), Swift (iOS) |
| **AI 模型** | 支持多种 LLM 提供商（OpenAI、Anthropic 等） |

## 3. 项目入口

### 启动流程

```
openclaw.mjs → src/entry.ts → src/index.ts → src/cli/run-main.ts
```

### 详细流程

1. **openclaw.mjs** - 主入口点
   - 检查 Node.js 版本（≥22.12.0）
   - 启用编译缓存
   - 尝试导入 dist/entry.js 或 dist/entry.mjs

2. **src/entry.ts** - 入口包装器
   - 进程标题设置
   - 环境变量标准化
   - 实验性警告抑制
   - 快速路径处理（--version, --help）
   - 调用 runCli

3. **src/index.ts** - 主程序逻辑
   - 环境配置加载
   - 错误处理设置
   - 构建 CLI 程序
   - 导出核心功能

4. **src/cli/run-main.ts** - CLI 运行时
   - 参数解析和路由
   - 命令注册
   - CLI 执行

## 4. 目录结构

```
src/
├── acp/                    # Agent Client Protocol
│   ├── control-plane/      # 控制平面
│   └── runtime/            # 运行时
├── agents/                 # AI 代理系统
│   ├── auth-profiles/      # 认证配置
│   ├── cli-runner/         # CLI 运行器
│   ├── pi-embedded-*       # Pi-Agent 集成
│   ├── sandbox/            # 沙盒环境
│   ├── skills/             # 技能定义
│   └── tools/              # 工具集
├── auto-reply/             # 自动回复系统
├── browser/                # 浏览器集成
├── canvas-host/           # Canvas 宿主
├── channels/               # 消息渠道
│   ├── allowlists/         # 允许列表
│   ├── plugins/            # 渠道插件
│   ├── telegram/           # Telegram 集成
│   ├── transport/          # 传输层
│   └── web/                # Web 渠道
├── cli/                    # CLI 命令
│   ├── browser-cli-*       # 浏览器 CLI
│   ├── cron-cli/           # 定时任务 CLI
│   ├── daemon-cli/         # 守护进程 CLI
│   ├── gateway-cli/        # 网关 CLI
│   ├── node-cli/           # 节点 CLI
│   ├── nodes-cli/          # 节点管理 CLI
│   ├── program/            # 程序构建
│   └── update-cli/         # 更新 CLI
├── commands/               # 命令定义
├── config/                 # 配置管理
├── cron/                   # 定时任务
├── daemon/                 # 守护进程
├── discord/                # Discord 集成
├── gateway/                # 网关核心
├── hooks/                  # 钩子系统
├── imessage/               # iMessage 集成
├── infra/                  # 基础设施
├── line/                   # LINE 集成
├── memory/                 # 记忆系统
├── node-host/              # 节点宿主
├── pairing/                # 配对系统
├── plugin-sdk/            # 插件 SDK
├── plugins/                # 插件实现
├── providers/              # 服务提供商
├── routing/                # 路由系统
├── scripts/                # 构建脚本
├── security/               # 安全模块
├── sessions/               # 会话管理
├── signal/                 # Signal 集成
├── slack/                  # Slack 集成
├── telegram/               # Telegram 机器人
├── terminal/               # 终端界面
├── test-helpers/          # 测试助手
├── test-utils/             # 测试工具
├── tts/                    # 文本转语音
├── tui/                    # 终端用户界面
├── types/                  # 类型定义
├── utils/                  # 工具函数
├── web/                    # Web 集成
└── whatsapp/              # WhatsApp 集成
```

## 5. 依赖关系

### 核心依赖

| 依赖 | 用途 |
|------|------|
| **@agentclientprotocol/sdk** | 代理客户端协议 |
| **@whiskeysockets/baileys** | WhatsApp 集成 |
| **grammy** | Telegram 机器人框架 |
| **@slack/bolt** | Slack 集成 |
| **@discordjs/voice** | Discord 语音 |
| **@line/bot-sdk** | LINE 集成 |
| **express** | Web 服务器 |
| **sqlite-vec** | 向量搜索 |
| **sharp** | 图像处理 |
| **playwright-core** | 浏览器自动化 |

### 对等依赖（可选）

| 依赖 | 用途 |
|------|------|
| **@napi-rs/canvas** | Canvas 渲染 |
| **node-llama-cpp** | 本地 LLM 支持 |

## 6. 构建配置

### 构建系统

- **tsdown**: 自定义 TypeScript 构建器
- **多入口构建**: 支持 CLI、SDK、插件等多个入口
- **插件 SDK**: 支持多个通讯平台的 SDK 导出

### Docker 配置

- 多阶段构建，减小镜像大小
- 支持 slim 变体
- 可选扩展依赖构建时注入
- 基于 Node.js 22-bookworm

### 测试配置

| 配置文件 | 用途 |
|---------|------|
| vitest.unit | 单元测试 |
| vitest.e2e | 端到端测试 |
| vitest.live | 实际环境测试 |
| vitest.gateway | 网关测试 |
| vitest.channels | 渠道测试 |
| vitest.extensions | 扩展测试 |
| docker 测试 | 完整容器环境测试 |

### 开发工具

- **oxfmt**: 代码格式化
- **oxlint**: 代码检查
- **jscpd**: 代码重复检测
- **pre-commit**: Git 钩子

## 7. 关键特性

1. **多渠道支持**: 集成了 20+ 个主流通讯平台
2. **本地部署**: 所有数据本地存储，保护隐私
3. **插件系统**: 可扩展的插件架构
4. **跨平台**: 支持 macOS、Linux、Windows（WSL2）、Android、iOS
5. **本地 LLM 支持**: 可选的本地模型运行
6. **实时界面**: 支持终端 UI 和 Canvas 渲染
