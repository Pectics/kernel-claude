/*
 * Group entity for database persistence
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.perms.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * Entity class representing a group in the database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupEntity {

    /**
     * The group ID (name).
     */
    private @NotNull String groupId;

}
