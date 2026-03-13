/*
 * Storage interface for permission data
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.storage;

import me.pectics.kernelclaude.perms.model.Group;
import me.pectics.kernelclaude.perms.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Storage interface for loading and saving permission data.
 */
public interface Storage {

    // ==================== User Operations ====================

    /**
     * Loads a user by unique ID.
     *
     * @param uniqueId the unique ID
     * @return the user, or null if not found
     */
    @NotNull CompletableFuture<@Nullable User> loadUser(@NotNull String uniqueId);

    /**
     * Loads a user by platform and native ID.
     *
     * @param platform the platform
     * @param nativeId the native ID
     * @return the user, or null if not found
     */
    @NotNull CompletableFuture<@Nullable User> loadUser(@NotNull String platform, @NotNull String nativeId);

    /**
     * Saves a user.
     *
     * @param user the user to save
     * @return a future that completes when done
     */
    @NotNull CompletableFuture<Void> saveUser(@NotNull User user);

    /**
     * Gets all known user IDs.
     *
     * @return a set of unique IDs
     */
    @NotNull CompletableFuture<@NotNull Set<String>> getAllUserIds();

    // ==================== Group Operations ====================

    /**
     * Loads a group by name.
     *
     * @param name the group name
     * @return the group, or null if not found
     */
    @NotNull CompletableFuture<@Nullable Group> loadGroup(@NotNull String name);

    /**
     * Saves a group.
     *
     * @param group the group to save
     * @return a future that completes when done
     */
    @NotNull CompletableFuture<Void> saveGroup(@NotNull Group group);

    /**
     * Creates a new group.
     *
     * @param name the group name
     * @return the created group
     */
    @NotNull CompletableFuture<@NotNull Group> createGroup(@NotNull String name);

    /**
     * Deletes a group.
     *
     * @param name the group name
     * @return true if deleted
     */
    @NotNull CompletableFuture<Boolean> deleteGroup(@NotNull String name);

    /**
     * Gets all group names.
     *
     * @return a set of group names
     */
    @NotNull CompletableFuture<@NotNull Set<String>> getAllGroupNames();

    /**
     * Loads all groups.
     *
     * @return a collection of all groups
     */
    @NotNull CompletableFuture<@NotNull Collection<Group>> loadAllGroups();

    // ==================== Utility Operations ====================

    /**
     * Initializes the storage.
     */
    @NotNull CompletableFuture<Void> initialize();

    /**
     * Shuts down the storage.
     */
    @NotNull CompletableFuture<Void> shutdown();
}
