package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.NotNull;

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
     * 获取用户继承的权限组
     */
    @NotNull Set<String> getGroups();

    /**
     * 将用户继承自指定权限组
     *
     * @param groupId 权限组 ID
     */
    void inherit(String groupId);

    /**
     * 将用户取消继承指定权限组
     *
     * @param groupId 权限组 ID
     */
    void uninherit(String groupId);

    /**
     * 检查用户是否继承指定权限组（包括间接继承）
     *
     * @param groupId 要检查的权限组 ID
     * @return 是否继承
     */
    boolean inherits(String groupId);
}
