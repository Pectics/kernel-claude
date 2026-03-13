package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * 权限持有者密封接口
 * <p>
 * 允许 User 和 Group 实现此接口，拥有权限节点操作能力。
 * <p>
 * 设计参考：LuckPerms 的 PermissionHolder
 */
sealed interface PermissionHolder permits Group, User {

    /**
     * 获取唯一标识
     */
    @NotNull String getId();

    /**
     * 获取所有直接权限节点
     *
     * @return 权限节点集合
     */
    @NotNull Set<PermissionNode> getPermissionNodes();

    /**
     * 添加直接权限节点
     *
     * @param node 权限节点
     */
    void addPermissionNode(@NotNull PermissionNode node);

    /**
     * 移除直接权限节点
     *
     * @param key 权限键
     */
    void removePermissionNode(@NotNull String key);
}
