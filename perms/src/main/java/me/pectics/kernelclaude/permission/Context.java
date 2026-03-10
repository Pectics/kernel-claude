package me.pectics.kernelclaude.permission;

import lombok.NonNull;

/**
 * 上下文条件
 * <p>
 * 用于实现"在特定条件下权限才生效"的功能。
 * <p>
 * 示例：<br>
 * {@code platform=telegram} → 权限只在 Telegram 平台生效<br>
 * {@code group=12345} → 权限只在群组 12345 中生效<br>
 * {@code server=production} → 权限只在生产环境生效
 *
 * @param key   上下文键
 * @param value 上下文值
 */
public record Context(String key, String value) {

    /**
     * 检查两个上下文是否匹配
     */
    public boolean matches(@NonNull Context other) {
        if (!this.key.equals(other.key)) return false;

        // 支持通配符值
        if (this.value.equals("*") || other.value.equals("*")) return true;

        return this.value.equals(other.value);
    }

}
