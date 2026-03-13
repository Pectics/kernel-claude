/*
 * Based on LuckPerms' DataType
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

/**
 * Represents a type of data stored in a permission holder.
 */
public enum DataType {

    /**
     * Normal data - persisted to storage.
     */
    NORMAL,

    /**
     * Transient data - only exists for the current session, not saved to storage.
     */
    TRANSIENT
}
