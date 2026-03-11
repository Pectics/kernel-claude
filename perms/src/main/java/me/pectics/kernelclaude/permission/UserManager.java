package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * 用户管理器
 */
public interface UserManager {

    /**
     * 使用用户 ID 获取用户
     *
     * @param id 用户 ID
     * @return 用户对象，不存在则返回 null
     */
    @Nullable User getUser(String id);

    /**
     * 使用平台标识和平台用户 ID 获取用户
     *
     * @param platformId 平台标识
     * @param userId     平台用户 ID
     * @return 用户对象，不存在则返回 null
     */
    @Nullable User getUser(String platformId, String userId);

    /**
     * 获取指定平台下的所有用户
     *
     * @param platformId 平台标识
     * @return 用户集合
     */
    @NotNull Collection<User> getUsersByPlatform(String platformId);

    /**
     * 获取属于指定权限组的所有用户
     *
     * @param groupId 权限组 ID
     * @return 用户集合
     */
    @NotNull Collection<User> getUsersByGroup(String groupId);

    /**
     * 创建用户
     *
     * @param platformId 平台标识
     * @param userId     平台用户 ID
     * @return 新创建的用户
     * @throws IllegalArgumentException 如果用户名已存在
     */
    @NotNull User createUser(String platformId, String userId);

    /**
     * 获取或创建用户
     *
     * @param platformId 平台标识
     * @param userId     平台用户 ID
     * @return 用户对象（如果存在则返回现有用户，否则创建新用户）
     */
    default @NotNull User getOrCreateUser(String platformId, String userId) {
        User user = getUser(platformId, userId);
        if (user != null)
            return user;
        return createUser(platformId, userId);
    }

    /**
     * 使用用户 ID 删除用户
     *
     * @param id 用户 ID
     * @return 是否成功
     */
    boolean deleteUser(String id);

    /**
     * 使用平台标识和平台用户 ID 删除用户
     *
     * @param platformId 平台标识
     * @param userId     平台用户 ID
     * @return 是否成功
     */
    boolean deleteUser(String platformId, String userId);

    /**
     * 使用用户 ID 检查用户是否存在
     *
     * @param id 用户 ID
     * @return 是否存在
     */
    boolean hasUser(String id);

    /**
     * 使用平台标识和平台用户 ID 检查用户是否存在
     *
     * @param platformId 平台标识
     * @param userId     平台用户 ID
     * @return 是否存在
     */
    boolean hasUser(String platformId, String userId);
}
