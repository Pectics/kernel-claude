/*
 * User entity for database persistence
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.perms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Entity class representing a user in the database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    /**
     * The unique ID (MD5 hash).
     */
    private @NotNull String userId;

    /**
     * The platform (e.g., "telegram", "discord").
     */
    private @NotNull String platform;

    /**
     * The native ID from the platform.
     */
    private @NotNull String nativeId;

    /**
     * The primary group name.
     */
    private @NotNull String primaryGroup;

}
