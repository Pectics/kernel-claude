### 🤖 Agents 模块架构设计梳理

#### 1. 入口与路由层 (Task Router)
所有来自社交媒体平台或管理终端的原始输入（Input），统一进入 **Task Router**，分为两层处理：

*   **第一层：硬路由 (Hard Routing)**
    *   **权限检查**：检查 Input 是否有直接连接子 Agent 的权限。
    *   **任务匹配**：检查 Input 所属的 Task ID 是否已分配给某个子 Agent。
    *   **决策**：
        *   ✅ 满足条件：Input 直接进入子 Agent（ bypass 软路由，降低延迟）。
        *   ❌ 不满足：进入下一层软路由。
*   **第二层：软路由 (Soft Router Agent)**
    *   **实现方式**：基于大模型 Agent，通过 **MCP 或 Skills** 注入 SDK 接口实现，无需复杂提示词。
    *   **核心能力**：语义理解 Input，在已有 Task 中匹配所属任务。
    *   **决策**：
        *   找到匹配 Task：将 Input 分发到对应 Task（若匹配多个则拆分）。
        *   无匹配：创建新任务流程。

#### 2. 任务层 (Task Layer)
经过路由后的 Task 进入抽象的 **Task Layer** 进行排布，主要包含两种形式：

*   **优先级队列 (Priority Queue)**：
    *   基于任务属性（如紧急程度）排序。
    *   等级相同则按输入顺序处理。
*   **任务池 (Task Pool)**：
    *   存放定时任务或周期任务（Scheduled Task）。

#### 3. 执行与通信层 (Task Executor & Communication)
*   **Task Executor**：
    *   **并发控制**：数量有限，取决于配置文件。
    *   **工作流程**：空闲时从 Priority Queue 取任务 -> 处理 -> 触发 **Finished 回调**。
    *   **事件反馈**：回调将结果打包成事件，进入 **事件总线 (Event Bus)**，回传至顶层。
*   **Agent 间通信与逻辑**：
    *   **实现方式**：**Soft Router Agent** 与 **Task Executor Agent** 的理解、任务分配、回调、通信等逻辑，均通过 **MCP 或 Skills** 直接注入 SDK 提供的接口。
    *   **优势**：无需设计复杂的提示词工程，标准化调用，降低维护成本。

#### 4. 任务管理类 (Task Manager)
*   **定位调整**：不再作为所有用户消息的必经之路，仅作为 **代码层面管理 Task 生命周期的类**。
*   **核心职能**：
    *   自主创建、归档、反归档、删除任务。
    *   响应用户查询（如查询进度）时，通过调用工具与子 Agent 通信，获取进度报告后回复用户。
*   **权限移交机制**：
    *   当 Task Manager 将原始输入分配到新任务后，若判定无需再经手，可通过 **权限移交**，让后续输入经硬路由直接对接子 Agent，避免 Task Manager 成为瓶颈。