# OpenClaw 核心模块架构

## 1. Agent 系统

### 1.1 Agent 定义
**代码位置：** `src/agents/agent-scope.ts`

Agent 系统的核心定义在 `agent-scope.ts` 中，主要包含以下关键概念：

- **Agent ID 管理**：支持多 Agent 实例，每个 Agent 有唯一的 ID（默认为 "main"）
- **Agent 配置解析**：`resolveAgentConfig()` 函数解析每个 Agent 的配置
- **模型配置**：支持主备模型配置，通过 `model.primary` 和 `model.fallbacks` 定义
- **技能过滤**：每个 Agent 可以配置特定的技能集

```typescript
type AgentConfig = {
  id: string;
  default?: boolean;
  name?: string;
  workspace?: string;
  agentDir?: string;
  model?: AgentModelConfig;
  skills?: string[];
  memorySearch?: MemorySearchConfig;
  humanDelay?: HumanDelayConfig;
  identity?: IdentityConfig;
  groupChat?: GroupChatConfig;
  subagents?: SubagentConfig;
  sandbox?: AgentSandboxConfig;
  params?: Record<string, unknown>;
  tools?: AgentToolsConfig;
  runtime?: AgentRuntimeConfig;
};
```

### 1.2 Agent 生命周期
**创建流程：**
1. 通过 `resolveDefaultAgentId()` 确定默认 Agent
2. 通过 `resolveSessionAgentIds()` 解析会话级 Agent ID
3. 使用 `resolveAgentConfig()` 加载特定配置
4. 支持 ACP（Agent Control Plane）运行时模式

**运行时管理：**
- 支持嵌入式运行时和 ACP 运行时两种模式
- 通过 `acp-spawn.ts` 管理 Agent 的启动和会话绑定
- 支持子 Agent 的创建和管理

### 1.3 关键实现
**核心特性：**
- **多 Agent 支持**：每个 Agent 有独立的工作空间和配置
- **会话绑定**：支持将消息绑定到特定 Agent 的会话
- **模型回退**：支持主备模型自动切换
- **认证轮换**：通过 `auth-profiles` 模块支持多个 API Key 轮换

---

## 2. 上下文引擎

### 2.1 上下文引擎定义
**代码位置：** `src/context-engine/types.ts`

ContextEngine 是可插拔的上下文管理接口，定义了以下核心方法：

```typescript
interface ContextEngine {
  readonly info: ContextEngineInfo;

  // 可选初始化
  bootstrap?(params: { sessionId: string; sessionFile: string }): Promise<BootstrapResult>;

  // 消息摄入
  ingest(params: { sessionId: string; message: AgentMessage; isHeartbeat?: boolean }): Promise<IngestResult>;

  // 批量摄入
  ingestBatch?(params: { sessionId: string; messages: AgentMessage[]; isHeartbeat?: boolean }): Promise<IngestBatchResult>;

  // 组装模型上下文
  assemble(params: { sessionId: string; messages: AgentMessage[]; tokenBudget?: number }): Promise<AssembleResult>;

  // 压缩上下文
  compact(params: { sessionId: string; sessionFile: string; tokenBudget?: number; force?: boolean }): Promise<CompactResult>;
}
```

### 2.2 上下文管理机制
**存储结构：**
- 每个会话独立的上下文存储
- 支持消息历史和状态持久化
- 自动压缩机制控制 token 使用

**检索方式：**
- 基于预算的上下文组装
- 支持时间衰减的优先级排序
- 自动去重和摘要生成

### 2.3 与 AI 模型集成
**集成方式：**
- 通过 `assemble()` 方法准备模型输入
- 支持自定义系统提示添加
- 管理上下文窗口限制
- 提供压缩优化以节省成本

---

## 3. 记忆系统

### 3.1 记忆存储结构
**代码位置：** `src/memory/manager.ts` 和 `src/memory/types.ts`

记忆系统使用向量数据库进行语义搜索，支持：

```typescript
type MemorySearchResult = {
  path: string;
  startLine: number;
  endLine: number;
  score: number;
  snippet: string;
  source: MemorySource;
  citation?: string;
};
```

**存储后端：**
- SQLite 向量数据库（内置）
- QMD（Query Memory Database）扩展
- 支持多种嵌入模型（OpenAI、Gemini、Voyage 等）

