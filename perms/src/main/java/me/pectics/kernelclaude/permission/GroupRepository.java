package me.pectics.kernelclaude.permission;

import java.util.Optional;
import java.util.Set;

/**
 * 权限组仓库接口
 * <p>
 * 负责权限组的持久化操作
 */
public interface GroupRepository {

    /**
     * 根据权限组 ID 查找权限组
     *
     * @param groupId 权限组 ID
     * @return 权限组对象
     */
    Optional<Group> findById(String groupId);

    /**
     * 查找所有权限组
     *
     * @return 权限组对象集合
     */
    Set<Group> findAll();

    /**
     * 查找指定权限组的所有继承的权限组 ID
     *
     * @param groupId 权限组 ID
     * @return 继承的权限组 ID 集合
     */
    Set<String> findInheritedGroupIds(String groupId);

    /**
     * 保存权限组（新增或更新）
     *
     * @param group 权限组对象
     * @return 保存后的权限组对象
     */
    Group save(Group group);

    /**
     * 删除权限组
     *
     * @param groupId 权限组 ID
     */
    void delete(String groupId);

    /**
     * 检查权限组是否存在
     *
     * @param groupId 权限组 ID
     * @return 是否存在
     */
    boolean exists(String groupId);
}
