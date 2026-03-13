/*
 * Group Manager
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.manager;

import me.pectics.kernelclaude.perms.model.Group;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * Manager for loading and caching groups.
 */
public interface GroupManager {

    /**
     * Loads a group by groupId.
     *
     * @param groupId the group groupId
     * @return the group, or null if not found
     */
    @NotNull CompletableFuture<@Nullable Group> loadGroup(@NotNull String groupId);

    /**
     * Gets a cached group by groupId.
     *
     * @param groupId the group groupId
     * @return the group, or null if not cached
     */
    @Nullable Group getCachedGroup(@NotNull String groupId);

    /**
     * Gets all cached groups.
     *
     * @return collection of cached groups
     */
    @NotNull Collection<Group> getCachedGroups();

    /**
     * Saves a group.
     *
     * @param group the group to save
     * @return a future completing when saved
     */
    @NotNull CompletableFuture<Void> saveGroup(@NotNull Group group);

    /**
     * Creates a new group.
     *
     * @param groupId the group groupId
     * @return the created group
     */
    @NotNull CompletableFuture<@NotNull Group> createGroup(@NotNull String groupId);

    /**
     * Deletes a group.
     *
     * @param groupId the group groupId
     * @return true if deleted
     */
    @NotNull CompletableFuture<Boolean> deleteGroup(@NotNull String groupId);
}
