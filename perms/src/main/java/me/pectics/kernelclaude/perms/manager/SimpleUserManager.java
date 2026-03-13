/*
 * Simple User Manager
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.manager;

import lombok.val;
import me.pectics.kernelclaude.perms.model.User;
import me.pectics.kernelclaude.perms.model.SimpleUser;
import me.pectics.kernelclaude.perms.node.types.InheritanceNode;
import me.pectics.kernelclaude.perms.storage.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple implementation of UserManager with in-memory caching.
 */
public class SimpleUserManager implements UserManager {

    private final Storage storage;
    private final GroupManager groupManager;

    // Cache by unique ID
    private final ConcurrentHashMap<String, User> userCacheById = new ConcurrentHashMap<>();

    // Cache by platform:nativeId for quick lookup
    private final ConcurrentHashMap<String, String> idMapping = new ConcurrentHashMap<>();

    public SimpleUserManager(@NotNull Storage storage, @NotNull GroupManager groupManager) {
        this.storage = storage;
        this.groupManager = groupManager;
    }

    @Override
    public @NotNull CompletableFuture<@Nullable User> loadUser(@NotNull String platform, @NotNull String nativeId) {
        String uniqueId = User.computeId(platform, nativeId);

        // Check cache first
        User cached = userCacheById.get(uniqueId);
        if (cached != null)
            return CompletableFuture.completedFuture(cached);

        // Load from storage
        return storage.loadUser(platform, nativeId).thenApply(user -> {
            if (user != null) {
                setupUser(user);
                userCacheById.put(user.getUserId(), user);
                idMapping.put(platform + ":" + nativeId, user.getUserId());
            }
            return user;
        });
    }

    /**
     * Gets a cached user by platform and native ID.
     *
     * @param platform the platform
     * @param nativeId the native ID
     * @return the user, or null if not cached
     */
    @Override
    public @Nullable User getCachedUser(@NotNull String platform, @NotNull String nativeId) {
        val userId = idMapping.get(platform + ":" + nativeId);
        if (userId == null)
            return null;
        // Read cache
        return userCacheById.get(userId);
    }

    @Override
    public @NotNull CompletableFuture<Void> saveUser(@NotNull User user) {
        return storage.saveUser(user).thenRun(() -> {
            // Update cache
            userCacheById.put(user.getUserId(), user);
            idMapping.put(user.getPlatform() + ":" + user.getNativeId(), user.getUserId());
        });
    }

    @Override
    public void unloadUser(@NotNull String platform, @NotNull String nativeId) {
        val userId = idMapping.get(platform + ":" + nativeId);
        if (userId == null)
            return;
        // Drop cache
        userCacheById.remove(userId);
        idMapping.remove(platform + ":" + nativeId);
    }

    @Override
    public @NotNull Iterable<User> getCachedUsers() {
        return userCacheById.values();
    }

    /**
     * Sets up a user with resolvers and validators.
     *
     * @param user the user to set up
     */
    private void setupUser(@NotNull User user) {
        if (user instanceof SimpleUser simpleUser) {
            // Set group resolver
            simpleUser.setGroupResolver(groupManager::getCachedGroup);

            // Set primary group validator
            simpleUser.setPrimaryGroupValidator((u, groupName) -> {
                // Check if user has inheritance node for this group
                return u.getNodes().stream()
                        .anyMatch(node -> {
                            if (node instanceof InheritanceNode inheritanceNode)
                                return inheritanceNode.getGroupName().equals(groupName);
                            return false;
                        });
            });
        }
    }

    /**
     * Clears the user cache.
     */
    public void clearCache() {
        userCacheById.clear();
        idMapping.clear();
    }

}
