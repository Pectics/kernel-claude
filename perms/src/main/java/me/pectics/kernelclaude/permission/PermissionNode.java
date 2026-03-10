package me.pectics.kernelclaude.permission;

import lombok.NonNull;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.Contract;

import java.util.OptionalLong;
import java.util.Set;

/**
 * 权限节点
 * <p>
 * 核心抽象：一切皆节点。<br>
 * - 权限 (Permission)<br>
 * - 继承关系 (Inheritance)<br>
 * - 元数据 (Metadata)
 * <p>
 * 设计参考：LuckPerms Node System
 *
 * @param key      节点键，如 "telegram.message.send"
 * @param value    节点值，true=允许，false=拒绝
 * @param contexts 上下文条件，如 platform=telegram
 * @param until    过期时间（Unix 时间戳），0 表示永不过期
 */
public record PermissionNode(
        String key,
        boolean value,
        Set<Context> contexts,
        long until
) {

    // 权限键格式：小写字母、数字、下划线，分段由点分隔，支持末尾通配符
    private static final @RegExp String KEY_PATTERN = "^[a-z0-9_]+(?:\\.[a-z0-9_]+)*(\\.\\*)?$";

    /**
     * 创建一个权限节点
     */
    @Contract("_, _ -> new")
    public static @NonNull PermissionNode of(@NonNull String key, boolean value) {
        if (!key.matches(KEY_PATTERN))
            throw new IllegalArgumentException("Invalid permission key: " + key);
        return new PermissionNode(key, value, Set.of(), 0L);
    }

    /**
     * 创建允许权限
     */
    @Contract("_ -> new")
    public static @NonNull PermissionNode allow(@NonNull String key) {
        return of(key, true);
    }

    /**
     * 创建拒绝权限
     */
    @Contract("_ -> new")
    public static @NonNull PermissionNode deny(@NonNull String key) {
        return of(key, false);
    }

    /**
     * 检查是否已过期
     */
    public boolean isExpired() {
        if (until == 0) return false;
        return System.currentTimeMillis() > until;
    }

    /**
     * 检查是否在指定上下文中生效
     *
     * @param contexts 查询上下文
     * @return 是否匹配
     */
    public boolean matchesContext(Set<Context> contexts) {
        // 无上下文限制，始终生效
        if (this.contexts.isEmpty()) return true;

        // 检查所有必需的上下文是否满足
        for (Context required : this.contexts) {
            boolean found = false;
            for (Context query : contexts) {
                if (required.matches(query)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        return true;
    }

    /**
     * 获取过期时间的 Optional
     */
    public OptionalLong getUntil() {
        return until == 0 ? OptionalLong.empty() : OptionalLong.of(until);
    }

    /**
     * 通配符匹配
     * <p>
     * {@code telegram.message.send} → 精确匹配<br>
     * {@code telegram.message.*} → 匹配 {@code telegram.message} 下所有<br>
     * {@code telegram.*} → 匹配 {@code telegram} 下所有<br>
     * {@code *} → 匹配所有
     */
    public boolean matchesKey(String queryKey) {
        if (key.equals(queryKey)) return true;
        if (key.equals("*")) return true;

        // 通配符匹配
        if (key.endsWith("*")) {
            String prefix = key.substring(0, key.length() - 1);
            return queryKey.startsWith(prefix);
        }

        return false;
    }
}
