package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * 用户管理器
 */
public interface UserManager {

    /**
     * 获取用户
     *
     * @param username 用户名
     * @return 用户，不存在则返回 null
     */
    @Nullable
    User getUser(String username);

    /**
     * 创建用户
     *
     * @param username 用户名
     * @return 新创建的用户
     * @throws IllegalArgumentException 如果用户名已存在
     */
    User createUser(String username);

    /**
     * 获取或创建用户
     *
     * @param username 用户名
     * @return 用户（如果存在则返回现有用户，否则创建新用户）
     */
    User getOrCreateUser(String username);

    /**
     * 删除用户
     *
     * @param username 用户名
     * @return 是否成功
     */
    boolean deleteUser(String username);

    /**
     * 检查用户是否存在
     *
     * @param username 用户名
     * @return 是否存在
     */
    boolean hasUser(String username);

    /**
     * 获取所有用户
     *
     * @return 用户集合
     */
    Collection<User> getUsers();

    /**
     * 获取用户数量
     *
     * @return 数量
     */
    int getUserCount();
}
