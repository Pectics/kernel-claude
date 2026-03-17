/*
 * Based on LuckPerms' User
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

import lombok.SneakyThrows;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Represents a user (player) that holds permission data.
 */
public non-sealed interface User extends PermissionHolder {

    /**
     * Gets the user ID of this user.
     *
     * @return the user ID
     */
    @NotNull String getUserId();

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
        return getUserId();
    }

    /**
     * Computes a unique ID from platform and native ID.
     *
     * @param platform the platform
     * @param nativeId the native ID
     * @return a unique ID
     */
    @SneakyThrows
    static @NotNull String computeId(@NotNull String platform, @NotNull String nativeId) {
        val input = "?platform=" + platform + "&native_id=" + nativeId;
        // Use md5 hash for better distribution
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest)
            hex.append(String.format("%02x", b));
        return hex.toString();
    }

}
