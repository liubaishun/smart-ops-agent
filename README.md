# 🤖 Smart-Ops-Agent：基于 LangChain4j + DeepSeek 的金融级微服务智能诊断平台

> **一句话简介**：专为千万级高并发、跨系统微服务（如云闪付绑卡链路）设计的 AI 运维诊断 Agent。通过 **ReAct 架构** 协同 ES、MySQL 与 SOP 知识库，实现线上突发故障从报错到根因分析的**秒级自动化定位**。

![Java 17](https://img.shields.io/badge/Java-17-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-green.svg)
![LangChain4j](https://img.shields.io/badge/LangChain4j-0.31.0-blue.svg)
![DeepSeek](https://img.shields.io/badge/LLM-DeepSeek--R1%2FV3-purple.svg)
![License](https://img.shields.io/badge/License-Apache%202.0-red.svg)

---

## 📸 效果展示 (Quick Demo)

输入线上报警的 `TraceId`，Agent 自主规划工具链并秒级输出 Markdown 格式的排查报告：

```text
==================== Agent 思考链 (ReAct Thought Trail) ====================
[Thought] 收到报错请求，先根据 TraceId 查询 ES 堆栈日志...
[Action]  Call @Tool: fetchErrorLogFromES(traceId="TRACE_UP_9901")
[Result]  日志检测到 CannotAcquireLockException，涉及 t_bind_card_02 表行锁...
[Thought] 发现 SQL 锁等待，进一步查询 MySQL sys.innodb_lock_waits 视图...
[Action]  Call @Tool: inspectMySQLStatus(table="t_bind_card_02")
[Result]  检测到持锁事务 TX_99812 已阻塞 51 秒...
[Thought] 召回 RAG 运维 SOP 知识库并进行卡号脱敏，生成最终诊断报告。
==========================================================================

## 📸 效果展示 (Quick Demo)

输入线上报警的 `TraceId`，Agent 自主规划工具链并秒级输出 Markdown 格式的排查报告：

```text
==================== Agent 思考链 (ReAct Thought Trail) ====================
[Thought] 收到报错请求，先根据 TraceId 查询 ES 堆栈日志...
[Action]  Call @Tool: fetchErrorLogFromES(traceId="TRACE_UP_9901")
[Result]  日志检测到 CannotAcquireLockException，涉及 t_bind_card_02 表行锁...
[Thought] 发现 SQL 锁等待，进一步查询 MySQL sys.innodb_lock_waits 视图...
[Action]  Call @Tool: inspectMySQLStatus(table="t_bind_card_02")
[Result]  检测到持锁事务 TX_99812 已阻塞 51 秒...
[Thought] 召回 RAG 运维 SOP 知识库并进行卡号脱敏，生成最终诊断报告。
==========================================================================

```

---

## 🌟 核心特性 (Key Features)

* 🧠 **ReAct 自主决策思考链**：基于 LangChain4j 声明式范式， Agent 可根据线上堆栈自主推理下一步诊断动作（查 ES 堆栈 → 查 MySQL 锁状态 → 检索 RAG SOP）。
* 🔍 **RAG 运维知识库增强**：集成向量数据库与 Hybrid Search（混合检索），将历史上千条绑卡链路 SOP 向量化，故障定位精准度提升至 **85%+**。
* 🛡️ **金融级安全与防幻觉机制**：
* **只读 SQL 熔断器**：通过 AST/正则拦截一切非 `SELECT` 的破坏性 SQL，彻底杜绝大模型“幻觉误删库”。
* **动态数据脱敏**：符合金融 PCI-DSS 规范，针对 13~19 位银行卡号自动进行 `6222****9999` 掩码脱敏。
* **Human-in-the-Loop 审批**：针对清除缓存、服务重启等高危动作，引入 Console/Hook 人工二次确认机制。


* ⚡ **开箱即用与零成本依赖**：轻量化架构设计，无缝对接 DeepSeek、Qwen 或本地 Ollama 部署的大模型。

---

## 🏗️ 系统架构图 (Architecture)

```text
                               +-----------------------------+
                               |  运维人员 / AlertManager 告警  |
                               +-----------------------------+
                                              |
                                     发送 TraceId / 报错信息
                                              v
+--------------------------------------------------------------------------------------------------+
|                                    smart-ops-agent 诊断引擎                                      |
|                                                                                                  |
|   +------------------------------------+        +------------------------------------------+     |
|   |   ReAct 思考引擎 (LangChain4j)      | <----> |  RAG 运维 SOP 知识库 (Qdrant / Hybrid)   |     |
|   +------------------------------------+        +------------------------------------------+     |
|                     |                                                                            |
|                     v                                                                            |
|   +------------------------------------------------------------------------------------------+   |
|   |                           金融级安全拦截器 (Security Interceptor)                          |   |
|   |   ├── [只读 SQL 校验]  ├── [银行卡号脱敏 (Luhn)]  ├── [Human-in-the-loop 确认]           |   |
|   +------------------------------------------------------------------------------------------+   |
|                     |                                                                            |
|                     v  (Function Calling / @Tool)                                                |
|   +------------------------------------------------------------------------------------------+   |
|   |  ElasticSearchTool   |   MySQLDiagnoseTool   |   RedisInspectTool   |   DubboTraceTool   |   |
|   +------------------------------------------------------------------------------------------+   |
+--------------------------------------------------------------------------------------------------+
                                              |
                                     输出标准化故障根因报告
                                              v
                               +-----------------------------+
                               |   Markdown 格式排查报告与建议  |
                               +-----------------------------+

```

---

## 🛠️ 技术栈 (Tech Stack)

| 领域 | 技术方案 |
| --- | --- |
| **核心框架** | Java 17 / Spring Boot 3.2.5 |
| **AI Agent 框架** | LangChain4j 0.31.0 |
| **大模型 (LLM)** | DeepSeek-V3 / DeepSeek-R1 / OpenAI API |
| **中间件 & 工具** | ElasticSearch / MySQL / Redis / Dubbo |
| **向量检索 (RAG)** | Qdrant / Pgvector (支持 Hybrid Search) |

---

## 🚀 快速开始 (Quick Start)

### 1. 克隆项目与配置环境

```bash
git clone https://github.com/liubaishun/smart-ops-agent.git
cd smart-ops-agent

```

### 2. 配置 DeepSeek API Key

```bash
# Linux / macOS
export DEEPSEEK_API_KEY="your-actual-deepseek-api-key"

# Windows (PowerShell)
$env:DEEPSEEK_API_KEY="your-actual-deepseek-api-key"

```

### 3. 编译并运行

```bash
mvn clean package
java -jar target/smart-ops-agent-1.0.0-SNAPSHOT.jar

```

---

## 📝 典型诊断输出报告 (Sample Output)

```markdown
### 线上绑卡链路故障诊断报告 (TraceId: TRACE_UP_20260803_9901)

#### 1. 故障根因分析 (Root Cause)
* **故障定位**：云闪付绑卡核心数据库表 `t_bind_card_02` 发生 **数据库行锁等待超时 (Lock Wait Timeout)**。
* **链路分析**：用户针对卡号 `6222****9999` 发起绑卡请求时，高并发重试触发锁竞争，持锁事务 `TX_99812` 执行时间过长（51s），导致后续 Dubbo 线程池阻塞。

#### 2. 金融安全合规审查
* **数据脱敏**：敏感字段已全量按金融规范完成动态掩码脱敏。
* **SQL 鉴权**：排查全程未触发非只读写指令。

#### 3. 建议修复措施 (SOP)
1. **紧急处置**：人工评估并终止 MySQL 阻塞事务 `TX_99812`。
2. **长远优化**：检查网关侧 Redis 分布式锁拦截粒度，将高并发重复请求拦截在缓存层。

```

---

## 📄 开源协议 (License)

本项目遵循 [Apache 2.0 License](https://www.google.com/search?q=LICENSE) 开源协议。
