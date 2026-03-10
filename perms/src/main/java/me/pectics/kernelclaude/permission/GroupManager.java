package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * 权限组管理器
 */
public interface GroupManager {

    /**
     * 获取权限组
     *
     * @param groupName 组名
     * @return 权限组，不存在则返回 null
     */
    @Nullable
    Group getGroup(String groupName);

    /**
     * 创建权限组
     *
     * @param groupName 组名
     * @return 新创建的组
     * @throws IllegalArgumentException 如果组名已存在
     */
    Group createGroup(String groupName);

    /**
     * 获取或创建权限组
     *
     * @param groupName 组名
     * @return 权限组（如果存在则返回现有组，否则创建新组）
     */
    Group getOrCreateGroup(String groupName);

    /**
     * 删除权限组
     *
     * @param groupName 组名
     * @return 是否删除成功
     */
    boolean deleteGroup(String groupName);

    /**
     * 检查权限组是否存在
     *
     * @param groupName 组名
     * @return 是否存在
     */
    boolean hasGroup(String groupName);

    /**
     * 获取所有权限组
     *
     * @return 权限组集合
     */
    Collection<Group> getGroups();

    /**
     * 获取权限组数量
     *
     * @return 数量
     */
    int getGroupCount();
}
