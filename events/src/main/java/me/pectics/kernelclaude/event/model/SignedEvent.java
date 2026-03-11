package me.pectics.kernelclaude.event.model;

import java.util.Collections;
import java.util.Set;

/**
 * 签名事件模型
 * <p>
 * 包含权限信息和数字签名的事件，
 * 用于防止提示词注入攻击。
 * <p>
 * 核心安全机制：
 * 1. 权限在事件入队时就确定，不可被篡改
 * 2. Agent 只能执行签名中授权的操作
 * 3. 签名验证失败则拒绝执行
 *
 * @param event       原始事件
 * @param permissions 授予的权限节点集合
 * @param signature   事件签名（HMAC-SHA256）
 * @param signedAt    签名时间戳
 */
public record SignedEvent(
        KernelEvent event,
        Set<String> permissions,
        String signature,
        long signedAt
) {

    /**
     * 检查是否拥有指定权限
     * <p>
     * 支持通配符匹配：
     * - "telegram.message.send" 匹配精确权限
     * - "telegram.message.*" 匹配前缀权限
     * - "*" 匹配所有权限（超级管理员）
     *
     * @param permission 要检查的权限节点
     * @return 是否拥有该权限
     */
    public boolean hasPermission(String permission) {
        if (permissions == null || permissions.isEmpty()) {
            return false;
        }

        // 精确匹配
        if (permissions.contains(permission)) {
            return true;
        }

        // 通配符匹配
        String[] parts = permission.split("\\.");
        StringBuilder prefix = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            prefix.append(parts[i]).append(".");
            if (permissions.contains(prefix + "*")) {
                return true;
            }
        }

        // 超级权限
        return permissions.contains("*");
    }

    /**
     * 获取只读的权限集合
     */
    public Set<String> getPermissions() {
        return permissions != null ? Collections.unmodifiableSet(permissions) : Set.of();
    }

    /**
     * 获取事件ID（便捷方法）
     */
    public String getEventId() {
        return event != null ? event.eventId() : null;
    }

    /**
     * 获取会话ID（便捷方法）
     */
    public String getSessionId() {
        return event != null ? event.sessionId() : null;
    }
}
