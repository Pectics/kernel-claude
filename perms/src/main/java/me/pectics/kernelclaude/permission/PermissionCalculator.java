package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * 权限计算器
 * <p>
 * 负责计算用户的最终权限。计算过程：
 * <ol>
 *   <li>收集用户直接拥有的权限节点</li>
 *   <li>收集用户所属组的权限节点</li>
 *   <li>递归收集组继承的权限节点</li>
 *   <li>按权重和优先级合并所有节点</li>
 *   <li>返回最终的权限检查结果</li>
 * </ol>
 * <p>
 * 设计参考：LuckPerms 的 PermissionCalculator
 */
public interface PermissionCalculator {

    /**
     * 检查用户是否拥有指定权限
     *
     * @param user     用户
     * @param key      权限键
     * @param contexts 上下文条件
     * @return 权限检查结果
     */
    @NotNull PermissionResult check(User user, String key, Set<Context> contexts);

    /**
     * 检查用户是否拥有指定权限
     *
     * @param user 用户
     * @param key  权限键
     * @return 权限检查结果
     */
    default @NotNull PermissionResult check(User user, String key) {
        return check(user, key, Set.of());
    }

    /**
     * 获取用户的所有有效权限
     *
     * @param user     用户
     * @param contexts 上下文条件
     * @return 所有生效的权限节点
     */
    @NotNull Set<PermissionNode> getPermissions(User user, Set<Context> contexts);

    /**
     * 获取用户的所有有效权限
     *
     * @param user 用户
     * @return 所有生效的权限节点
     */
    default @NotNull Set<PermissionNode> getPermissions(User user) {
        return getPermissions(user, Set.of());
    }

    /**
     * 获取组的所有有效权限（包括继承的）
     *
     * @param group    权限组
     * @param contexts 上下文条件
     * @return 所有生效的权限节点
     */
    @NotNull Set<PermissionNode> getPermissions(Group group, Set<Context> contexts);

    /**
     * 获取组的所有有效权限（包括继承的）
     *
     * @param group 权限组
     * @return 所有生效的权限节点
     */
    default @NotNull Set<PermissionNode> getPermissions(Group group) {
        return getPermissions(group, Set.of());
    }

}
