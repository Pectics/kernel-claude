package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.NotNull;

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
     * 获取组权重
     * <p>
     * 当用户属于多个组且权限冲突时，权重高的组优先
     */
    int getWeight();

    /**
     * 设置组权重
     */
    void setWeight(int weight);
}
