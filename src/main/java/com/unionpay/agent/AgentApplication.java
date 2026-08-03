package com.unionpay.agent;

import com.unionpay.agent.service.DiagnoseAgent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@SpringBootApplication
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }


    @Bean
    CommandLineRunner startDiagnosticTest(DiagnoseAgent diagnoseAgent, @Qualifier("agentDiagnoseExecutor") ExecutorService agentDiagnoseExecutor) {

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
            String report1 = diagnoseAgent.diagnose(prompt);

            System.out.println("\n=================================================================");
            System.out.println("📊 Agent 最终生成的故障诊断报告：");
            System.out.println("=================================================================");
            System.out.println(report1);


            // 1. 构造云闪付 5 大线上真实运维故障场景队列 (Java 8 风格)
            List<IncidentAlert> alertQueue = Arrays.asList(new IncidentAlert("场景 1: JVM OOM 溢出", "【告警】绑卡微服务 bind-card-core 突发 OOM (OutOfMemoryError) 且响应卡死，请立即分析 JVM 和 HeapDump 根因！"), new IncidentAlert("场景 2: Redis 锁与慢查询", "【告警】绑卡接口响应超时，日志显示大量线程卡在获取 Redis 锁 bind_card_lock:user_123，请排查！"), new IncidentAlert("场景 3: Dubbo 分布式链路雪崩", "【告警】告警系统监测到绑卡微服务抛异常，请立即帮我排查 TraceId 为 TRACE_UP_20260803_9901 的故障根因！"), new IncidentAlert("场景 4: Pod CPU 100% 突增", "【告警】Pod bind-card-core-7d8f9-x2zp CPU 占用率达到 99.8%，请结合 Prometheus 指标定位异常！"), new IncidentAlert("场景 5: 三方招行专线网络抖动", "【告警】批量用户反馈招商银行（CMB）绑卡失败，本地系统正常，请排查外部通道健康度！"));

            long startTime = System.currentTimeMillis();

            System.out.println("⚡ [Java 8 CompletableFuture] 开始使用线程池并发并发排查 " + alertQueue.size() + " 大运维场景...\n");

            // 2. 使用 Java 8 CompletableFuture + Stream 提交并行诊断任务
            List<CompletableFuture<DiagnoseResult>> futureList = alertQueue.stream().map(alert -> CompletableFuture.supplyAsync(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.println("➔ [" + threadName + "] 开始诊断场景: " + alert.getSceneName());

                // 触发 Agent 诊断流程（内部包含多轮 ReAct 工具调用）
                String report = diagnoseAgent.diagnose(alert.getPrompt());

                return new DiagnoseResult(alert.getSceneName(), report, threadName);
            }, agentDiagnoseExecutor)).collect(Collectors.toList());

            // 3. CompletableFuture.allOf 组装所有异步任务，等待全部执行完成
            CompletableFuture<Void> allOfFuture = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));

            // 4. 当所有场景并行诊断结束后，统一提取并汇总报告
            allOfFuture.thenRun(() -> {
                long totalTime = System.currentTimeMillis() - startTime;
                System.out.println("\n=================================================================");
                System.out.println("📊 所有告警场景并发排查完成！总耗时: " + totalTime + " ms");
                System.out.println("=================================================================\n");

                // 使用 Stream API 汇总打印每个场景的诊断报告
                futureList.stream().map(CompletableFuture::join).forEach(result -> {
                    System.out.println("-----------------------------------------------------------------");
                    System.out.println("📌 [" + result.getSceneName() + "] 诊断报告 (处理线程: " + result.getExecutionThread() + "):");
                    System.out.println("-----------------------------------------------------------------");
                    System.out.println(result.getReport());
                    System.out.println();
                });

                System.out.println("=================================================================");
                System.out.println("✅ 云闪付智能运维 Agent 自动化排查流水线执行完毕！");
                System.out.println("=================================================================");
            }).join(); // 阻塞主线程直到完整的异步链路打印完毕
        };
    }

    /**
     * 告警输入实体 (Java 8 风格 POJO)
     */
    static class IncidentAlert {
        private final String sceneName;
        private final String prompt;

        public IncidentAlert(String sceneName, String prompt) {
            this.sceneName = sceneName;
            this.prompt = prompt;
        }

        public String getSceneName() {
            return sceneName;
        }

        public String getPrompt() {
            return prompt;
        }
    }

    /**
     * Agent 诊断结果封装类
     */
    static class DiagnoseResult {
        private final String sceneName;
        private final String report;
        private final String executionThread;

        public DiagnoseResult(String sceneName, String report, String executionThread) {
            this.sceneName = sceneName;
            this.report = report;
            this.executionThread = executionThread;
        }

        public String getSceneName() {
            return sceneName;
        }

        public String getReport() {
            return report;
        }

        public String getExecutionThread() {
            return executionThread;
        }
    }

}


/**
 *
 *
 * =================================================================
 * 🚀 云闪付智能日志诊断与运维 Agent 平台启动成功！
 * =================================================================
 * <p>
 * ⚡ [Java 8 CompletableFuture] 开始使用线程池并发并发排查 5 大运维场景...
 * <p>
 * ➔ [ops-agent-runner-1] 开始诊断场景: 场景 1: JVM OOM 溢出
 * ➔ [ops-agent-runner-2] 开始诊断场景: 场景 2: Redis 锁与慢查询
 * ➔ [ops-agent-runner-3] 开始诊断场景: 场景 3: Dubbo 分布式链路雪崩
 * ➔ [ops-agent-runner-4] 开始诊断场景: 场景 4: Pod CPU 100% 突增
 * ➔ [ops-agent-runner-5] 开始诊断场景: 场景 5: 三方招行专线网络抖动
 * <p>
 * =================================================================
 * 📊 所有告警场景并发排查完成！总耗时: 3240 ms
 * =================================================================
 * <p>
 * -----------------------------------------------------------------
 * 📌 [场景 1: JVM OOM 溢出] 诊断报告 (处理线程: ops-agent-runner-1):
 * -----------------------------------------------------------------
 * ### 🔍 诊断报告
 * - **根因**：分析发现 `BindCardOrder` 大对象大量积压（占用 82.4% 堆内存），大表未分页查询引发频繁 Full GC 并导致 OOM。
 * ...
 * <p>
 * -----------------------------------------------------------------
 * 📌 [场景 3: Dubbo 分布式链路雪崩] 诊断报告 (处理线程: ops-agent-runner-3):
 * -----------------------------------------------------------------
 * ### 🔍 诊断报告
 * - **根因**：TraceId `TRACE_UP_20260803_9901` 链路显示 `bind-card-core` 响应超时，Dubbo 线程池已被完全打满 (200/200)。
 * ...
 * <p>
 * =================================================================
 * ✅ 云闪付智能运维 Agent 自动化排查流水线执行完毕！
 * =================================================================
 */