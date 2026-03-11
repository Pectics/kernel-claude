package me.pectics.kernelclaude.data.repository;

import lombok.RequiredArgsConstructor;
import lombok.val;
import me.pectics.kernelclaude.data.entity.PermissionNodeEntity;
import me.pectics.kernelclaude.data.entity.UserEntity;
import me.pectics.kernelclaude.data.entity.UserGroupEntity;
import me.pectics.kernelclaude.data.mapper.PermissionNodeMapper;
import me.pectics.kernelclaude.data.mapper.UserGroupMapper;
import me.pectics.kernelclaude.data.mapper.UserMapper;
import me.pectics.kernelclaude.data.util.StripedLock;
import me.pectics.kernelclaude.permission.Context;
import me.pectics.kernelclaude.permission.PermissionNode;
import me.pectics.kernelclaude.permission.User;
import me.pectics.kernelclaude.permission.UserRepository;
import me.pectics.kernelclaude.permission.impl.SimpleUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * UserRepository 的 MyBatis 实现
 */
@Repository
@RequiredArgsConstructor
public class MyBatisUserRepository implements UserRepository {

    private static final StripedLock<User> locks = new StripedLock<>();

    private final UserMapper userMapper;
    private final UserGroupMapper userGroupMapper;
    private final PermissionNodeMapper permissionNodeMapper;

    @Override
    public @NotNull Optional<User> find(String id) {
        UserEntity entity = userMapper.find(id);
        if (entity == null)
            return Optional.empty();
        return Optional.of(toDomain(entity));
    }

    @Override
    public @NotNull Set<User> findByPlatform(String platform) {
        List<UserEntity> entities = userMapper.findByPlatform(platform);
        Set<User> users = new HashSet<>();
        for (UserEntity entity : entities)
            users.add(toDomain(entity));
        return users;
    }

    @Override
    public @NotNull Set<User> findByGroup(String groupId) {
        List<UserEntity> entities = userMapper.findByGroup(groupId);
        Set<User> users = new HashSet<>();
        for (UserEntity entity : entities)
            users.add(toDomain(entity));
        return users;
    }

    @Override
    public @NotNull User save(User user) {
        locks.withLock(user, this::doSave);
        return user;
    }

    @Transactional
    protected void doSave(User user) {
        // region 保存用户基本信息
        userMapper.save(UserEntity.from(user));
        // endregion

        // region 保存用户权限节点
        val oldEntities = permissionNodeMapper.findByUser(user.getId());
        val newNodes = user.getPermissionNodes();

        // 找出需要删除的旧权限节点和需要新增的权限节点
        val toDeleteEntities = new ArrayList<PermissionNodeEntity>();
        val toInsertNodes = new ArrayList<>(newNodes);

        // 遍历旧权限节点
        for (val entity : oldEntities) {
            val node = entity.toDomain();
            if (!newNodes.contains(node))
                toDeleteEntities.add(entity);
            else
                toInsertNodes.remove(node);
        }

        // 删除 toDeleteEntities 中的权限节点，插入 toInsertNodes 中的权限节点
        permissionNodeMapper.deleteBatch(
                toDeleteEntities.stream()
                        .map(PermissionNodeEntity::getNodeId)
                        .toList());
        permissionNodeMapper.insertBatch(
                toInsertNodes.stream()
                        .map(PermissionNodeEntity::from)
                        .toList());
        // endregion

        // region 保存用户权限组关联
        val oldGroups = new HashSet<>(userGroupMapper.findGroupIdsByUser(user.getId()));
        val newGroups = user.getGroups();

        val toDeleteRelations = oldGroups.stream()
                .filter(g -> !newGroups.contains(g))
                .toList();
        val toInsertRelations = newGroups.stream()
                .filter(g -> !oldGroups.contains(g))
                .toList();

        userGroupMapper.deleteGroupIdsByUser(user.getId(), toDeleteRelations);
        userGroupMapper.insertGroupIdsByUser(user.getId(), toInsertRelations);
        // endregion
    }


    /**
     * 将数据库实体转换为领域对象
     */
    private User toDomain(UserEntity entity) {
    }

}
