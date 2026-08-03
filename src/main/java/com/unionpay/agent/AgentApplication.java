package com.unionpay.agent;

import com.unionpay.agent.service.DiagnoseAgent;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }

    @Bean
    CommandLineRunner startDiagnosticTest(DiagnoseAgent diagnoseAgent) {
        return args -> {
            System.out.println("=================================================================");
            System.out.println("🚀 云闪付智能日志诊断与运维 Agent 平台启动成功！");
            System.out.println("=================================================================\n");

            // 模拟线上报警系统自动发起的诊断指令
            String traceId = "TRACE_UP_20260803_9901";
            String prompt = "告警系统监测到绑卡微服务抛异常，请立即帮我排查 TraceId 为 " + traceId + " 的故障根因！";

            System.out.println("收到诊断指令: " + prompt + "\n");
            System.out.println("🤖 Agent 开始 ReAct 自主思考与排查...\n");

            // 触发 Agent 诊断流程
            String report = diagnoseAgent.diagnose(prompt);

            System.out.println("\n=================================================================");
            System.out.println("📊 Agent 最终生成的故障诊断报告：");
            System.out.println("=================================================================");
            System.out.println(report);
        };
    }
}