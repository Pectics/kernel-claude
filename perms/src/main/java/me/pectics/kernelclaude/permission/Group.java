package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * 权限组
 * <p>
 * 一组可以复用的权限集合
 */
public interface Group extends PermissionHolder {

    /**
     * 获取权限组唯一标识
     */
    @NotNull String getId();

    /**
     * 获取显示名称
     */
    @NotNull String getDisplayName();

    /**
     * 获取组权重
     * <p>
     * 当用户属于多个组且权限冲突时，权重高的组优先
     */
    int getWeight();

    /**
     * 设置组权重
     */
    void setWeight(int weight);

    /**
     * 继承另一权限组
     *
     * @param groupId 要继承的权限组 ID
     */
    void inherit(String groupId);

    /**
     * 取消继承另一权限组
     *
     * @param groupId 要取消继承的权限组 ID
     */
    void uninherit(String groupId);

    /**
     * 获取所有继承的组
     */
    @NotNull Set<String> getSupers();

    /**
     * 检查是否继承了指定组（包括间接继承）
     *
     * @param groupId 要检查的权限组 ID
     * @return 是否继承
     */
    boolean inherits(String groupId);
}
