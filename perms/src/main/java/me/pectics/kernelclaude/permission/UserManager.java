package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

/**
 * 用户管理器
 */
public interface UserManager {

    /**
     * 使用用户 ID 获取用户
     *
     * @param id 用户 ID
     * @return 用户对象，不存在则返回 empty
     */
    @NotNull Optional<User> getUser(String id);

    /**
     * 使用平台和原生用户 ID 获取用户
     *
     * @param platform 平台标识
     * @param nativeId 原生用户 ID
     * @return 用户对象，不存在则返回 empty
     */
    @NotNull Optional<User> getUser(String platform, String nativeId);

    /**
     * 获取指定平台下的所有用户
     *
     * @param platform 平台标识
     * @return 用户集合
     */
    @NotNull Collection<User> getUsersByPlatform(String platform);

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
     * @param platform 平台标识
     * @param nativeId 原生用户 ID
     * @return 新创建的用户
     * @throws IllegalArgumentException 如果用户名已存在
     */
    @NotNull User createUser(String platform, String nativeId);

    /**
     * 获取或创建用户
     *
     * @param platform 平台标识
     * @param nativeId 原生用户 ID
     * @return 用户对象（如果存在则返回现有用户，否则创建新用户）
     */
    default @NotNull User getOrCreateUser(String platform, String nativeId) {
        return getUser(platform, nativeId).orElseGet(() -> createUser(platform, nativeId));
    }

    /**
     * 使用用户 ID 删除用户
     *
     * @param id 用户 ID
     * @return 是否成功
     */
    boolean deleteUser(String id);

    /**
     * 使用平台标识和原生用户 ID 删除用户
     *
     * @param platform 平台标识
     * @param nativeId 原生用户 ID
     * @return 是否成功
     */
    boolean deleteUser(String platform, String nativeId);

    /**
     * 使用用户 ID 检查用户是否存在
     *
     * @param id 用户 ID
     * @return 是否存在
     */
    boolean hasUser(String id);

    /**
     * 使用平台标识和原生用户 ID 检查用户是否存在
     *
     * @param platform 平台标识
     * @param nativeId 原生用户 ID
     * @return 是否存在
     */
    boolean hasUser(String platform, String nativeId);
}
