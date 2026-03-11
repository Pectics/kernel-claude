package me.pectics.kernelclaude.permission;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern JSON_PATTERN = Pattern.compile("\\{\"key\":\"([^\"]*)\",\"value\":\"([^\"]*)\"}");

    /**
     * 检查两个上下文是否匹配
     */
    public boolean matches(@NonNull Context other) {
        if (!this.key.equals(other.key)) return false;

        // 支持通配符值
        if (this.value.equals("*") || other.value.equals("*")) return true;

        return this.value.equals(other.value);
    }

    /**
     * 将 Context 转换为 JSON 字符串
     */
    public @NotNull String toJson() {
        return "{\"key\":\"" + escapeJson(key) + "\",\"value\":\"" + escapeJson(value) + "\"}";
    }

    /**
     * 从 JSON 字符串解析单个 Context
     */
    public static @Nullable Context fromJsonSingle(String json) {
        if (json == null || json.isBlank())
            return null;

        Matcher matcher = JSON_PATTERN.matcher(json);
        if (matcher.matches())
            return new Context(unescapeJson(matcher.group(1)), unescapeJson(matcher.group(2)));

        return null;
    }

    /**
     * 将 Context 集合转换为 JSON 数组字符串
     */
    public static @NotNull String toJson(Set<Context> contexts) {
        if (contexts == null || contexts.isEmpty())
            return "[]";

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Context ctx : contexts) {
            if (!first) sb.append(",");
            sb.append(ctx.toJson());
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 从 JSON 数组字符串解析 Context 集合
     */
    public static @NotNull Set<Context> fromJson(String json) {
        Set<Context> contexts = new HashSet<>();
        if (json == null || json.isBlank() || json.equals("[]"))
            return contexts;

        // 简单解析 JSON 数组
        Matcher matcher = JSON_PATTERN.matcher(json);
        while (matcher.find()) {
            Context ctx = new Context(unescapeJson(matcher.group(1)), unescapeJson(matcher.group(2)));
            contexts.add(ctx);
        }
        return contexts;
    }

    private static @NotNull String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private static @NotNull String unescapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\\"", "\"")
                  .replace("\\\\", "\\")
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t");
    }

}
