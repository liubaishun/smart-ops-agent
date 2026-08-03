package com.unionpay.agent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 智能运维 Agent 并行诊断专属线程池配置 (修复 IllegalArgumentException)
 */
@Configuration
public class ThreadPoolConfig {

    @Bean(name = "agentDiagnoseExecutor")
    public ExecutorService agentDiagnoseExecutor() {
        int cpuCores = Runtime.getRuntime().availableProcessors();

        // 核心线程数
        int corePoolSize = Math.max(2, cpuCores);
        // 最大线程数：务必保证 maximumPoolSize >= corePoolSize
        int maxPoolSize = corePoolSize * 2;
        long keepAliveTime = 60L;

        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                runnable -> {
                    Thread thread = new Thread(runnable);
                    thread.setName("ops-agent-runner-" + thread.getId());
                    thread.setDaemon(true);
                    return thread;
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：由调用者线程处理
        );
    }
}