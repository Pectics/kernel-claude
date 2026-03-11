package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * 权限持有者接口
 * <p>
 * User 和 Group 都实现此接口，拥有共同的权限操作能力。
 * <p>
 * 设计参考：LuckPerms 的 PermissionHolder 设计
 */
public interface PermissionHolder {

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
    void addPermissionNode(PermissionNode node);

    /**
     * 移除直接权限节点
     *
     * @param key 权限键
     */
    void removePermissionNode(String key);

    /**
     * 获取继承权限组
     *
     * @return 继承权限组集合
     */
    @NotNull Set<Group> getSuperGroups();

    /**
     * 添加继承权限组
     *
     * @param groupId 权限组 ID
     */
    void addSuperGroup(String groupId);

    /**
     * 取消继承权限组
     *
     * @param groupId 权限组 ID
     */
    void removeSuperGroup(String groupId);

    /**
     * 获取权重（用于冲突解决）
     * <p>
     * 权重越高，优先级越高
     *
     * @return 权重值
     */
    int getWeight();

    /**
     * 检查是否拥有指定权限
     * <p>
     * 权限检查顺序：<br>
     * 1. 检查直接权限<br>
     * 2. 检查继承的组权限（递归）<br>
     * 3. 考虑上下文条件
     *
     * @param key      权限键
     * @param contexts 当前上下文
     * @return 权限检查结果
     */
    @NotNull PermissionResult checkPermission(String key, Set<Context> contexts);
}
