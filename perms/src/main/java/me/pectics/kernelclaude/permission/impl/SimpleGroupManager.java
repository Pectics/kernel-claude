package me.pectics.kernelclaude.permission.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.pectics.kernelclaude.permission.Group;
import me.pectics.kernelclaude.permission.GroupManager;
import me.pectics.kernelclaude.permission.GroupRepository;
import me.pectics.kernelclaude.permission.PermissionCalculator;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GroupManager 的简单实现
 * <p>
 * 带内存缓存层的权限组管理器
 */
@RequiredArgsConstructor
public class SimpleGroupManager implements GroupManager {

    private final GroupRepository groupRepository;
    private final PermissionCalculator permissionCalculator;

    /**
     * 权限组缓存
     * Key: 权限组 ID
     */
    private final Map<String, Group> groupCache = new ConcurrentHashMap<>();

    @Override
    public @NotNull Optional<Group> getGroup(@NonNull String groupId) {
        return Optional.ofNullable(
                groupCache.computeIfAbsent(groupId, key ->
                        groupRepository.findById(key).orElse(null)
                )
        );
    }

    @Override
    public @NotNull Group createGroup(@NonNull String groupId, int weight, String displayName) {
        if (hasGroup(groupId)) {
            throw new IllegalArgumentException("Group already exists: " + groupId);
        }

        // 创建新权限组
        String name = displayName != null ? displayName : groupId;
        SimpleGroup group = new SimpleGroup(groupId, weight, name);
        injectDependencies(group);

        // 保存到仓库
        groupRepository.save(group);

        // 加入缓存
        groupCache.put(groupId, group);

        return group;
    }

    @Override
    public boolean deleteGroup(@NonNull String groupId) {
        if (!hasGroup(groupId)) {
            return false;
        }

        // 从仓库删除
        groupRepository.delete(groupId);

        // 从缓存移除
        groupCache.remove(groupId);

        return true;
    }

    @Override
    public boolean hasGroup(@NonNull String groupId) {
        if (groupCache.containsKey(groupId)) {
            return true;
        }
        return groupRepository.exists(groupId);
    }

    /**
     * 保存权限组到仓库并更新缓存
     *
     * @param group 权限组对象
     */
    public void saveGroup(@NonNull Group group) {
        groupRepository.save(group);
        groupCache.put(group.getId(), group);
    }

    /**
     * 刷新缓存中的权限组数据
     *
     * @param groupId 权限组 ID
     */
    public void refreshGroup(@NonNull String groupId) {
        groupCache.remove(groupId);
        getGroup(groupId);
    }

    /**
     * 清空缓存
     */
    public void clearCache() {
        groupCache.clear();
    }

    /**
     * 向权限组对象注入依赖
     */
    private void injectDependencies(SimpleGroup group) {
        group.setCalculator(permissionCalculator);
        group.setGroupManager(this);
    }
}
