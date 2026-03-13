package me.pectics.kernelclaude.data.repository;

import lombok.RequiredArgsConstructor;
import me.pectics.kernelclaude.data.entity.GroupEntity;
import me.pectics.kernelclaude.data.entity.GroupSuperEntity;
import me.pectics.kernelclaude.data.entity.PermissionNodeEntity;
import me.pectics.kernelclaude.data.mapper.GroupMapper;
import me.pectics.kernelclaude.data.mapper.GroupSuperMapper;
import me.pectics.kernelclaude.data.mapper.PermissionNodeMapper;
import me.pectics.kernelclaude.permission.Context;
import me.pectics.kernelclaude.permission.Group;
import me.pectics.kernelclaude.permission.GroupRepository;
import me.pectics.kernelclaude.permission.PermissionNode;
import me.pectics.kernelclaude.permission.impl.SimpleGroup;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * GroupRepository 的 MyBatis 实现
 */
@Repository
@RequiredArgsConstructor
public class MyBatisGroupRepository implements GroupRepository {

    private static final String HOLDER_TYPE_GROUP = "GROUP";

    private final GroupMapper groupMapper;
    private final PermissionNodeMapper permissionNodeMapper;
    private final GroupSuperMapper groupSuperMapper;

    @Override
    public @NonNull Optional<Group> findById(String groupId) {
        GroupEntity entity = groupMapper.find(groupId);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(entity));
    }

    @Override
    public @NonNull Set<Group> findAll() {
        List<GroupEntity> entities = groupMapper.findAll();
        Set<Group> groups = new HashSet<>();
        for (GroupEntity entity : entities) {
            groups.add(toDomain(entity));
        }
        return groups;
    }

    @Override
    public @NonNull Set<String> findSuperIds(String groupId) {
        return new HashSet<>(groupSuperMapper.findSuperIdsByGroup(groupId));
    }

    @Override
    public void save(Group group) {
        long now = System.currentTimeMillis();

        // 保存组基本信息
        GroupEntity entity = GroupEntity.builder()
                .groupId(group.getId())
                .displayName(group.getDisplayName())
                .weight(group.getWeight())
                .createdAt(now)
                .updatedAt(now)
                .build();
        groupMapper.save(entity);

        String holderId = group.getId();

        // 删除旧的权限节点
        permissionNodeMapper.deleteByHolder(HOLDER_TYPE_GROUP, holderId);

        // 保存新的权限节点
        for (PermissionNode node : group.getPermissionNodes()) {
            PermissionNodeEntity nodeEntity = PermissionNodeEntity.builder()
                    .holderType(HOLDER_TYPE_GROUP)
                    .holderId(holderId)
                    .key(node.key())
                    .value(node.value())
                    .contexts(Context.toJson(node.contexts()))
                    .until(node.until())
                    .build();
            permissionNodeMapper.insert(nodeEntity);
        }

        // 删除旧的继承关系
        groupSuperMapper.deleteByGroup(holderId);

        // 保存新的继承关系
        for (String superId : group.getSupers()) {
            groupSuperMapper.insert(GroupSuperEntity.builder()
                    .groupId(holderId)
                    .superId(superId)
                    .build());
        }

    }

    @Override
    public void delete(String groupId) {
        // 删除权限节点
        permissionNodeMapper.deleteByHolder(HOLDER_TYPE_GROUP, groupId);

        // 删除继承关系（作为子组和作为父组）
        groupSuperMapper.deleteByGroup(groupId);
        groupSuperMapper.deleteBySuper(groupId);

        // 删除组
        groupMapper.delete(groupId);
    }

    @Override
    public boolean exists(String groupId) {
        return groupMapper.exists(groupId);
    }

    /**
     * 将数据库实体转换为领域对象
     */
    private Group toDomain(GroupEntity entity) {
        // 创建组对象
        SimpleGroup group = new SimpleGroup(entity.getGroupId(), entity.getWeight());
        group.setDisplayName(entity.getDisplayName());

        // 加载权限节点
        List<PermissionNodeEntity> nodeEntities = permissionNodeMapper.findByHolder(HOLDER_TYPE_GROUP, entity.getGroupId());
        for (PermissionNodeEntity nodeEntity : nodeEntities) {
            PermissionNode node = new PermissionNode(
                    nodeEntity.getKey(),
                    nodeEntity.getValue(),
                    Context.fromJson(nodeEntity.getContexts()),
                    nodeEntity.getUntil()
            );
            group.addPermissionNode(node);
        }

        // 加载继承关系
        List<String> superIds = groupSuperMapper.findSuperIdsByGroup(entity.getGroupId());
        for (String superId : superIds) {
            group.inherit(superId);
        }

        return group;
    }
}
