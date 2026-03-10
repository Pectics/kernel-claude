# 项目完整架构设计

## 项目开发背景

> Inspired by Agents like OpenClaw, nanobot, Claude Code, Codex, etc.

### 当前产品的主要问题

- **流程管理缺乏专业性**：OpenClaw 的设计理念主要在于解决传统 AI Agent 与社交媒体的通信问题；nanobot 的初衷是从头设计一个精简而小巧的 Agent Loop。但该系列产品在核心提示词设计上打磨不够精细，缺乏专业性，导致在实际应用中效果不佳。
- **任务目标不匹配**：Claude Code, Codex 等产品在任务目标上主要面向编程、代码、开发等领域，对核心的任务和流程的管理、工具的调用具有强专业性，其设计理念中的产品定位主要在“称手的开发工具”上。但该系列产品无法完成类似 Agent Loop 的持续部署，无法实现类似 OpenClaw 系列软件的“Always online”效果。

### 一次实验性尝试（on Claude Code）

#### 实验背景

**我注意到 Claude Code 的一个关键设计：**

在模型的工具调用完成后，Claude Code 会继续请求模型对工具调用的结果进行总结或输出。

#### 实验方式

使用提示词工程，修改 `CLAUDE.md` 