### 3.2 记忆检索和使用
**检索机制：**
- 基于语义相似度的向量搜索
- 支持多源搜索（memory 和 sessions）
- 评分和相关性排序
- 支持最小阈值过滤

**使用方式：**
- 通过 `MemorySearchManager` 接口进行查询
- 支持异步批量嵌入
- 自动去重和合并结果
- 支持文件级和代码级搜索

---

## 4. 会话管理

### 4.1 会话创建和维护
**代码位置：** `src/config/sessions/store.ts`

会话管理系统负责：

- **会话标识**：使用 UUID 格式的会话 ID
- **状态持久化**：基于文件的会话状态存储
- **缓存机制**：45秒 TTL 的会话缓存
- **写入锁**：防止并发写入冲突

### 4.2 会话状态管理
**核心功能：**
- **消息历史**：维护完整的对话历史
- **元数据管理**：存储会话相关的元数据
- **传递上下文**：管理跨消息的传递信息
- **磁盘预算**：自动清理过期会话

**会话类型：**
- 主会话（main session）
- 线程会话（thread sessions）
- 对等会话（peer sessions）
- 组聊会话（group sessions）

---

## 5. 路由系统

### 5.1 消息路由机制
**代码位置：** `src/routing/session-key.ts`

路由系统使用结构化的会话键进行消息路由：

```typescript
// 会话键格式
agent:{agentId}:{mainKey}
agent:{agentId}:{channel}:{peerKind}:{peerId}
```

**路由规则：**
- 基于 Agent ID 的路由
- 支持频道和用户级别的路由
- 支持群聊和私聊的区分
- 支持身份链接的解析

### 5.2 请求分发逻辑
**分发机制：**
- **绑定匹配**：通过 `AgentBindingMatch` 进行路由匹配
- **Agent 路由绑定**：`AgentRouteBinding` 定义路由规则
- **ACP 绑定**：支持 Agent Control Plane 的特殊路由
- **回退机制**：没有匹配时的默认路由

---

## 6. 提供者系统

### 6.1 AI 模型提供者抽象
**代码位置：** `src/config/types.models.ts`

提供者系统定义了统一的模型接口：

```typescript
type ModelProviderConfig = {
  baseUrl: string;
  apiKey?: SecretInput;
  auth?: ModelProviderAuthMode;
  api?: ModelApi;
  injectNumCtxForOpenAICompat?: boolean;
  headers?: Record<string, SecretInput>;
  authHeader?: boolean;
  models: ModelDefinitionConfig[];
};
```

### 6.2 不同模型的适配方式
**支持的模型 API：**
- OpenAI Completions/Responses
- Anthropic Messages
- Google Generative AI
- GitHub Copilot
- AWS Bedrock
- Ollama

**适配机制：**
- 统一的模型定义配置
- 兼容性配置（`ModelCompatConfig`）
- 自动认证和头部注入
- 成本和限制管理

---

## 7. 模块间数据流

### 7.1 数据流动路径
```
用户输入 → 路由系统 → Agent 系统
                    ↓
                 上下文引擎
                    ↓
         记忆系统 ← AI 模型提供者
                    ↓
                 会话管理
                    ↓
                 输出响应
```

### 7.2 关键交互点
1. **路由到 Agent**：路由系统根据会话键决定处理消息的 Agent
2. **上下文准备**：上下文引擎从历史记录和记忆系统中准备输入上下文
3. **AI 调用**：Agent 使用准备好的上下文调用 AI 模型
4. **响应处理**：响应通过会话系统持久化，并更新记忆系统
5. **状态同步**：所有模块通过配置系统保持状态一致

### 7.3 数据一致性保证
- **写入锁**：确保会话状态的原子性写入
- **缓存机制**：优化性能同时保证数据一致性
- **版本控制**：配置和状态的版本管理
- **错误恢复**：失败时的回滚和恢复机制

---

## 总结

这个架构设计展现了 OpenClaw 作为现代 AI Agent 系统的核心能力，通过模块化的设计实现了高度的可扩展性和可维护性。每个模块都有清晰的职责边界，同时通过标准化的接口实现松耦合的协作。
