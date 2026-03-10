# OpenClaw 通信渠道架构

## 1. 渠道抽象层
### 1.1 通用接口
OpenClaw 的通信渠道采用插件化架构，核心接口定义在 `src/channels/plugins/types.core.ts` 和 `src/channels/plugins/types.adapters.ts` 中。

主要接口包括：
- **ChannelSetupAdapter**: 渠道设置适配器，负责账户配置和验证
- **ChannelConfigAdapter**: 渠道配置适配器，管理账户状态和配置
- **ChannelOutboundAdapter**: 外发消息适配器，处理消息发送逻辑
- **ChannelStatusAdapter**: 状态监控适配器，提供健康检查和审计功能
- **ChannelGatewayAdapter**: 网关适配器，支持网关模式通信

每个渠道都需要实现这些适配器来与 OpenClaw 核心系统集成。

### 1.2 适配器模式
系统采用适配器模式统一不同渠道的差异：
- **标准化接口**: 所有渠道通过统一的适配器接口与核心系统交互
- **类型安全**: 使用 TypeScript 确保类型安全
- **功能分层**: 将不同功能分离到不同的适配器中，提高可维护性
- **插件注册**: 通过 `src/channels/registry.ts` 管理所有支持的渠道

## 2. Discord 渠道
### 2.1 实现方式
- **位置**: `src/discord/`
- **核心库**: 使用 `@buape/carbon` 作为 Discord 客户端框架
- **依赖**: Discord API v10, WebSocket 连接
- **认证**: Bot Token 认证

关键实现文件：
- `monitor.ts`: 主监控服务，处理消息接收和事件
- `send.outbound.ts`: 消息发送逻辑，支持文本、媒体、组件
- `accounts.ts`: 账户管理
- `probe.ts`: 连接健康检查

### 2.2 消息处理
- **消息接收**: 通过 Discord Gateway 实时接收消息
- **消息发送**: 支持文本分块（4096字符限制）、嵌入、组件
- **线程支持**: 支持 Discord 线程消息和论坛帖子
- **媒体处理**: 支持图片、文件、语音消息发送
- **权限控制**: 基于 allowlist 的访问控制

## 3. Slack 渠道
### 3.1 实现方式
- **位置**: `src/slack/`
- **核心库**: `@slack/web-api`, Socket Mode
- **认证**: Bot Token + Socket Mode 连接
- **特性**: 支持 Blocks、Markdown 转换

关键实现文件：
- `monitor.ts`: 监控服务，处理事件和消息
- `send.ts`: 消息发送，支持文本和 blocks
- `accounts.ts`: 账户管理
- `format.ts`: Markdown 到 Slack 格式转换

### 3.2 事件处理机制
- **Socket Mode**: 保持实时连接
- **消息路由**: 支持 DM 和频道消息
- **线程回复**: 支持 Slack 线程回复
- **权限验证**: 基于 allowlist 和频道策略
- **错误处理**: 自动重试和降级策略

## 4. Telegram 渠道
### 4.1 实现方式
- **位置**: `src/telegram/`
- **核心库**: `grammY` 框架
- **认证**: Bot Token
- **模式**: 支持轮询和 Webhook 两种模式

关键实现文件：
- `monitor.ts`: 主监控服务
- `bot.ts`: Bot 核心逻辑
- `send.ts`: 消息发送
- `accounts.ts`: 账户管理

### 4.2 消息格式处理
- **格式转换**: Markdown 到 Telegram HTML 格式
- **媒体支持**: 图片、音频、文档发送
- **按钮组件**: 内联按钮和回复按钮
- **群组管理**: 支持 supergroups 和普通群组
- **身份验证**: 基于 allowlist 的用户验证

## 5. 其他渠道
### 5.1 WhatsApp 集成
- **位置**: `src/whatsapp/`
- **实现方式**: 通过 Web API 或客户端库
- **消息处理**: E164 号码标准化，群组/用户消息区分
- **特性**: 支持文本、图片、语音消息

### 5.2 LINE 集成
- **位置**: `src/line/`
- **API**: LINE Messaging API
- **认证**: Channel Access Token
- **特性**: 支持模板消息、Flex 消息、富菜单

### 5.3 iMessage 集成
- **位置**: `src/imessage/`
- **实现**: 通过 macOS 消息应用集成
- **特性**: 原生消息应用支持，无需额外服务器

### 5.4 Signal 集成
- **位置**: `src/signal/`
- **实现**: 通过 signal-cli 命令行工具
- **认证**: 设备链接
- **特性**: 端到端加密，群组支持

## 6. 网关系统
### 6.1 消息网关实现
- **位置**: `src/gateway/`
- **核心功能**: 提供统一的 WebSocket 接口
- **协议转换**: 将各渠道消息转换为统一格式

### 6.2 协议转换
- **消息格式标准化**: 将不同渠道的消息转换为统一的内部格式
- **双向转换**: 支持从内部格式转换为各渠道特定格式
- **实时同步**: 通过 WebSocket 保持实时连接
- **连接管理**: 支持断线重连和连接健康监控

## 7. 渠道与核心系统交互
### 7.1 消息流向
```
用户消息 → 渠道适配器 → 消息路由 → Agent 系统 → 回复处理 → 渠道适配器 → 用户
```

### 7.2 核心交互机制
- **插件化注册**: 各渠道作为插件注册到系统中
- **统一配置**: 通过 OpenClawConfig 统一管理渠道配置
- **错误处理**: 统一的错误处理和重试机制
- **状态监控**: 各渠道健康状态实时监控
- **负载均衡**: 支持多账户和并发处理

### 7.3 关键特性
- **实时性**: 大部分渠道支持实时消息推送
- **可扩展性**: 新渠道通过适配器模式轻松集成
- **安全性**: 支持 allowlist 和权限控制
- **容错性**: 单渠道故障不影响其他渠道运行
- **监控性**: 完整的日志和状态追踪

## 8. 技术栈总结
- **前端**: TypeScript, Node.js
- **Discord**: @buape/carbon, discord-api-types
- **Slack**: @slack/web-api, Socket Mode
- **Telegram**: grammY
- **LINE**: @line/bot-sdk
- **消息处理**: 自定义 Markdown 渲染器
- **网关**: WebSocket, 自定义协议
- **配置管理**: JSON 配置文件 + 环境变量

各渠道实现遵循统一的设计模式，确保系统的一致性和可维护性。
