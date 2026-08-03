package com.unionpay.agent.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalTask {
    private String approvalId;       // 唯一审批ID
    private String actionName;       // 动作名称 (如: 清除 Redis 缓存)
    private String details;          // 动作细节
    private ApprovalStatus status;   // 审批状态
    private LocalDateTime createTime;

    public enum ApprovalStatus {
        PENDING,    // 等待审批
        APPROVED,   // 已批准
        REJECTED,   // 已拒绝
        TIMEOUT     // 超时熔断
    }
}