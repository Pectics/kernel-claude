# core 核心模块架构设计

## 设计原则

- core 模块不依赖 Spring，保持纯 Java 实现。
- Spring 仅在最外层的 app 模块中用于组装各子模块。
- Event（事件）和 Message（消息）是两个独立的概念，职责明确分离。

## 概念定义

### Event（事件）

事件是开发角度的、软件内部的通信机制。用于模块间解耦和插件扩展。

事件本身不携带业务数据流转的职责，它是一种通知——告知系统中发生了某件事。
消息生命周期的关键节点会产生对应的事件（如消息接收、路由完成、处理失败等）。

### Message（消息）

消息是数据角度的、在运行时由上游模块产生的业务载体。
消息需要被程序处理并返回内容，是本项目的一等公民。

消息由外部来源（如 social 模块对接的 Discord、Telegram 等）产生，
经过权限校验后进入消息队列，最终被路由到 agents 模块处理。

## 事件系统

### 核心组件

- **EventBus**：事件总线，维护 handler 注册表，按优先级分发事件。纯 Java 实现，不依赖 Spring。
- **EventListener**：空接口，作为事件监听器的标记。只有实现了 EventListener 的类中被 `@EventHandler` 注解标记的方法才能被注册为处理器。
- **@EventHandler**：方法级注解，标记事件处理方法。支持优先级属性。

### 注册方式

1. 实现 EventListener 接口，在方法上标注 `@EventHandler`，由 EventBus 扫描注册。
2. 以 lambda Consumer 的形式直接向 EventBus 注册匿名处理器。

### 分发机制

- 事件按处理器的优先级顺序依次分发。
- EventBus 通过反射调用 `@EventHandler` 标记的方法。

### 与 Spring 的桥接（可选）

在 app 模块中，可通过 `SpringEventBridgeListener` 将内部事件转发到 Spring 的 `ApplicationEventPublisher`，
实现与 Spring 组件的单向互通。此桥接是可选的，不影响 core 模块的独立性。

## 消息系统

### 核心组件

- **MessageManager**：消息的入口，负责接收来自其他模块的消息并放入队列。
- **MessageRouter**（接口）：定义在 core 模块中，由 agents 模块提供实现并注入。负责将消息路由到对应的处理逻辑。

### 数据流转

1. 消息由上游模块（如 social 模块）产生，通过 MessageManager 传入。
2. 消息在进入队列前经过 perms 模块的权限校验，携带权限细节信息。
3. 消息以优先级队列（`PriorityBlockingQueue`）的形式存储。
4. 消息从队列中取出后，流入 MessageRouter 进行路由和处理。

### 权限校验失败的处理

权限校验失败时，消息不进入队列，而是产生一个权限校验失败的 Event。
开发者可通过事件系统监听该事件，自行决定失败后的处理逻辑（如回复提示、记录日志等）。

### 关于消息量级

本项目的典型应用场景中，消息量级在千级别，内存中的 `PriorityBlockingQueue` 完全满足需求。
不预留外部消息队列（如 RabbitMQ）的抽象层，避免过度设计。如未来有需求，届时再抽象。
