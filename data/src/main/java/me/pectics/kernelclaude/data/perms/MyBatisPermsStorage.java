/*
 * MyBatis implementation of Storage interface for perms module
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.perms;

import lombok.extern.slf4j.Slf4j;
import me.pectics.kernelclaude.data.perms.entity.GroupEntity;
import me.pectics.kernelclaude.data.perms.entity.GroupNodeEntity;
import me.pectics.kernelclaude.data.perms.entity.UserEntity;
import me.pectics.kernelclaude.data.perms.entity.UserNodeEntity;
import me.pectics.kernelclaude.data.perms.mapper.GroupMapper;
import me.pectics.kernelclaude.data.perms.mapper.GroupNodeMapper;
import me.pectics.kernelclaude.data.perms.mapper.UserMapper;
import me.pectics.kernelclaude.data.perms.mapper.UserNodeMapper;
import me.pectics.kernelclaude.perms.model.DataType;
import me.pectics.kernelclaude.perms.model.Group;
import me.pectics.kernelclaude.perms.model.SimpleGroup;
import me.pectics.kernelclaude.perms.model.SimpleUser;
import me.pectics.kernelclaude.perms.model.User;
import me.pectics.kernelclaude.perms.node.Node;
import me.pectics.kernelclaude.perms.storage.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * MyBatis-based implementation of the Storage interface.
 */
@Slf4j
public class MyBatisPermsStorage implements Storage {

    private final UserMapper userMapper;
    private final UserNodeMapper userNodeMapper;
    private final GroupMapper groupMapper;
    private final GroupNodeMapper groupNodeMapper;
    private final ExecutorService executor;

    private volatile SimpleGroup.GroupResolver groupGroupResolver;
    private volatile SimpleUser.GroupResolver userGroupResolver;
    private volatile SimpleUser.PrimaryGroupValidator primaryGroupValidator;

    public MyBatisPermsStorage(
            UserMapper userMapper,
            UserNodeMapper userNodeMapper,
            GroupMapper groupMapper,
            GroupNodeMapper groupNodeMapper,
            ExecutorService executor) {
        this.userMapper = userMapper;
        this.userNodeMapper = userNodeMapper;
        this.groupMapper = groupMapper;
        this.groupNodeMapper = groupNodeMapper;
        this.executor = executor;
    }

    /**
     * Sets the group resolver for inheritance resolution (for groups).
     *
     * @param resolver the resolver
     */
    public void setGroupResolver(@Nullable SimpleGroup.GroupResolver resolver) {
        this.groupGroupResolver = resolver;
    }

    /**
     * Sets the group resolver for inheritance resolution (for users).
     *
     * @param resolver the resolver
     */
    public void setUserGroupResolver(@Nullable SimpleUser.GroupResolver resolver) {
        this.userGroupResolver = resolver;
    }

    /**
     * Sets the primary group validator.
     *
     * @param validator the validator
     */
    public void setPrimaryGroupValidator(@Nullable SimpleUser.PrimaryGroupValidator validator) {
        this.primaryGroupValidator = validator;
    }

    // ==================== User Operations ====================

