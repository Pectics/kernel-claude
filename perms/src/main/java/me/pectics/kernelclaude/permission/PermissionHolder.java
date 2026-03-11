package me.pectics.kernelclaude.permission;

import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Set;

import static me.pectics.kernelclaude.permission.PermissionResult.GRANTED;
import static me.pectics.kernelclaude.permission.PermissionResult.REJECTED;

/**
 * 权限持有者接口
 * <p>
 * User 和 Group 都实现此接口，拥有共同的权限操作能力。
 * <p>
 * 设计参考：LuckPerms 的 PermissionHolder 设计
 */
public interface PermissionHolder {

    /**
     * 权限持有者类型
     */
    enum Type {
        USER, // 用户
        GROUP // 权限组
    }

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

    /**
     * 获取继承的权限组
     *
     * @return 继承的权限组集合
     */
    @NotNull Set<Group> getSuperGroups();

    /**
     * 添加继承的权限组
     *
     * @param groupId 权限组 ID
     */
    void addSuperGroup(@NotNull String groupId);

    /**
     * 取消继承权限组
     *
     * @param groupId 权限组 ID
     */
    void removeSuperGroup(@NotNull String groupId);

    /**
     * 检查是否继承指定权限组
     *
     * @param groupId 要检查的权限组 ID
     * @return 是否继承
     */
    boolean hasSuperGroup(@NotNull String groupId);

    /**
     * 检查是否拥有指定权限
     * <p>
     * 权限检查顺序：<br>
     * 1. 检查直接权限<br>
     * 2. 检查继承的组权限（递归）
     *
     * @param key      权限键
     * @param contexts 当前上下文
     * @return 权限检查结果
     */
    default @NotNull PermissionResult checkPermission(String key, Set<Context> contexts) {
        if (key == null || key.isEmpty())
            return PermissionResult.UNDEFINED;

        // 1. 检查直接权限
        for (PermissionNode node : getPermissionNodes()) {
            if (!node.matches(key)) continue;
            if (!node.matches(contexts)) continue;
            if (node.isExpired()) continue;
            return node.value() ? GRANTED : REJECTED;
        }

        // 2. 检查继承的组权限（递归）
        val groups = getSuperGroups().stream()
                .sorted(Comparator.comparingInt(Group::getWeight).reversed()) // 权重高的组优先检查
                .toList();
        for (Group group : groups) {
            val result = group.checkPermission(key, contexts);
            if (result != PermissionResult.UNDEFINED)
                return result;
        }

        // 3. 没有明确的权限设置，返回 UNDEFINED
        return PermissionResult.UNDEFINED;
    }
}
