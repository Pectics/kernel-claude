# OpenClaw 扩展系统架构

## 1. 插件 SDK

### 1.1 插件接口

OpenClaw 的插件系统通过 `OpenClawPluginApi` 接口提供强大的扩展能力。核心接口定义在 `src/plugins/types.ts` 中：

```typescript
export type OpenClawPluginApi = {
  id: string;
  name: string;
  version?: string;
  description?: string;
  source: string;
  config: OpenClawConfig;
  pluginConfig?: Record<string, unknown>;
  runtime: PluginRuntime;
  logger: PluginLogger;

  // 注册方法
  registerTool: (tool: AnyAgentTool | OpenClawPluginToolFactory, opts?: OpenClawPluginToolOptions) => void;
  registerHook: (events: string | string[], handler: InternalHookHandler, opts?: OpenClawPluginHookOptions) => void;
  registerHttpRoute: (params: OpenClawPluginHttpRouteParams) => void;
  registerChannel: (registration: OpenClawPluginChannelRegistration | ChannelPlugin) => void;
  registerGatewayMethod: (method: string, handler: GatewayRequestHandler) => void;
  registerCli: (registrar: OpenClawPluginCliRegistrar, opts?: { commands?: string[] }) => void;
  registerService: (service: OpenClawPluginService) => void;
  registerProvider: (provider: ProviderPlugin) => void;
  registerCommand: (command: OpenClawPluginCommandDefinition) => void;
  registerContextEngine: (id: string, factory: ContextEngineFactory) => void;

  resolvePath: (input: string) => string;
  on: <K extends PluginHookName>(hookName: K, handler: PluginHookHandlerMap[K], opts?: { priority?: number }) => void;
};
```

### 1.2 开发 API

插件 SDK 提供了以下核心 API：

#### 工具注册
- `registerTool`: 注册自定义 Agent 工具
- 支持直接工具实例或工厂函数
- 支持工具选项配置（名称、可选性等）

#### 钩子系统
- `registerHook`: 注册生命周期钩子
- `on`: 注册插件生命周期钩子
- 支持事件优先级设置

#### 服务扩展
- `registerHttpRoute`: 注册 HTTP 路由
- `registerCli`: 注册 CLI 命令
- `registerService`: 注册后台服务
- `registerProvider`: 注册 AI 模型提供者

#### 通道扩展
- `registerChannel`: 注册新的消息通道
- 支持 `ChannelPlugin` 类型的完整实现

#### 命令系统
- `registerCommand`: 注册自定义命令，绕过 LLM 直接处理

### 1.3 示例插件

插件有两种定义方式：

**对象形式**：
```typescript
export type OpenClawPluginDefinition = {
  id?: string;
  name?: string;
  description?: string;
  version?: string;
  kind?: PluginKind; // "memory" | "context-engine"
  configSchema?: OpenClawPluginConfigSchema;
  register?: (api: OpenClawPluginApi) => void | Promise<void>;
  activate?: (api: OpenClawPluginApi) => void | Promise<void>;
};
```

**函数形式**：
```typescript
export type OpenClawPluginModule =
  | OpenClawPluginDefinition
  | ((api: OpenClawPluginApi) => void | Promise<void>);
```

---

## 2. 扩展系统

### 2.1 扩展与插件的区别

| 类型 | 位置 | 主要用途 |
|------|------|---------|
| **扩展 (Extensions)** | `extensions/` | 通道集成（telegram、discord、whatsapp 等） |
| **插件 (Plugins)** | `plugins/` | 通用扩展（工具、钩子、服务等） |

### 2.2 扩展加载机制

扩展加载通过 `PluginRuntime` 实现，提供：
- 配置管理
- 环境解析
- 服务生命周期管理
- 安全边界

---

## 3. 钩子系统

### 3.1 钩子点

OpenClaw 提供了丰富的钩子点，覆盖整个生命周期：

```typescript
export type PluginHookName =
  // 模型相关
  | "before_model_resolve"     // 模型解析前
  | "before_prompt_build"      // 提示构建前
  // Agent 生命周期
  | "before_agent_start"       // Agent 启动前
  | "agent_end"                // Agent 结束
  // LLM 交互
  | "llm_input"                // LLM 输入
  | "llm_output"               // LLM 输出
  // 上下文压缩
  | "before_compaction"        // 压缩前
  | "after_compaction"         // 压缩后
  // 会话管理
  | "before_reset"             // 重置前
  | "session_start"            // 会话开始
  | "session_end"              // 会话结束
  // 消息处理
  | "message_received"         // 消息接收
  | "message_sending"          // 消息发送
  | "message_sent"             // 消息已发送
  | "before_message_write"     // 消息写入前
  // 工具调用
  | "before_tool_call"         // 工具调用前
  | "after_tool_call"          // 工具调用后
  | "tool_result_persist"      // 工具结果持久化
  // 子 Agent
  | "subagent_spawning"        // 子 Agent 生成
  | "subagent_spawned"         // 子 Agent 已生成
  | "subagent_ended"           // 子 Agent 结束
  // 网关
  | "gateway_start"            // 网关启动
  | "gateway_stop";            // 网关停止
```

