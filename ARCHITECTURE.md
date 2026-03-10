# Kernel-Claude 架构设计文档

> 基于 Claude Agent SDK 构建的专业级多平台智能体系统

## 目录

- [设计原则](#设计原则)
- [整体架构](#整体架构)
- [核心模块](#核心模块)
- [数据流设计](#数据流设计)
- [核心接口定义](#核心接口定义)
- [技术选型](#技术选型)
- [开发阶段规划](#开发阶段规划)

---

## 设计原则

1. **事件优先**：所有跨模块通信优先使用事件，避免直接依赖调用
2. **优雅降级**：任何模块失败不应导致整体崩溃，提供降级方案
3. **可观测性**：关键路径必须有日志、指标、追踪
4. **配置外置**：敏感配置不进代码，支持运行时热更新
5. **安全第一**：权限归属于事件，防止提示词注入攻击
6. **敏捷迭代**：先跑通核心流程，再逐步完善容错和优化

---

## 整体架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                        外部平台 (External Platforms)                │
│          Telegram  │  OneBot/QQ  │  Discord  │  更多...             │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     平台适配层 (Platform Adapter Layer)             │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐   │
│  │ EventWrapper     │  │ ActionClient     │  │ SkillDocGenerator│   │
│  │ 事件标准化       │  │ 行为接口封装     │  │ SKILL 文档生成   │   │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘   │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        安全层 (Security Layer)                      │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐   │
│  │ PermissionManager│  │ EventSigner      │  │ RateLimiter      │   │
│  │ 权限鉴定与管理   │  │ 事件签名防篡改   │  │ 请求速率限制     │   │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘   │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        事件层 (Event Layer)                         │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐   │
│  │ EventStore       │  │ EventBus         │  │ EventRouter      │   │
│  │ 事件持久化存储   │  │ 事件总线 (双写)  │  │ 智能事件路由     │   │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘   │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       智能体层 (Agent Layer)                        │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐   │
│  │ AgentPool        │  │ AgentRouter      │  │ RateLimitGuard   │   │
│  │ Agent 生命周期   │  │ 授权委托路由     │  │ API 并发控制     │   │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘   │
│  ┌───────────────────────────────────────────────────────────────┐  │
│  │                    Claude Agent SDK Wrapper                   │  │
│  │         主 Agent (调度者)  ←→  子 Agent (执行者)              │  │
│  └───────────────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    可观测层 (Observability Layer)                   │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐   │
│  │ BehaviorLogger   │  │ MetricsCollector │  │ EventTracer      │   │
│  │ 行为日志 (AOP)   │  │ 指标收集         │  │ 事件追踪         │   │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘   │
└───────────────────────────┬─────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────────────┐
│                       表现层 (Presentation Layer)                   │
│  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐   │
│  │ REST API         │  │ WebSocket        │  │ Web Dashboard    │   │
│  │ 管理接口         │  │ 实时推送         │  │ 可视化面板       │   │
│  └──────────────────┘  └──────────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 核心模块

### 1. 平台接口 (Platform Interface)

平台接口是项目连接外部消息渠道的适配层，采用**可扩展设计**。

#### 1.1 事件包装器接口 (Event Wrapper Interface)

将各平台的原始事件转换为统一的标准事件格式。

```
原始事件 (Telegram/OneBot/Discord)
        │
        ▼
┌───────────────────────────┐
│     EventWrapper          │
│  - 解析平台特定格式       │
│  - 提取通用字段           │
│  - 生成标准 KernelEvent   │
└───────────────────────────┘
        │
        ▼
KernelEvent (标准格式)
```

#### 1.2 智能体行为接口 (Agent Behavior Interface)

将平台功能封装为 Agent 可调用的方法，并自动生成 SKILL 文档注入到 Agent 上下文。

```
┌─────────────────────────────────────────────────────────────────┐
│                   Agent Behavior Interface                      │
├─────────────────────────────────────────────────────────────────┤
│  操作接口 (Action Interface)                                    │
│  - sendMessage() → 返回操作结果                                 │
│  - deleteMessage() → 返回操作结果                               │
│  - kickUser() → 返回操作结果                                    │
│                                                                 │
│  资源接口 (Resource Interface)                                  │
│  - getMessages() → 返回消息列表 (支持分页)                      │
│  - getGroupMembers() → 返回成员列表 (支持分页)                  │
│  - getGroupInfo() → 返回群组信息                                │
└─────────────────────────────────────────────────────────────────┘
        │
        ▼ 自动生成
┌─────────────────────────────────────────────────────────────────┐
│                    SKILL 文档 (注入 Agent)                      │
│                                                                 │
│  # Telegram 平台工具                                            │
│                                                                 │
│  ## send_message                                                │
│  - 描述: 发送消息到指定群组/私聊                                │
│  - 参数: chat_id (string), text (string)                        │
│  - 返回: { success: bool, message_id?: string, error?: string } │
│  ...                                                            │
└─────────────────────────────────────────────────────────────────┘
```

---

### 2. 权限管理系统 (Permission Management System)

所有消息输入的必经之路，核心安全模块。

#### 2.1 权限模型

```
┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│   UserGroup  │────→│     User     │────→│ Permission   │
│   用户组     │     │    用户      │     │   权限节点   │
└──────────────┘     └──────────────┘     └──────────────┘

权限节点示例：
- telegram.message.send      → 允许发送 Telegram 消息
- telegram.message.delete    → 允许删除 Telegram 消息
- telegram.admin.kick        → 允许踢人
- agent.tool.web_search      → 允许使用网络搜索工具
```

#### 2.2 事件签名机制

防止提示词注入攻击的核心设计：

```
用户消息 → 权限查询 → 权限注入 → 事件签名

签名事件 (SignedEvent) {
    event: KernelEvent,
    permissions: Set<Permission>,
    signature: HMAC-SHA256(event + permissions + secret),
    timestamp: long
}

Agent 调用工具时：
1. 检查工具所需权限
2. 验证事件签名中的权限是否包含所需权限
3. 签名不匹配 → 拒绝执行
```

---

### 3. 事件总线 (Event Bus)

整个系统的核心模块，采用**双写策略**保证可靠性。

#### 3.1 双写策略

```
                    ┌─────────────────┐
事件发布 ──────────→│   EventBus      │
                    └────────┬────────┘
                             │
              ┌──────────────┴──────────────┐
              ▼                              ▼
    ┌─────────────────┐            ┌─────────────────┐
    │  内存队列       │            │   EventStore    │
    │  (高性能消费)   │            │   (持久化)      │
    └─────────────────┘            └─────────────────┘
              │
              ▼
         事件消费

异常恢复：
- 程序重启时，从 EventStore 加载未处理事件到内存队列
```

#### 3.2 事件优先级

```
优先级队列：
1. CRITICAL  - 系统级事件 (健康检查、关闭信号)
2. HIGH      - 管理员命令
3. NORMAL    - 普通用户消息
4. LOW       - 定时任务、批量操作
```

---

### 4. 事件路由 (Event Router)

实现**智能授权委托**的事件分发机制。

#### 4.1 授权委托机制

```
┌─────────────────────────────────────────────────────────────────┐
│                         主 Agent                                │
│  角色：调度者 (Dispatcher)                                      │
│  职责：                                                         │
│  - 分析事件类型和复杂度                                         │
│  - 决定由谁来处理                                               │
│  - 创建子 Agent 并授权                                          │
│  - 监控子 Agent 状态                                            │
│  - 接收子 Agent 完成报告                                        │
└─────────────────────────────────────────────────────────────────┘
         │
         │ 创建子 Agent + 授权委托
         ▼
┌─────────────────────────────────────────────────────────────────┐
│  Session → Agent 映射表                                         │
│  ┌───────────────────┬───────────────────┐                      │
│  │ session_id        │ agent_id          │                      │
│  ├───────────────────┼───────────────────┤                      │
│  │ "tg_12345_67890"  │ "sub_agent_001"   │                      │
│  │ "qq_98765_43210"  │ "sub_agent_002"   │                      │
│  └───────────────────┴───────────────────┘                      │
└─────────────────────────────────────────────────────────────────┘
         │
         │ 事件路由时查询
         ▼
┌─────────────────────────────────────────────────────────────────┐
│                        子 Agent                                 │
│  角色：执行者 (Executor)                                        │
│  职责：                                                         │
│  - 持有特定会话的授权                                           │
│  - 直接接收该会话的后续事件                                     │
│  - 完成任务后向主 Agent 报告                                    │
│  - 自我销毁                                                     │
└─────────────────────────────────────────────────────────────────┘
```

#### 4.2 路由流程

```
事件到达 → 查询 Session-Agent 映射表
              │
              ├─ 命中 → 直接路由到对应子 Agent
              │
              └─ 未命中 → 路由到主 Agent
                           │
                           ├─ 简单任务 → 主 Agent 直接处理
                           │
                           └─ 复杂/耗时任务 → 创建子 Agent
                                              │
                                              ├─ 写入映射表
                                              └─ 后续事件自动路由
```

---

### 5. 智能体集群 (Agent Cluster)

基于 Claude Agent SDK 构建的任务处理集群。

#### 5.1 Agent 生命周期

```
┌─────────┐    ┌─────────┐    ┌─────────┐    ┌─────────┐
│ CREATED │───→│ RUNNING │───→│REPORTING│───→│DESTROYED│
└─────────┘    └────┬────┘    └─────────┘    └─────────┘
                    │
                    ▼ 异常
               ┌─────────┐
               │  ERROR  │ → 主 Agent 接管
               └─────────┘
```

#### 5.2 API 并发控制

使用**令牌桶 + 请求队列**控制 LLM API 调用速率。

```
┌─────────────────────────────────────────────────────────────────┐
│                      RateLimitGuard                             │
│                                                                 │
│   ┌─────────────┐     ┌─────────────┐     ┌─────────────┐       │
│   │ 请求队列    │ ──→ │令牌桶控制器 │ ──→ │  API 调用   │       │
│   │ (虚拟线程)  │     │             │     │             │       │
│   └─────────────┘     └─────────────┘     └─────────────┘       │
│                                                                 │
│   配置：                                                        │
│   - RPM (Requests Per Minute): 60                               │
│   - TPM (Tokens Per Minute): 100000                             │
│   - 并发上限: 10                                                │
│                                                                 │
│   策略：                                                        │
│   - 有令牌 → 立即发送                                           │
│   - 无令牌 → 进入队列等待 (虚拟线程，几乎零成本)                │
│   - 令牌按需补充 (RPM/TPM 双重限制)                             │
└─────────────────────────────────────────────────────────────────┘
```

---

### 6. 行为记录器 (Behavior Logger)

使用 **AOP (面向切面编程)** 实现无侵入式的行为记录。

#### 6.1 切面设计

```java
@Aspect
@Component
public class BehaviorLogAspect {

    // 拦截所有 Agent 工具调用
    @Around("execution(* me.pectics.kernelclaude.agent..*(..))")
    public Object logAgentBehavior(ProceedingJoinPoint joinPoint) {
        // 1. 记录调用开始
        // 2. 执行原方法
        // 3. 记录调用结果
        // 4. 异步写入日志
    }

    // 拦截所有平台接口调用
    @Around("execution(* me.pectics.kernelclaude.platform..*(..))")
    public Object logPlatformBehavior(ProceedingJoinPoint joinPoint) {
        // ...
    }
}
```

#### 6.2 日志结构

```
BehaviorLog {
    id: UUID,
    timestamp: long,
    type: enum (AGENT_CALL, PLATFORM_CALL, TOOL_CALL),
    source: string,           // 调用来源
    target: string,           // 调用目标
    method: string,           // 方法名
    arguments: object,        // 参数 (脱敏)
    result: object,           // 返回值 (脱敏)
    duration: long,           // 耗时
    success: boolean,         // 是否成功
    errorMessage: string      // 错误信息
}
```

---

### 7. 用户界面 (User Interface)

实时可视化面板，展示系统运行状态。

#### 7.1 数据推送

```
┌─────────────────────────────────────────────────────────────────┐
│                    WebSocket 实时推送                           │
│                                                                 │
│   EventBus ───────────┐                                         │
│                       │                                         │
│   EventRouter ────────┼───→ WebSocketHandler ──→ 前端           │
│                       │                                         │
│   BehaviorLogger ─────┘                                         │
└─────────────────────────────────────────────────────────────────┘
```

#### 7.2 功能模块

- **仪表盘**：系统概览、实时事件流、Agent 状态
- **事件监控**：事件队列状态、处理延迟、失败重试
- **Agent 管理**：Agent 列表、生命周期管理、手动干预
- **行为日志**：操作记录查询、审计追踪
- **配置管理**：平台配置、权限配置、系统参数

---

## 数据流设计

### 完整事件处理流程

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    场景：用户在 Telegram 发送 "查询重庆天气"            │
└─────────────────────────────────────────────────────────────────────────┘

Step 1: 事件接收
┌─────────────┐         ┌─────────────────┐
│  Telegram   │ Webhook │ TelegramAdapter │
│   Server    │ ──────→ │ EventWrapper    │
└─────────────┘         └────────┬────────┘
                                 │
                                 ▼ 标准化
                        ┌─────────────────┐
                        │  KernelEvent    │
                        │  type: MESSAGE  │
                        │  platform: TG   │
                        │  user: xxx      │
                        │  content: "..." │
                        └────────┬────────┘

Step 2: 权限处理
                                 │
                                 ▼
                        ┌─────────────────┐
                        │ PermissionMgr   │
                        │ 查询用户权限    │
                        │ 生成事件签名    │
                        └────────┬────────┘
                                 │
                                 ▼
                        ┌─────────────────┐
                        │  SignedEvent    │
                        │ + permissions   │
                        │ + signature     │
                        └────────┬────────┘

Step 3: 事件入队
                                 │
                                 ▼
                        ┌─────────────────┐
                        │   EventBus      │
                        │ 双写: 内存+DB   │
                        └────────┬────────┘

Step 4: 事件路由
                                 │
                                 ▼
                        ┌─────────────────┐
                        │  EventRouter    │
                        │ 查询映射表      │
                        └────────┬────────┘
                                 │
                    ┌────────────┴────────────┐
                    ▼                         ▼
            已有子Agent                  新会话/主Agent
                    │                         │
                    ▼                         ▼
            ┌─────────────┐          ┌─────────────┐
            │  子 Agent   │          │  主 Agent   │
            │  (直接处理) │          │  (调度决策) │
            └─────────────┘          └─────────────┘

Step 5: Agent 处理 (以主 Agent 为例)
                                 │
                                 ▼
                        ┌─────────────────┐
                        │   主 Agent      │
                        │分析: 需要查天气 │
                        │决策: 简单任务   │
                        │ 自己处理        │
                        └────────┬────────┘
                                 │
              ┌──────────────────┼──────────────────┐
              ▼                  ▼                  ▼
      ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
      │ 权限检查    │    │ 调用工具    │    │调用平台接口 │
      │ telegram.   │    │ web_search  │    │ send_message│
      │ message.send│    │             │    │             │
      └─────────────┘    └─────────────┘    └─────────────┘
              │                  │                  │
              └──────────────────┼──────────────────┘
                                 ▼
                        ┌─────────────────┐
                        │ BehaviorLogger  │
                        │ AOP 自动记录    │
                        └────────┬────────┘

Step 6: 实时推送
                                 │
                                 ▼
                        ┌─────────────────┐
                        │  WebSocket      │
                        │ 推送到前端      │
                        └─────────────────┘
```

---

## 核心接口定义

### KernelEvent (标准事件)

```java
public record KernelEvent(
    String eventId,           // 事件唯一ID
    EventType type,           // 事件类型
    String platform,          // 来源平台
    String platformEventId,   // 平台原始事件ID
    String sessionId,         // 会话ID
    String userId,            // 用户ID
    long timestamp,           // 时间戳
    Map<String, Object> data  // 事件数据
) {
    public enum EventType {
        MESSAGE,              // 消息事件
        MESSAGE_EDIT,         // 消息编辑
        MESSAGE_DELETE,       // 消息删除
        USER_JOIN,            // 用户加入
        USER_LEAVE,           // 用户离开
        COMMAND,              // 命令事件
        SYSTEM                // 系统事件
    }
}
```

### SignedEvent (签名事件)

```java
public record SignedEvent(
    KernelEvent event,
    Set<String> permissions,
    String signature,
    long signedAt
) {
    public boolean hasPermission(String permission) {
        return permissions.contains(permission) ||
               permissions.contains(permission + ".*") ||
               permissions.contains("*");
    }

    public boolean verifySignature(String secret) {
        String expected = HmacUtils.hmacSha256Hex(
            secret,
            event.eventId() + permissions.toString() + signedAt
        );
        return expected.equals(signature);
    }
}
```

### PlatformAdapter (平台适配器接口)

```java
public interface PlatformAdapter {

    // 平台标识
    String getPlatformId();

    // 事件包装
    KernelEvent wrapEvent(Object rawEvent);

    // 行为接口
    interface ActionClient {
        ActionResult sendMessage(String sessionId, String text);
        ActionResult deleteMessage(String messageId);
        ActionResult kickUser(String sessionId, String userId);
    }

    // 资源接口
    interface ResourceClient {
        Page<Message> getMessages(String sessionId, int page, int size);
        Page<User> getMembers(String sessionId, int page, int size);
        GroupInfo getGroupInfo(String sessionId);
    }

    // SKILL 文档生成
    String generateSkillDocumentation();
}
```

### AgentInterface (智能体接口)

```java
public interface Agent {

    String getAgentId();

    AgentType getType();

    void acceptEvent(SignedEvent event);

    void start();

    void stop();

    AgentStatus getStatus();

    void reportToSupervisor(AgentReport report);

    enum AgentType {
        PRIMARY,    // 主 Agent
        WORKER      // 子 Agent
    }

    enum AgentStatus {
        CREATED,
        RUNNING,
        REPORTING,
        DESTROYED,
        ERROR
    }
}
```

### RateLimiter (API 限流器)

```java
public interface RateLimiter {

    // 同步获取许可，返回等待时间（毫秒）
    long acquire(int estimatedTokens);

    // 异步获取许可
    CompletableFuture<Void> acquireAsync(int estimatedTokens);

    // 尝试获取，立即返回是否成功
    boolean tryAcquire(int estimatedTokens);

    // 获取当前状态
    RateLimitStatus getStatus();
}

public record RateLimitStatus(
    int availableRpmTokens,
    int availableTpmTokens,
    int queueSize,
    long nextRefillTime
) {}
```

---

## 技术选型

| 层级            | 技术栈                       | 说明       |
|---------------|---------------------------|----------|
| **框架**        | Spring Boot 4.x           | 核心框架     |
| **构建工具**      | Gradle (Kotlin DSL)       | 构建和依赖管理  |
| **Agent SDK** | Claude Agent SDK          | 官方 SDK   |
| **数据库**       | H2 (开发) / PostgreSQL (生产) | 事件持久化    |
| **缓存**        | Caffeine                  | 内存缓存     |
| **异步**        | 虚拟线程 (Virtual Threads)    | 高并发处理    |
| **WebSocket** | Spring WebSocket          | 实时推送     |
| **AOP**       | Spring AOP                | 行为记录     |
| **限流**        | Resilience4j RateLimiter  | API 并发控制 |
| **测试**        | JUnit 5 + Mockito         | 单元测试     |

---

## 开发阶段规划

### Phase 1: 基础架构
- [ ] 项目结构搭建
- [ ] 核心接口定义
- [ ] 事件总线原型 (双写)
- [ ] 基础配置管理

### Phase 2: 平台适配层
- [ ] PlatformAdapter 接口实现
- [ ] Telegram 适配器
- [ ] SKILL 文档自动生成
- [ ] Webhook 接收端点

### Phase 3: 安全与权限
- [ ] 用户/用户组模型
- [ ] 权限节点管理
- [ ] 事件签名机制
- [ ] 权限验证切面

### Phase 4: 智能体集成
- [ ] Claude Agent SDK 集成
- [ ] Agent 生命周期管理
- [ ] 授权委托路由
- [ ] API 限流器

### Phase 5: 可观测性
- [ ] AOP 行为记录
- [ ] 日志查询 API
- [ ] 指标收集
- [ ] 健康检查

### Phase 6: 用户界面
- [ ] REST API
- [ ] WebSocket 推送
- [ ] Web Dashboard
- [ ] 配置管理界面

---

## 附录

### 名词解释

| 术语        | 解释                          |
|-----------|-----------------------------|
| **SKILL** | 注入到 Agent 上下文中的工具使用文档       |
| **授权委托**  | 主 Agent 将特定会话的处理权转让给子 Agent |
| **双写**    | 同时写入内存队列和持久化存储              |
| **令牌桶**   | 一种限流算法，以固定速率补充令牌            |

### 参考资源

- [Claude Agent SDK 文档](https://github.com/anthropics/claude-agent-sdk)
- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [Resilience4j 文档](https://resilience4j.readme.io/)
