/*
 * User node entity for database persistence
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.perms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Entity class representing a user's permission node in the database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNodeEntity {

    /**
     * The auto-generated ID.
     */
    private Integer id;

    /**
     * The user ID this node belongs to.
     */
    private @NotNull String userId;

    /**
     * The node key (permission, inheritance, meta, etc.).
     */
    private @NotNull String nodeKey;

    /**
     * The node value (true = granted, false = denied).
     */
    private boolean nodeValue;

    /**
     * The expiry timestamp in epoch seconds (0 = no expiry).
     */
    private long expireAt;

    /**
     * The serialized context set (JSON).
     */
    private @NotNull String contexts;

}
