package com.unionpay.agent.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/approval")
public class ApprovalController {

    @Autowired
    private HumanInTheLoopApproval humanInTheLoopApproval;

    /**
     * 运维人员点击卡片 [批准/拒绝] 后的回调端点
     *
     * 示例 HTTP 请求:
     * POST /api/v1/approval/callback?approvalId=APPV-A1B2C3D4&approved=true
     */
    @PostMapping("/callback")
    public ResponseEntity<String> approvalCallback(
            @RequestParam("approvalId") String approvalId,
            @RequestParam("approved") boolean approved) {

        boolean success = humanInTheLoopApproval.handleApprovalCallback(approvalId, approved);

        if (success) {
            return ResponseEntity.ok("审批结果提交成功！单号: " + approvalId + ", 结果: " + (approved ? "已批准" : "已拒绝"));
        } else {
            return ResponseEntity.badRequest().body("审批处理失败：单号不存在或已超时熔断。");
        }
    }
}