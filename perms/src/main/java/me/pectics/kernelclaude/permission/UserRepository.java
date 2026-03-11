package me.pectics.kernelclaude.permission;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

/**
 * 用户仓库接口
 * <p>
 * 负责用户的持久化操作
 */
public interface UserRepository {

    /**
     * 根据用户 ID 查找用户
     *
     * @param id 用户 ID
     * @return 用户对象，不存在则返回 empty
     */
    @NotNull Optional<User> findById(String id);

    /**
     * 根据平台标识和平台用户 ID 查找用户
     *
     * @param platform 平台标识
     * @param nativeId 原生用户 ID
     * @return 用户对象，不存在则返回 empty
     */
    @NotNull Optional<User> findByPlatformAndNativeId(String platform, String nativeId);

    /**
     * 查找指定平台下的所有用户
     *
     * @param platform 平台标识
     * @return 用户集合
     */
    @NotNull Set<User> findByPlatform(String platform);

    /**
     * 查找属于指定权限组的所有用户
     *
     * @param groupId 权限组 ID
     * @return 用户集合
     */
    @NotNull Set<User> findByGroupId(String groupId);

    /**
     * 保存用户（新增或更新）
     *
     * @param user 用户对象
     * @return 保存后的用户
     */
    User save(User user);

    /**
     * 删除用户
     *
     * @param platform 平台标识
     * @param nativeId 原生用户 ID
     */
    void delete(String platform, String nativeId);

    /**
     * 检查用户是否存在
     *
     * @param platform 平台标识
     * @param nativeId 原生用户 ID
     * @return 是否存在
     */
    boolean exists(String platform, String nativeId);
}
