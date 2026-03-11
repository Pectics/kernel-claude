package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

/**
 * 用户
 * <p>
 * 代表一个权限主体
 */
public interface User extends PermissionHolder {

    /**
     * 获取用户唯一标识
     */
    @NotNull String getId();

    /**
     * 获取平台标识
     */
    @NotNull String getPlatform();

    /**
     * 获取原生用户 ID
     */
    @NotNull String getNativeId();

    /**
     * 获取显示名称
     */
    @NotNull String getDisplayName();

    /**
     * 设置显示名称
     *
     * @param displayName 显示名称
     */
    void setDisplayName(String displayName);

    /**
     * 生成用户唯一标识
     */
    static @NotNull String generateId(String platform, String nativeId) {
        String input = "?platform=" + platform + "&native_id=" + nativeId;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (byte b : digest)
                sb.append(String.format("%02x", b));

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // MD5 是 Java 标准算法，理论上不会抛出此异常
            throw new RuntimeException("Never happens: MD5 algorithm not found", e);
        }
    }

}
