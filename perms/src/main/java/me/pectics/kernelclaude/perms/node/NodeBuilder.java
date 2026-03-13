/*
 * Based on LuckPerms' NodeBuilder implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.node;

import me.pectics.kernelclaude.perms.context.ContextSet;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

/**
 * Builder for creating nodes.
 *
 * @param <N> the node type
 * @param <B> the builder type (self-referencing)
 */
public interface NodeBuilder<N extends Node, B extends NodeBuilder<N, B>> {

    /**
     * Sets the value of the node.
     *
     * @param value the value (true = granted, false = denied)
     * @return this builder
     */
    @NotNull B value(boolean value);

    /**
     * Sets the node to be negated (value = false).
     *
     * @return this builder
     */
    default @NotNull B negated() {
        return value(false);
    }

    /**
     * Sets the expiry time.
     *
     * @param expiry the expiry instant, or null for permanent
     * @return this builder
     */
    @NotNull B expiry(@NotNull Instant expiry);

    /**
     * Sets the expiry time using a duration from now.
     *
     * @param duration the duration until expiry
     * @return this builder
     */
    @NotNull B expiry(@NotNull Duration duration);

    /**
     * Sets the expiry time using epoch seconds.
     *
     * @param epochSeconds the epoch seconds, or 0 for permanent
     * @return this builder
     */
    @NotNull B expiry(long epochSeconds);

    /**
     * Removes the expiry time (makes the node permanent).
     *
     * @return this builder
     */
    @NotNull B clearExpiry();

    /**
     * Sets the contexts for this node.
     *
     * @param contexts the context set
     * @return this builder
     */
    @NotNull B context(@NotNull ContextSet contexts);

    /**
     * Adds a context entry.
     *
     * @param key the context key
     * @param value the context value
     * @return this builder
     */
    @NotNull B withContext(@NotNull String key, @NotNull String value);

    /**
     * Adds a context entry.
     *
     * @param context the context
     * @return this builder
     */
    @NotNull B withContext(@NotNull me.pectics.kernelclaude.perms.context.Context context);

    /**
     * Builds the node.
     *
     * @return the built node
     * @throws IllegalStateException if required fields are not set
     */
    @NotNull N build();
}
