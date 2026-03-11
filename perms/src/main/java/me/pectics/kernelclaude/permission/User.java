package me.pectics.kernelclaude.permission;

import java.util.Set;

/**
 * 用户
 * <p>
 * 代表一个权限主体
 */
public interface User extends PermissionHolder {

    /**
     * 获取用户唯一标识（复合标识）
     */
    String getId();

    /**
     * 获取平台标识
     */
    String getPlatformId();

    /**
     * 获取平台唯一标识
     */
    String getPlatformUserId();

    /**
     * 获取显示名称
     */
    String getDisplayName();

    /**
     * 设置显示名称
     *
     * @param displayName 显示名称
     */
    void setDisplayName(String displayName);

    /**
     * 获取用户继承的权限组
     */
    Set<String> getGroups();

    /**
     * 将用户继承自指定权限组
     *
     * @param groupName 组名
     */
    void inherit(String groupName);

    /**
     * 将用户取消继承指定权限组
     *
     * @param groupName 组名
     */
    void uninherit(String groupName);

    /**
     * 检查用户是否继承指定权限组（包括间接继承）
     *
     * @param groupName 组名
     * @return 是否继承
     */
    boolean inherits(String groupName);
}