    @Override
    public @NotNull CompletableFuture<@Nullable User> loadUser(@NotNull String uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            UserEntity entity = userMapper.findByUserId(uniqueId);
            if (entity == null)
                return null;

            SimpleUser user = new SimpleUser(entity.getPlatform(), entity.getNativeId());
            user.setPrimaryGroup(entity.getPrimaryGroup());
            user.setGroupResolver(userGroupResolver);
            user.setPrimaryGroupValidator(primaryGroupValidator);

            // Load nodes
            List<UserNodeEntity> nodeEntities = userNodeMapper.findByUserId(uniqueId);
            for (UserNodeEntity ne : nodeEntities) {
                Node node = NodeEntityMapper.toNode(ne);
                if (node != null && !node.hasExpired())
                    user.getData(DataType.NORMAL).add(node);
            }

            return user;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<@Nullable User> loadUser(@NotNull String platform, @NotNull String nativeId) {
        return CompletableFuture.supplyAsync(() -> {
            UserEntity entity = userMapper.findByPlatformAndNativeId(platform, nativeId);
            if (entity == null)
                return null;

            SimpleUser user = new SimpleUser(entity.getPlatform(), entity.getNativeId());
            user.setPrimaryGroup(entity.getPrimaryGroup());
            user.setGroupResolver(userGroupResolver);
            user.setPrimaryGroupValidator(primaryGroupValidator);

            // Load nodes
            List<UserNodeEntity> nodeEntities = userNodeMapper.findByUserId(entity.getUserId());
            for (UserNodeEntity ne : nodeEntities) {
                Node node = NodeEntityMapper.toNode(ne);
                if (node != null && !node.hasExpired())
                    user.getData(DataType.NORMAL).add(node);
            }

            return user;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveUser(@NotNull User user) {
        return CompletableFuture.runAsync(() -> {
            String userId = user.getUserId();

            // Delete existing nodes
            userNodeMapper.deleteByUserId(userId);

            // Insert new nodes
            List<UserNodeEntity> nodes = user.getData(DataType.NORMAL).toCollection().stream()
                    .filter(node -> !node.hasExpired())
                    .map(node -> NodeEntityMapper.toUserNodeEntity(userId, node))
                    .toList();

            if (!nodes.isEmpty())
                userNodeMapper.batchInsert(nodes);

            // Update or insert user
            UserEntity entity = new UserEntity(
                    userId,
                    user.getPlatform(),
                    user.getNativeId(),
                    user.getPrimaryGroup()
            );
            if (userMapper.update(entity) == 0) {
                userMapper.insert(entity);
            }

            log.debug("Saved user: {}", userId);
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Set<String>> getAllUserIds() {
        return CompletableFuture.supplyAsync(() -> {
            List<String> ids = userMapper.findAllUserIds();
            return new HashSet<>(ids);
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> deleteUser(@NotNull String uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            // Delete nodes first
            userNodeMapper.deleteByUserId(uniqueId);
            // Delete user
            int affected = userMapper.deleteByUserId(uniqueId);
            return affected > 0;
        }, executor);
    }

    // ==================== Group Operations ====================

    @Override
    public @NotNull CompletableFuture<@Nullable Group> loadGroup(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            GroupEntity entity = groupMapper.findByGroupId(name);
            if (entity == null)
                return null;

            SimpleGroup group = new SimpleGroup(entity.getGroupId());
            group.setGroupResolver(groupGroupResolver);

            // Load nodes
            List<GroupNodeEntity> nodeEntities = groupNodeMapper.findByGroupId(name);
            for (GroupNodeEntity ne : nodeEntities) {
                Node node = NodeEntityMapper.toNode(ne);
                if (node != null && !node.hasExpired())
                    group.getData(DataType.NORMAL).add(node);
            }

            return group;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveGroup(@NotNull Group group) {
        return CompletableFuture.runAsync(() -> {
            String groupId = group.getGroupId();

            // Delete existing nodes
            groupNodeMapper.deleteByGroupId(groupId);

            // Insert new nodes
            List<GroupNodeEntity> nodes = group.getData(DataType.NORMAL).toCollection().stream()
                    .filter(node -> !node.hasExpired())
                    .map(node -> NodeEntityMapper.toGroupNodeEntity(groupId, node))
                    .toList();

            if (!nodes.isEmpty())
                groupNodeMapper.batchInsert(nodes);

            // Ensure group exists
            GroupEntity entity = groupMapper.findByGroupId(groupId);
            if (entity == null) {
                groupMapper.insert(new GroupEntity(groupId));
            }

            log.debug("Saved group: {}", groupId);
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Group> createGroup(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            GroupEntity entity = groupMapper.findByGroupId(name);
            if (entity != null)
                return loadGroup(name).join();

            groupMapper.insert(new GroupEntity(name));
            SimpleGroup group = new SimpleGroup(name);
            group.setGroupResolver(groupGroupResolver);
            return group;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<Boolean> deleteGroup(@NotNull String name) {
        return CompletableFuture.supplyAsync(() -> {
            // Delete nodes first
            groupNodeMapper.deleteByGroupId(name);
            // Delete group
            int affected = groupMapper.deleteByGroupId(name);
            return affected > 0;
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Set<String>> getAllGroupNames() {
        return CompletableFuture.supplyAsync(() -> {
            List<String> names = groupMapper.findAllGroupIds();
            return new HashSet<>(names);
        }, executor);
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Collection<Group>> loadAllGroups() {
        return CompletableFuture.supplyAsync(() -> {
            List<String> groupIds = groupMapper.findAllGroupIds();
            Set<Group> groups = new HashSet<>();
            for (String groupId : groupIds) {
                Group group = loadGroup(groupId).join();
                if (group != null)
                    groups.add(group);
            }
            return groups;
        }, executor);
    }

    // ==================== Utility Operations ====================

    @Override
    public @NotNull CompletableFuture<Void> initialize() {
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public @NotNull CompletableFuture<Void> shutdown() {
        return CompletableFuture.runAsync(() -> {
            executor.shutdown();
            log.info("Storage shutdown completed");
        }, executor);
    }

}
