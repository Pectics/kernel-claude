package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * 用户-权限组关系管理器
 */
public interface UserGroupManager {

    /**
     * 获取用户所在的权限组
     *
     * @param userId 用户 ID
     * @return 用户所在的权限组集合
     */
    @NotNull Set<Group> getUserGroups(@NotNull String userId);

    /**
     * 获取用户所在的权限组
     *
     * @param platform 平台标识
     * @param nativeId 原生用户 ID
     * @return 用户所在的权限组集合
     */
    default @NotNull Set<Group> getUserGroups(@NotNull String platform, @NotNull String nativeId) {
        return getUserGroups(User.calculateId(platform, nativeId));
    }

    /**
     * 为权限组添加用户
     *
     * @param groupId 权限组 ID
     * @param userId  用户 ID
     */
    void addGroupUser(@NotNull String groupId, @NotNull String userId);

    /**
     * 为权限组添加用户
     *
     * @param groupId  权限组 ID
     * @param platform 平台标识
     * @param nativeId 原生用户 ID
     */
    default void addGroupUser(@NotNull String groupId, @NotNull String platform, @NotNull String nativeId) {
        addGroupUser(groupId, User.calculateId(platform, nativeId));
    }

    /**
     * 为权限组移除用户
     *
     * @param groupId 权限组 ID
     * @param userId  用户 ID
     */
    void removeGroupUser(@NotNull String groupId, @NotNull String userId);

    /**
     * 为权限组移除用户
     *
     * @param platform 平台标识
     * @param nativeId 原生用户 ID
     * @param groupId  权限组 ID
     */
    default void removeUserGroup(@NotNull String groupId, @NotNull String platform, @NotNull String nativeId) {
        removeGroupUser(groupId, User.calculateId(platform, nativeId));
    }

    /**
     * 检查权限组是否包含指定用户
     *
     * @param groupId 权限组 ID
     * @param userId  用户 ID
     * @return 是否包含
     */
    boolean hasGroupUser(@NotNull String groupId, @NotNull String userId);

    /**
     * 检查权限组是否包含指定用户
     *
     * @param groupId  权限组 ID
     * @param platform 平台标识
     * @param nativeId 原生用户 ID
     * @return 是否包含
     */
    default boolean hasGroupUser(@NotNull String groupId, @NotNull String platform, @NotNull String nativeId) {
        return hasGroupUser(User.calculateId(platform, nativeId), groupId);
    }

}