### 3.2 钩子注册

```typescript
registerHook: (
  events: string | string[],
  handler: InternalHookHandler,
  opts?: OpenClawPluginHookOptions,
) => void;
```

**特性**：
- 支持通配符事件（如 "message:*"）
- 并发执行，错误不影响其他钩子
- 提供详细的上下文信息

---

## 4. 命令系统

### 4.1 命令定义

```typescript
export type ChatCommandDefinition = {
  key: string;                     // 命令键
  nativeName?: string;             // 原生命令名
  description: string;             // 描述
  textAliases: string[];           // 文本别名
  acceptsArgs?: boolean;           // 是否接受参数
  argsParsing?: "none" | "rest";   // 参数解析方式
  scope: "chat" | "cli" | "both";  // 作用域
  category?: string;               // 分类
};
```

### 4.2 命令解析流程

1. **文本命令检测**: 通过正则表达式识别命令前缀
2. **别名解析**: 支持多个文本别名
3. **参数解析**: 支持 REST 参数
4. **权限检查**: 基于配置的访问控制
5. **执行**: 调用相应的处理函数

---

## 5. 技能系统

### 5.1 技能定义

技能位于 `skills/` 目录，每个技能都是一个独立的目录：

```
skills/my-skill/
├── index.ts      # 技能入口
├── package.json  # 依赖声明
└── README.md     # 文档
```

### 5.2 技能调用机制

1. **发现**: 扫描启用的技能目录
2. **注册**: 将技能注册为系统命令
3. **路由**: 命令路由到相应的技能处理器
4. **执行**: 技能执行并返回结果

**权限控制**：
- 支持基于配置的访问控制
- 可以通过 `allowlist` 和 `denylist` 管理

---

## 6. 提供者扩展

### 6.1 AI 模型提供者

```typescript
export type ProviderPlugin = {
  id: string;
  label: string;
  docsPath?: string;
  aliases?: string[];
  envVars?: string[];
  models?: ModelProviderConfig;
  auth: ProviderAuthMethod[];       // 认证方法
  formatApiKey?: (cred: AuthProfileCredential) => string;
  refreshOAuth?: (cred: OAuthCredential) => Promise<OAuthCredential>;
};
```

### 6.2 认证支持

| 认证方式 | 描述 |
|---------|------|
| **API Key** | 直接 API 密钥 |
| **OAuth** | OAuth 2.0 流程 |
| **Token** | Bearer Token |
| **Device Code** | 设备码认证 |
| **Custom** | 自定义认证 |

### 6.3 添加新的 AI 提供者

1. 实现 `ProviderPlugin` 接口
2. 定义认证方法（API Key、OAuth 等）
3. 注册到系统
4. 配置模型参数

---

## 7. 扩展开发指南

### 7.1 开发插件

```typescript
// plugin.ts
export default {
  id: "my-plugin",
  name: "My Plugin",
  description: "A sample plugin",
  register: (api) => {
    api.registerTool(myTool);
    api.registerHook("message_received", myHandler);
  }
};
```

配置启用：
```json
{
  "plugins": {
    "my-plugin": {
      "enabled": true
    }
  }
}
```

### 7.2 开发通道扩展

```typescript
export const myChannelPlugin: ChannelPlugin = {
  id: "my-channel",
  meta: {
    label: "My Channel",
    capabilities: ["messaging", "commands"],
  },
  // ... 实现各种适配器
};

// 注册
api.registerChannel(myChannelPlugin);
```

### 7.3 最佳实践

| 方面 | 建议 |
|------|------|
| **安全性** | 所有外部输入都需要验证 |
| **错误处理** | 提供友好的错误信息 |
| **日志记录** | 使用提供的 logger 进行日志记录 |
| **性能** | 避免阻塞操作，使用异步处理 |
| **配置** | 提供灵活的配置选项 |

---

## 总结

OpenClaw 的扩展系统设计非常优雅和强大：

| 设计特点 | 描述 |
|---------|------|
| **模块化设计** | 每个组件都是独立的模块 |
| **事件驱动** | 通过钩子系统实现松耦合 |
| **类型安全** | 使用 TypeScript 确保类型安全 |
| **安全边界** | 沙箱机制确保安全 |
| **灵活配置** | 支持多种配置方式 |

这种设计使得 OpenClaw 可以轻松集成各种第三方服务，同时保持系统的稳定性和可维护性。
