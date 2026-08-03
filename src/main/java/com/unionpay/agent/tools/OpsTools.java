package com.unionpay.agent.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

import java.util.Optional;

/**
 * 运维 Agent 核心工具箱 (Java 8 风格)
 */
public class OpsTools {

    // ================= 场景 1: JVM GC & Heap Dump =================

    @Tool("获取指定微服务的 JVM GC 日志指标概览，用于诊断内存与 GC 频率问题")
    public String fetchJvmGCLog(@P("微服务名称，如 bind-card-core") String serviceName) {
        String service = Optional.ofNullable(serviceName).orElse("unknown-service");
        return "{\n" +
                "  \"service\": \"" + service + "\",\n" +
                "  \"young_gc_count_1h\": 142,\n" +
                "  \"full_gc_count_1h\": 58,\n" +
                "  \"avg_full_gc_pause_ms\": 3500,\n" +
                "  \"old_gen_occupancy_ratio\": \"98.8%\",\n" +
                "  \"status\": \"CRITICAL_FULL_GC_FREQUENT\"\n" +
                "}";
    }

    @Tool("分析最新的 HeapDump 堆转储概览，找出占用内存最高的 Top 对象类")
    public String analyzeHeapDumpSummary(@P("微服务名称") String serviceName) {
        String service = Optional.ofNullable(serviceName).orElse("unknown-service");
        return "{\n" +
                "  \"service\": \"" + service + "\",\n" +
                "  \"top_retained_objects\": [\n" +
                "    {\n" +
                "      \"class\": \"com.bank.card.model.BindCardOrder\",\n" +
                "      \"retained_bytes\": \"3.8 GB\",\n" +
                "      \"percentage\": \"82.4%\",\n" +
                "      \"count\": 1500000,\n" +
                "      \"suspect\": \"SQL query without pagination loading entire table\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"class\": \"byte[]\",\n" +
                "      \"retained_bytes\": \"400 MB\",\n" +
                "      \"percentage\": \"8.6%\",\n" +
                "      \"count\": 12000\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    // ================= 场景 2: Redis Key & 慢查询 =================

    @Tool("检查 Redis 对应 Key 的 TTL 生存时间、类型及占用状态")
    public String inspectRedisKeyStatus(@P("Redis Key 名称，例如 bind_card_lock:user_123") String key) {
        boolean isLockKey = Optional.ofNullable(key)
                .map(k -> k.contains("lock"))
                .orElse(false);

        if (isLockKey) {
            return "{\n" +
                    "  \"key\": \"" + key + "\",\n" +
                    "  \"exists\": true,\n" +
                    "  \"ttl_seconds\": -2,\n" +
                    "  \"holder_thread\": \"bind-service-thread-88\",\n" +
                    "  \"status\": \"LOCK_EXPIRED_BUT_HOLDER_THREAD_STILL_BLOCKED\"\n" +
                    "}";
        }
        return "{\"key\": \"" + key + "\", \"exists\": true, \"ttl_seconds\": 3600}";
    }

    @Tool("获取 Redis 最近的慢查询 SlowLog")
    public String fetchRedisSlowLog(@P("返回慢日志条数") int limit) {
        return "{\n" +
                "  \"slow_logs\": [\n" +
                "    {\n" +
                "      \"command\": \"HGETALL user:orders:all_history\",\n" +
                "      \"execution_time_ms\": 4200,\n" +
                "      \"client_ip\": \"10.0.1.22:45122\",\n" +
                "      \"key_size_estimate\": \"12MB (Big Value)\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }

    // ================= 场景 3: Dubbo / OpenTelemetry 链路诊断 =================

    @Tool("根据 TraceId 查询 SkyWalking/OpenTelemetry 的分布式调用链树")
    public String traceDubboSpan(@P("链路全局 TraceId") String traceId) {
        return "{\n" +
                "  \"trace_id\": \"" + traceId + "\",\n" +
                "  \"spans\": [\n" +
                "    {\"service\": \"api-gateway\", \"duration_ms\": 15002, \"status\": \"200\"},\n" +
                "    {\"service\": \"risk-control\", \"duration_ms\": 10, \"status\": \"200\"},\n" +
                "    {\"service\": \"bind-card-core\", \"duration_ms\": 14980, \"status\": \"TIMEOUT\", \"error\": \"DubboTimeoutException\"}\n" +
                "  ],\n" +
                "  \"bottleneck_service\": \"bind-card-core\"\n" +
                "}";
    }

    @Tool("查询指定微服务内部 Dubbo/Tomcat 线程池的实时指标")
    public String checkThreadPoolStatus(@P("微服务名称") String serviceName) {
        return "{\n" +
                "  \"service\": \"" + serviceName + "\",\n" +
                "  \"thread_pool_name\": \"DubboServerHandler\",\n" +
                "  \"active_threads\": 200,\n" +
                "  \"max_threads\": 200,\n" +
                "  \"queue_capacity\": 1000,\n" +
                "  \"queue_used\": 1000,\n" +
                "  \"status\": \"THREAD_POOL_EXHAUSTED\"\n" +
                "}";
    }

    // ================= 场景 4: Prometheus 指标查询 =================

    @Tool("执行 PromQL 查询 Prometheus 指标数据")
    public String queryPrometheusMetrics(@P("PromQL 语句") String promQl) {
        boolean isCpuMetric = Optional.ofNullable(promQl)
                .map(q -> q.toLowerCase().contains("cpu"))
                .orElse(false);

        if (isCpuMetric) {
            return "{\n" +
                    "  \"query\": \"" + promQl + "\",\n" +
                    "  \"result\": [\n" +
                    "    {\"pod\": \"bind-card-core-7d8f9-x2zp\", \"cpu_usage\": \"99.8%\", \"qps\": 120}\n" +
                    "  ],\n" +
                    "  \"insight\": \"CPU 100% while QPS remains normal. High probability of Regex catastrophic backtracking or infinite loop.\"\n" +
                    "}";
        }
        return "{\"query\": \"" + promQl + "\", \"result\": \"Metric normal.\"}";
    }

    // ================= 场景 5: 三方通道探针网络连通性 =================

    @Tool("对外部/三方 API Gateway 发起 HTTP/TCP 连通性探针测试")
    public String checkHttpEndpointHealth(@P("目标三方网关 URL") String endpointUrl) {
        boolean isCmb = Optional.ofNullable(endpointUrl)
                .map(u -> u.toLowerCase().contains("cmb") || u.contains("招商"))
                .orElse(false);

        if (isCmb) {
            return "{\n" +
                    "  \"target\": \"" + endpointUrl + "\",\n" +
                    "  \"http_status\": 504,\n" +
                    "  \"latency_ms\": 30000,\n" +
                    "  \"packet_loss\": \"52%\",\n" +
                    "  \"status\": \"CHANNEL_NETWORK_FLAPPING_AND_TIMEOUT\"\n" +
                    "}";
        }
        return "{\"target\": \"" + endpointUrl + "\", \"http_status\": 200, \"latency_ms\": 22, \"packet_loss\": \"0%\"}";
    }
}