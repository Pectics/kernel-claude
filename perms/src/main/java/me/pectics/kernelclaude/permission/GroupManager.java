package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * 权限组管理器
 */
public interface GroupManager {

    // Group 基本操作接口

    /**
     * 使用权限组 ID 获取权限组
     *
     * @param groupId 权限组 ID
     * @return 权限组对象，不存在则返回 null
     */
    @NotNull Optional<Group> getGroup(String groupId);

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
        return getGroup(groupId).orElseGet(() -> createGroup(groupId, weight, displayName));
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

    // Group-Super 关联相关接口

    /**
     * 获取权限组所继承的权限组集合
     *
     * @param groupId 权限组 ID
     * @return 继承的权限组集合
     */
    @NotNull Set<Group> getGroupSupers(@NotNull String groupId);

    /**
     * 将权限组继承自另一权限组
     *
     * @param groupId 权限组 ID
     * @param superId 另一权限组 ID
     */
    void addGroupSuper(@NotNull String groupId, @NotNull String superId);

    /**
     * 将权限组取消继承自另一权限组
     *
     * @param groupId 权限组 ID
     * @param superId 另一权限组 ID
     */
    void removeGroupSuper(@NotNull String groupId, @NotNull String superId);

    /**
     * 检查权限组是否存在继承自的权限组
     *
     * @param groupId 权限组 ID
     * @return 是否存在
     */
    boolean hasGroupSuper(@NotNull String groupId);

    /**
     * 检查权限组是否<strong>间接继承</strong>自另一权限组
     * <p>
     * 要进行<strong>直接继承</strong>检查请参阅：{@link #hasDirectGroupSuper(String, String)}
     *
     * @param groupId 权限组 ID
     * @param superId 另一权限组 ID
     * @return 是否<strong>间接继承</strong>
     */
    boolean hasGroupSuper(@NotNull String groupId, @NotNull String superId);

    /**
     * 检查权限组是否<strong>直接继承</strong>继承自另一权限组
     *
     * @param groupId 权限组 ID
     * @param superId 另一权限组 ID
     * @return 是否<strong>直接继承</strong>
     */
    boolean hasDirectGroupSuper(@NotNull String groupId, @NotNull String superId);
}
