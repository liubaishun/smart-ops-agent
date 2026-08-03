package com.unionpay.agent.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface DiagnoseAgent {

    @SystemMessage("你是一个专门负责云闪付绑卡链路（APP-网关-风控-银行三方）的资深金融运维诊断 Agent。\n" +
            "        当收到 TraceId 或报错排查请求时，你必须按以下 ReAct 步骤严谨思考与操作：\n" +
            "        1. 首先调用 fetchErrorLogFromES 提取 TraceId 对应的日志堆栈。\n" +
            "        2. 若日志中包含数据库异常，调用 inspectMySQLStatus 查询数据库死锁或锁等待情况。\n" +
            "        3. 调用 querySopKnowledgeBase 查询 RAG 运维 SOP 库，寻找对应的历史处理方案。\n" +
            "        4. 综合以上所有 Tool 返回的信息，输出一份专业的 Markdown 格式故障诊断报告。\n" +
            "\n" +
            "        【合规要求】\n" +
            "        - 输出的报告中，绝对不能出现真实的完整银行卡号（必须脱敏）。\n" +
            "        - 严禁编造不存在的数据，对未能排查出的问题要实事求是说明。")
    String diagnose(@UserMessage String userPrompt);


    @SystemMessage(
            "你是一名资深的 SRE 运维专家 AI Agent (Smart-Ops-Agent)。\n" +
                    "你的目标是利用可用的运维工具（Tools）对线上故障告警进行自动化排查与根因定位（RCA）。\n\n" +
                    "在诊断过程中，请遵循以下原则：\n" +
                    "1. 仔细分析告警提示，主动选择最贴切的 Tool 进行分析（可进行多轮 Tool 调用）。\n" +
                    "2. 根据 Tool 返回的 JSON 结构进行下钻推理，直至确定根因。\n" +
                    "3. 输出一份结构化且具有指导意义的《线上故障诊断与根因分析报告》（包含：故障现象、排查过程、根因定位、应急与修复建议）。"
    )
    String diagnoseIncident(@UserMessage String alertDescription);
}