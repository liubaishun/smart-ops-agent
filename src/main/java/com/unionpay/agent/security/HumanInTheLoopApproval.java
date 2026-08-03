package com.unionpay.agent.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class HumanInTheLoopApproval {

    private static final Logger log = LoggerFactory.getLogger(HumanInTheLoopApproval.class);

    // 存储审批任务状态（生产环境建议换成 Redis 共享缓存）
    private final Map<String, ApprovalTask> approvalStore = new ConcurrentHashMap<>();

    // 最大等待时间：60 秒（超时自动熔断拒绝）
    private static final long MAX_WAIT_SECONDS = 60;

    /**
     * 核心方法：发起审批并阻塞等待结果
     *
     * @param actionName 动作名称
     * @param details    详细信息
     * @return 是否批准执行
     */
    public boolean requestHumanApproval(String actionName, String details) {
        String approvalId = "APPV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        ApprovalTask task = new ApprovalTask(approvalId, actionName, details, ApprovalTask.ApprovalStatus.PENDING, LocalDateTime.now());

        approvalStore.put(approvalId, task);

        // 1. 推送通知到企业群（飞书/钉钉/工作台）
        sendApprovalNotificationToFeishu(task);

        log.warn("【⚠️ Human-in-the-loop】发起高危动作审批! ApprovalID: [{}], 动作: [{}], 细节: [{}]", approvalId, actionName, details);

        // 2. 轮询等待运维人员审批（同步阻塞当前 Tool 线程）
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < TimeUnit.SECONDS.toMillis(MAX_WAIT_SECONDS)) {
            ApprovalTask currentTask = approvalStore.get(approvalId);

            if (currentTask.getStatus() == ApprovalTask.ApprovalStatus.APPROVED) {
                log.info("【✅ 审批通过】ApprovalID: [{}], 运维已批准执行该动作。", approvalId);
                approvalStore.remove(approvalId); // 清理内存
                return true;
            }

            if (currentTask.getStatus() == ApprovalTask.ApprovalStatus.REJECTED) {
                log.warn("【❌ 审批拒绝】ApprovalID: [{}], 运维人拒绝了该动作！", approvalId);
                approvalStore.remove(approvalId);
                return false;
            }

            try {
                // 每 1 秒轮询一次状态
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        // 3. 超时熔断机制
        log.error("【⏰ 审批超时熔断】ApprovalID: [{}] 在 {} 秒内未响应，默认拒绝执行以保障生产安全！", approvalId, MAX_WAIT_SECONDS);
        task.setStatus(ApprovalTask.ApprovalStatus.TIMEOUT);
        approvalStore.remove(approvalId);
        return false;
    }

    /**
     * 外部 Webhook 调用的回调接口（当运维人员点击飞书/钉钉卡片按钮时触发）
     */
    public boolean handleApprovalCallback(String approvalId, boolean isApproved) {
        ApprovalTask task = approvalStore.get(approvalId);
        if (task == null) {
            log.warn("回调失败：未找到对应的审批单号或已过期，ApprovalID: {}", approvalId);
            return false;
        }

        if (task.getStatus() != ApprovalTask.ApprovalStatus.PENDING) {
            log.warn("回调失败：审批单已被处理，当前状态: {}", task.getStatus());
            return false;
        }

        task.setStatus(isApproved ? ApprovalTask.ApprovalStatus.APPROVED : ApprovalTask.ApprovalStatus.REJECTED);
        log.info("成功更新审批单 [{}] 状态为: {}", approvalId, task.getStatus());
        return true;
    }

    /**
     * 模拟发送飞书/钉钉交互式卡片消息
     */
    private void sendApprovalNotificationToFeishu(ApprovalTask task) {
        log.info("-----------------------------------------------------------------");
        log.info("📩 [已发送消息至运维工作群 Hook]");
        log.info("卡片标题: ⚠️ Agent 高危运维动作确认单");
        log.info("审批单号: {}", task.getApprovalId());
        log.info("请求动作: {}", task.getActionName());
        log.info("动作细节: {}", task.getDetails());
        log.info("审批按钮回调接口: POST /api/v1/approval/callback?approvalId={}&approved=true", task.getApprovalId());
        log.info("-----------------------------------------------------------------");
    }
}