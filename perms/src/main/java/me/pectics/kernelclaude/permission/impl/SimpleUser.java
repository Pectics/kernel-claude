package me.pectics.kernelclaude.permission.impl;

import lombok.Getter;
import lombok.Setter;
import me.pectics.kernelclaude.permission.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User 接口的简单实现
 */
public class SimpleUser implements User {

    private final @Getter @NotNull String id;
    private final @Getter @NotNull String platform;
    private final @Getter @NotNull String nativeId;
    private @Getter @Setter @NotNull String displayName;

    private final Set<PermissionNode> permissionNodes = ConcurrentHashMap.newKeySet();
    private final Set<Group> groups = ConcurrentHashMap.newKeySet();

    private @Setter @Nullable GroupManager groupManager;

    public SimpleUser(@NotNull String platform,
                      @NotNull String nativeId,
                      @NotNull String displayName) {
        this.id = User.generateId(platform, nativeId);
        this.platform = platform;
        this.nativeId = nativeId;
        this.displayName = displayName;
    }

    public SimpleUser(@NotNull String platform, @NotNull String nativeId) {
        this(platform, nativeId, nativeId);
    }

    // PermissionHolder 接口实现

    @Override
    public @NotNull Set<PermissionNode> getPermissionNodes() {
        return Set.copyOf(permissionNodes);
    }

    @Override
    public void addPermissionNode(@NotNull PermissionNode node) {
        permissionNodes.add(node);
    }

    @Override
    public void removePermissionNode(@NotNull String key) {
        permissionNodes.removeIf(n -> key.equals(n.key()));
    }

    @Override
    public @NotNull Set<Group> getSuperGroups() {
        return Set.copyOf(groups);
    }

    @Override
    public void addSuperGroup(@NotNull String groupId) {
        if (groupManager == null)
            throw new IllegalStateException("GroupManager is not set for user: " + id);
        Group group = groupManager.getGroup(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found: " + groupId));
        groups.add(group);
    }

    @Override
    public void removeSuperGroup(@NotNull String groupId) {
        groups.removeIf(g -> groupId.equals(g.getId()));
    }

    @Override
    public boolean hasSuperGroup(@NotNull String groupId) {
        return groups.stream().anyMatch(g -> groupId.equals(g.getId()));
    }

}
