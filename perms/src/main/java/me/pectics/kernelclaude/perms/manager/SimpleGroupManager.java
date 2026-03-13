/*
 * Simple Group Manager
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.manager;

import lombok.RequiredArgsConstructor;
import me.pectics.kernelclaude.perms.model.Group;
import me.pectics.kernelclaude.perms.model.SimpleGroup;
import me.pectics.kernelclaude.perms.storage.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple implementation of GroupManager with in-memory caching.
 */
@RequiredArgsConstructor
public class SimpleGroupManager implements GroupManager {

    private final @NotNull Storage storage;

    private final Map<String, Group> groupCache = new ConcurrentHashMap<>();

    @Override
    public @NotNull CompletableFuture<@Nullable Group> loadGroup(@NotNull String groupId) {
        // Check cache first
        Group cached = groupCache.get(groupId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        // Load from storage
        return storage.loadGroup(groupId).thenApply(group -> {
            if (group != null) {
                // Set up resolver for inheritance
                if (group instanceof SimpleGroup simpleGroup) {
                    simpleGroup.setGroupResolver(this::resolveGroup);
                }
                groupCache.put(groupId, group);
            }
            return group;
        });
    }

    @Override
    public @Nullable Group getCachedGroup(@NotNull String groupId) {
        return groupCache.get(groupId);
    }

    @Override
    public @NotNull Collection<Group> getCachedGroups() {
        return List.copyOf(groupCache.values());
    }

    @Override
    public @NotNull CompletableFuture<Void> saveGroup(@NotNull Group group) {
        return storage.saveGroup(group).thenRun(() -> {
            // Update cache
            groupCache.put(group.getGroupId(), group);

            // Invalidate caches for groups that might inherit from this one
            invalidateRelatedCaches(group.getGroupId());
        });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Group> createGroup(@NotNull String groupId) {
        return storage.createGroup(groupId).thenApply(group -> {
            // Set up resolver
            if (group instanceof SimpleGroup simpleGroup)
                simpleGroup.setGroupResolver(this::resolveGroup);

            groupCache.put(groupId, group);
            return group;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> deleteGroup(@NotNull String groupId) {
        return storage.deleteGroup(groupId).thenApply(deleted -> {
            if (deleted) {
                Group removed = groupCache.remove(groupId);
                // Invalidate caches for all groups (they might have inherited from this one)
                invalidateRelatedCaches(groupId);
            }
            return deleted;
        });
    }

    /**
     * Loads all groups from storage into cache.
     *
     * @return a future completing when all groups are loaded
     */
    public @NotNull CompletableFuture<Void> loadAllGroups() {
        return storage.loadAllGroups().thenAccept(groups -> {
            for (Group group : groups) {
                if (group instanceof SimpleGroup simpleGroup)
                    simpleGroup.setGroupResolver(this::resolveGroup);

                groupCache.put(group.getGroupId(), group);
            }
        });
    }

    /**
     * Resolves a group by groupId (synchronous, cache-only).
     *
     * @param groupId the group groupId
     * @return the group, or null if not cached
     */
    private @Nullable Group resolveGroup(@NotNull String groupId) {
        return groupCache.get(groupId);
    }

    /**
     * Invalidates caches for groups that might inherit from the given group.
     *
     * @param groupId the group name
     */
    private void invalidateRelatedCaches(@NotNull String groupId) {
        for (Group group : groupCache.values())
            if (group instanceof SimpleGroup simpleGroup)
                simpleGroup.invalidateCache();
    }

    /**
     * Invalidates all group caches.
     */
    private void invalidateAllCaches() {
        for (Group group : groupCache.values())
            if (group instanceof SimpleGroup simpleGroup)
                simpleGroup.invalidateCache();
    }

    /**
     * Clears the group cache.
     */
    public void clearCache() {
        groupCache.clear();
    }

}
