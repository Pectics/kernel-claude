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
    @NotNull Optional<User> find(String id);

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
    @NotNull Set<User> findByGroup(String groupId);

    /**
     * 保存用户（新增或更新）
     *
     * @param user 用户对象
     * @return 保存后的用户
     */
    @NotNull User save(User user);

    /**
     * 删除用户
     *
     * @param id 用户 ID
     */
    void delete(String id);

    /**
     * 检查用户是否存在
     *
     * @param id 用户 ID
     * @return 是否存在
     */
    boolean exists(String id);
}
