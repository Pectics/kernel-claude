/*
 * Based on LuckPerms' Tristate implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.types;

import org.jetbrains.annotations.NotNull;

/**
 * Represents three different states of a permission check.
 *
 * <ul>
 *     <li>{@link #TRUE} - permission is granted</li>
 *     <li>{@link #FALSE} - permission is explicitly denied (negated)</li>
 *     <li>{@link #UNDEFINED} - permission is not set</li>
 * </ul>
 */
public enum Tristate {

    /**
     * A value indicating the permission is granted
     */
    TRUE(true),

    /**
     * A value indicating the permission is explicitly denied
     */
    FALSE(false),

    /**
     * A value indicating the permission is not set
     */
    UNDEFINED(false);

    private final boolean booleanValue;

    Tristate(boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    /**
     * Returns a {@link Tristate} from a boolean.
     *
     * @param value the boolean value
     * @return {@link #TRUE} or {@link #FALSE}
     */
    public static @NotNull Tristate of(boolean value) {
        return value ? TRUE : FALSE;
    }

    /**
     * Alias for {@link #of(boolean)}.
     *
     * @param value the boolean value
     * @return {@link #TRUE} or {@link #FALSE}
     */
    public static @NotNull Tristate fromBoolean(boolean value) {
        return of(value);
    }

    /**
     * Returns a {@link Tristate} from a nullable boolean.
     *
     * <p>Unlike {@link #of(boolean)}, this returns {@link #UNDEFINED}
     * if the value is null.</p>
     *
     * @param value the boolean value (nullable)
     * @return {@link #UNDEFINED}, {@link #TRUE} or {@link #FALSE}
     */
    public static @NotNull Tristate of(Boolean value) {
        return value == null ? UNDEFINED : value ? TRUE : FALSE;
    }

    /**
     * Returns the value as a boolean.
     *
     * <p>{@link #UNDEFINED} converts to false.</p>
     *
     * @return a boolean representation
     */
    public boolean asBoolean() {
        return this.booleanValue;
    }

    /**
     * Returns true if this is {@link #TRUE}.
     *
     * @return true if TRUE
     */
    public boolean isTrue() {
        return this == TRUE;
    }

    /**
     * Returns true if this is {@link #FALSE}.
     *
     * @return true if FALSE
     */
    public boolean isFalse() {
        return this == FALSE;
    }

    /**
     * Returns true if this is {@link #UNDEFINED}.
     *
     * @return true if UNDEFINED
     */
    public boolean isUndefined() {
        return this == UNDEFINED;
    }

    /**
     * Returns the opposite of this Tristate.
     *
     * <p>TRUE becomes FALSE, FALSE becomes TRUE, UNDEFINED stays UNDEFINED.</p>
     *
     * @return the negated value
     */
    public @NotNull Tristate negated() {
        return switch (this) {
            case TRUE -> FALSE;
            case FALSE -> TRUE;
            case UNDEFINED -> UNDEFINED;
        };
    }
}
