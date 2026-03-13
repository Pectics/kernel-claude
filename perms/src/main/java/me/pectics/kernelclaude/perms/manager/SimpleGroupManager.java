/*
 * Simple Group Manager
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.manager;

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
public class SimpleGroupManager implements GroupManager {

    private final Storage storage;
    private final Map<String, Group> groupCache = new ConcurrentHashMap<>();

    public SimpleGroupManager(@NotNull Storage storage) {
        this.storage = storage;
    }

    @Override
    public @NotNull CompletableFuture<@Nullable Group> loadGroup(@NotNull String name) {
        // Check cache first
        Group cached = groupCache.get(name);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }

        // Load from storage
        return storage.loadGroup(name).thenApply(group -> {
            if (group != null) {
                // Set up resolver for inheritance
                if (group instanceof SimpleGroup simpleGroup) {
                    simpleGroup.setGroupResolver(this::resolveGroup);
                }
                groupCache.put(name, group);
            }
            return group;
        });
    }

    @Override
    public @Nullable Group getCachedGroup(@NotNull String name) {
        return groupCache.get(name);
    }

    @Override
    public @NotNull Collection<Group> getCachedGroups() {
        return List.copyOf(groupCache.values());
    }

    @Override
    public @NotNull CompletableFuture<Void> saveGroup(@NotNull Group group) {
        return storage.saveGroup(group).thenRun(() -> {
            // Update cache
            groupCache.put(group.getName(), group);

            // Invalidate caches for groups that might inherit from this one
            invalidateRelatedCaches(group.getName());
        });
    }

    @Override
    public @NotNull CompletableFuture<@NotNull Group> createGroup(@NotNull String name) {
        return storage.createGroup(name).thenApply(group -> {
            // Set up resolver
            if (group instanceof SimpleGroup simpleGroup) {
                simpleGroup.setGroupResolver(this::resolveGroup);
            }
            groupCache.put(name, group);
            return group;
        });
    }

    @Override
    public @NotNull CompletableFuture<Boolean> deleteGroup(@NotNull String name) {
        return storage.deleteGroup(name).thenApply(deleted -> {
            if (deleted) {
                Group removed = groupCache.remove(name);
                // Invalidate caches for all groups (they might have inherited from this one)
                invalidateAllCaches();
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
                if (group instanceof SimpleGroup simpleGroup) {
                    simpleGroup.setGroupResolver(this::resolveGroup);
                }
                groupCache.put(group.getName(), group);
            }
        });
    }

    /**
     * Resolves a group by name (synchronous, cache-only).
     *
     * @param name the group name
     * @return the group, or null if not cached
     */
    private @Nullable Group resolveGroup(@NotNull String name) {
        return groupCache.get(name);
    }

    /**
     * Invalidates caches for groups that might inherit from the given group.
     *
     * @param groupName the group name
     */
    private void invalidateRelatedCaches(@NotNull String groupName) {
        for (Group group : groupCache.values()) {
            if (group instanceof SimpleGroup simpleGroup) {
                simpleGroup.invalidateCache();
            }
        }
    }

    /**
     * Invalidates all group caches.
     */
    private void invalidateAllCaches() {
        for (Group group : groupCache.values()) {
            if (group instanceof SimpleGroup simpleGroup) {
                simpleGroup.invalidateCache();
            }
        }
    }

    /**
     * Clears the group cache.
     */
    public void clearCache() {
        groupCache.clear();
    }
}
