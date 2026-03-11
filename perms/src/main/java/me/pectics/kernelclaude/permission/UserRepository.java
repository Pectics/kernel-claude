package me.pectics.kernelclaude.permission;

import java.util.Optional;
import java.util.Set;

/**
 * 用户仓库接口
 * <p>
 * 负责用户的持久化操作
 */
public interface UserRepository {

    /**
     * 根据平台标识和平台用户 ID 查找用户
     *
     * @param platformId 平台标识
     * @param userId     平台用户 ID
     * @return 用户对象，不存在则返回 empty
     */
    Optional<User> findByPlatformIdAndUserId(String platformId, String userId);

    /**
     * 根据用户唯一复合标识查找用户
     *
     * @param id 复合 ID
     * @return 用户对象
     */
    Optional<User> findById(String id);

    /**
     * 查找指定平台下的所有用户
     *
     * @param platformId 平台标识
     * @return 用户集合
     */
    Set<User> findByPlatformId(String platformId);

    /**
     * 查找属于指定权限组的所有用户
     *
     * @param groupId 权限组 ID
     * @return 用户集合
     */
    Set<User> findByGroupId(String groupId);

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
     * @param platformId 平台标识
     * @param userId     平台用户 ID
     */
    void delete(String platformId, String userId);

    /**
     * 检查用户是否存在
     *
     * @param platformId 平台标识
     * @param userId     平台用户 ID
     * @return 是否存在
     */
    boolean exists(String platformId, String userId);
}
