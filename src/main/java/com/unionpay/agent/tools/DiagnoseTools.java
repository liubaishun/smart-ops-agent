package com.unionpay.agent.tools;

import com.unionpay.agent.security.HumanInTheLoopApproval;
import com.unionpay.agent.security.SecurityInterceptor;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiagnoseTools {

    @Autowired
    private SecurityInterceptor securityInterceptor;

    @Autowired
    private HumanInTheLoopApproval approval;

    @Tool("根据 TraceId 从 ElasticSearch 集群提取绑卡微服务链路的详细报错日志")
    public String fetchErrorLogFromES(@P("链路调用的 TraceId") String traceId) {
        // 模拟 ES 查询：返回包含死锁超时的异常日志
        String mockLog = "[ERROR] 2026-08-03 14:20:10.123 [dubbo-provider-thread-8] c.u.bindcard.service.BankService - "
                + "Card No: 6222021000088889999 bind failed. Cause: org.springframework.dao.CannotAcquireLockException: "
                + "Lock wait timeout exceeded; try restarting transaction; SQL: SELECT * FROM t_bind_card_02 WHERE card_no = '6222021000088889999' FOR UPDATE";

        // 数据自动脱敏处理
        return securityInterceptor.sanitizeSensitiveData(mockLog);
    }

    @Tool("分析 MySQL 数据库状态，检查指定 SQL 或数据表是否存在死锁与慢锁等待")
    public String inspectMySQLStatus(@P("排查分析所用的 SQL 语句或表名") String sqlOrTable) {
        // 安全拦截校验：防 SQL 注入与非只读动作
        securityInterceptor.validateReadOnlySql(sqlOrTable);

        // 模拟查询 MySQL sys.innodb_lock_waits 视图
        return "【MySQL 诊断数据】数据表 t_bind_card_02 当前存在 1 个事务正处于 LOCK WAIT 状态；持锁事务 ID: TX_99812，等待时长 51s；无未提交的长事务。";
    }

    @Tool("查询向量数据库（RAG 架构），召回绑卡链路历史 SOP 故障排查手册与解决方案")
    public String querySopKnowledgeBase(@P("故障特征关键字") String keyword) {
        // 模拟 RAG 向量混合检索（Hybrid Search）结果召回
        return "【SOP 知识库匹配结果 (相似度 0.92)】\n"
                + "故障场景：绑卡表行锁超时 (CannotAcquireLockException)。\n"
                + "触发根因：高并发大促期间同一卡号重复请求并发击穿幂等层，引发 Mysql 行级锁竞争。\n"
                + "处理建议：1. 评估释放持锁事务 TX_99812； 2. 检查 Redis 防刷分布式锁粒度与重试机制。";
    }

    @Tool("清理指定的 Redis 阻塞 Key 缓存（高危动作）")
    public String clearRedisCacheKey(@P("需要清理的 Redis Key 名称") String keyName) {
        // 触发 Human-in-the-loop 人工审批
        boolean approved = approval.requestHumanApproval("清除 Redis 缓存 Key", "Key 名: " + keyName);
        if (!approved) {
            return "【执行结果】运维人工二次审批拒绝，操作已取消。";
        }
        return "【执行结果】已成功清除 Redis 缓存 Key: " + keyName;
    }
}