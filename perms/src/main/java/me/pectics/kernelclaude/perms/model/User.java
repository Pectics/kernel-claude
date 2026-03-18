/*
 * Based on LuckPerms' User
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

import lombok.SneakyThrows;
import lombok.val;
import me.pectics.kernelclaude.uuid.UUIDGenerator;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Represents a user (player) that holds permission data.
 */
public non-sealed interface User extends PermissionHolder {

    /**
     * Gets the user ID of this user.
     *
     * @return the user ID
     */
    @NotNull UUID getUserId();

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
     * Gets the unique identifier for this user.
     *
     * @return the unique ID
     */
    @Override
    default @NotNull String getIdentifier() {
        return getUserId().toString();
    }

    /**
     * Computes a unique ID from platform and native ID.
     *
     * @param platform the platform
     * @param nativeId the native ID
     * @return a unique ID
     */
    @SneakyThrows
    static @NotNull UUID computeId(@NotNull String platform, @NotNull String nativeId) {
        val input = "user_id?platform=" + platform + "&native_id=" + nativeId;
        return UUIDGenerator.generate(input);
    }

}
