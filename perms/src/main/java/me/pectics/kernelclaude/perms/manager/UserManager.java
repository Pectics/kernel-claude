/*
 * User Manager
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.manager;

import me.pectics.kernelclaude.perms.model.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * Manager for loading and caching users.
 */
public interface UserManager {

    /**
     * Loads a user by platform and native ID.
     *
     * @param platform the platform
     * @param nativeId the native ID
     * @return the user, or null if not found
     */
    @NotNull CompletableFuture<@Nullable User> loadUser(@NotNull String platform, @NotNull String nativeId);

    /**
     * Gets a cached user by unique ID.
     *
     * @param uniqueId the unique ID
     * @return the user, or null if not cached
     */
    @Nullable User getCachedUser(@NotNull String uniqueId);

    /**
     * Saves a user.
     *
     * @param user the user to save
     * @return a future completing when saved
     */
    @NotNull CompletableFuture<Void> saveUser(@NotNull User user);

    /**
     * Unloads a user from cache.
     *
     * @param uniqueId the unique ID
     */
    void unloadUser(@NotNull String uniqueId);

    /**
     * Gets all cached users.
     *
     * @return collection of cached users
     */
    @NotNull Iterable<User> getCachedUsers();
}
