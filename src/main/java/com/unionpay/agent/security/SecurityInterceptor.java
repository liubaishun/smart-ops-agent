package com.unionpay.agent.security;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SecurityInterceptor {

    // 匹配 13-19 位中国大陆银行卡号
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("\\b(\\d{4})\\d{5,11}(\\d{4})\\b");

    // 高危 SQL 写操作正则拦截匹配（只允许纯 SELECT 诊断）
    private static final Pattern DANGEROUS_SQL_PATTERN = Pattern.compile("(?i)\\b(ALTER|DROP|TRUNCATE|DELETE|UPDATE|INSERT|GRANT|REVOKE)\\b");

    /**
     * SQL 安全检查：强制只读权限校验
     */
    public void validateReadOnlySql(String sql) {
        if (sql == null || sql.isEmpty()) {
            return;
        }
        if (DANGEROUS_SQL_PATTERN.matcher(sql).find()) {
            throw new SecurityException("【金融安全熔断】Agent 试图触发写 SQL 操作，已被阻断！SQL内容: " + sql);
        }
    }

    /**
     * 脱敏工具：将日志中的真实银行卡号脱敏为 6222****9999 模式
     */
    public String sanitizeSensitiveData(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return BANK_CARD_PATTERN.matcher(text).replaceAll("$1****$2");
    }
}