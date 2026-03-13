/*
 * Based on LuckPerms' User
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a user (player) that holds permission data.
 */
public interface User extends PermissionHolder {

    /**
     * Gets the unique ID of this user.
     *
     * @return the unique ID
     */
    @NotNull String getUniqueId();

    /**
     * Gets the platform this user belongs to.
     *
     * <p>Examples: "telegram", "discord", "minecraft"</p>
     *
     * @return the platform
     */
    @NotNull String getPlatform();

    /**
     * Gets the native ID from the platform.
     *
     * @return the native ID
     */
    @NotNull String getNativeId();

    /**
     * Gets the username of this user, if known.
     *
     * @return the username, or null if not known
     */
    @Nullable String getUsername();

    /**
     * Sets the username.
     *
     * @param username the username
     */
    void setUsername(@Nullable String username);

    /**
     * Gets the primary group of this user.
     *
     * @return the primary group name
     */
    @NotNull String getPrimaryGroup();

    /**
     * Sets the primary group.
     *
     * @param groupName the group name
     * @return the result
     * @throws IllegalStateException if the user is not a member of that group
     */
    @NotNull DataMutateResult setPrimaryGroup(@NotNull String groupName);

    /**
     * Gets the friendly name for this user.
     *
     * <p>Returns the username if known, otherwise the unique ID.</p>
     *
     * @return the friendly name
     */
    @Override
    default @NotNull String getFriendlyName() {
        String username = getUsername();
        return username != null ? username : getUniqueId();
    }

    /**
     * Gets the unique identifier for this user.
     *
     * @return the unique ID
     */
    @Override
    default @NotNull String getIdentifier() {
        return getUniqueId();
    }

    /**
     * Computes a unique ID from platform and native ID.
     *
     * @param platform the platform
     * @param nativeId the native ID
     * @return a unique ID
     */
    static @NotNull String computeId(@NotNull String platform, @NotNull String nativeId) {
        // Use a deterministic hash to create a unique ID
        int hash = (platform + ":" + nativeId).hashCode();
        return String.format("%s-%08x", platform.toLowerCase(), hash & 0xFFFFFFFFL);
    }
}
