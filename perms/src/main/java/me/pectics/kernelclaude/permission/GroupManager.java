package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 权限组管理器
 */
public interface GroupManager {

    /**
     * 使用权限组 ID 获取权限组
     *
     * @param groupId 权限组 ID
     * @return 权限组对象，不存在则返回 null
     */
    @Nullable Group getGroup(String groupId);

    /**
     * 新增权限组
     *
     * @param groupId     权限组 ID
     * @param weight      权限组权重
     * @param displayName 显示名称
     * @return 新创建的权限组对象
     * @throws IllegalArgumentException 如果权限组 ID 已存在
     */
    @NotNull Group createGroup(String groupId, int weight, String displayName);

    /**
     * 新增权限组
     *
     * @param groupId 权限组 ID
     * @param weight  权限组权重
     * @return 新创建的权限组对象
     * @throws IllegalArgumentException 如果权限组 ID 已存在
     */
    default @NotNull Group createGroup(String groupId, int weight) {
        return createGroup(groupId, weight, null);
    }

    /**
     * 新增权限组
     *
     * @param groupId 权限组 ID
     * @return 新创建的权限组对象
     * @throws IllegalArgumentException 如果权限组 ID 已存在
     */
    default @NotNull Group createGroup(String groupId) {
        return createGroup(groupId, 0, null);
    }

    /**
     * 获取或新增权限组
     *
     * @param groupId     权限组 ID
     * @param weight      权限组权重
     * @param displayName 显示名称
     * @return 权限组对象（如果存在则返回现有权限组，否则创建新权限组）
     */
    default @NotNull Group getOrCreateGroup(String groupId, int weight, String displayName) {
        Group group = getGroup(groupId);
        if (group != null)
            return group;
        return createGroup(groupId, weight, displayName);
    }

    /**
     * 获取或新增权限组
     *
     * @param groupId 权限组 ID
     * @param weight  权限组权重
     * @return 权限组对象（如果存在则返回现有权限组，否则创建新权限组）
     */
    default @NotNull Group getOrCreateGroup(String groupId, int weight) {
        return getOrCreateGroup(groupId, weight, null);
    }

    /**
     * 获取或新增权限组
     *
     * @param groupId 权限组 ID
     * @return 权限组对象（如果存在则返回现有权限组，否则创建新权限组）
     */
    default @NotNull Group getOrCreateGroup(String groupId) {
        return getOrCreateGroup(groupId, 0, null);
    }

    /**
     * 删除权限组
     *
     * @param groupId 权限组 ID
     * @return 是否成功
     */
    boolean deleteGroup(String groupId);

    /**
     * 检查权限组是否存在
     *
     * @param groupId 权限组 ID
     * @return 是否存在
     */
    boolean hasGroup(String groupId);
}
