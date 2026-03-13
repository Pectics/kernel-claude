/*
 * Based on LuckPerms' Node implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.node;

import me.pectics.kernelclaude.perms.context.ContextSet;
import me.pectics.kernelclaude.perms.context.ImmutableContextSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents a permission node - the core abstraction of the permission system.
 *
 * <p>A node encapsulates more than just permission assignments. Nodes are used to store:</p>
 * <ul>
 *     <li>Permissions (granted or denied)</li>
 *     <li>Inheritance relationships (group membership)</li>
 *     <li>Metadata (key-value pairs)</li>
 *     <li>Group weights</li>
 * </ul>
 *
 * <p>Nodes have the following key attributes:</p>
 * <ul>
 *     <li>{@link #getKey()} - the node key (permission string, group name, etc.)</li>
 *     <li>{@link #getValue()} - true for granted, false for denied</li>
 *     <li>{@link #getContexts()} - context conditions for the node to apply</li>
 *     <li>{@link #getExpiry()} - optional expiry time for temporary nodes</li>
 * </ul>
 */
public interface Node {

    /**
     * Gets the type of this node.
     *
     * @return the node type
     */
    @NotNull NodeType<?> getType();

    /**
     * Gets the key of this node.
     *
     * @return the key
     */
    @NotNull String getKey();

    /**
     * Gets the value of this node.
     *
     * <p>A value of false means the node is negated.</p>
     *
     * @return the value
     */
    boolean getValue();

    /**
     * Checks if this node is negated.
     *
     * @return true if negated
     */
    default boolean isNegated() {
        return !getValue();
    }

    /**
     * Gets the contexts required for this node to apply.
     *
     * @return the context set
     */
    @NotNull ImmutableContextSet getContexts();

    /**
     * Checks if this node has an expiry time.
     *
     * @return true if temporary
     */
    boolean hasExpiry();

    /**
     * Gets the expiry time of this node.
     *
     * @return the expiry instant, or null if permanent
     */
    @Nullable Instant getExpiry();

    /**
     * Gets the time until this node expires.
     *
     * @return the duration until expiry, or null if permanent
     */
    @Nullable Duration getExpiryDuration();

    /**
     * Checks if this node has expired.
     *
     * @return true if expired
     */
    boolean hasExpired();

    /**
     * Checks if this node matches the given permission key.
     *
     * <p>Supports wildcard matching:</p>
     * <ul>
     *     <li>{@code a.b.c} - exact match</li>
     *     <li>{@code a.b.*} - matches direct children (a.b.x)</li>
     *     <li>{@code a.b.**} - matches all descendants (a.b.x.y.z)</li>
     * </ul>
     *
     * @param key the key to match
     * @return true if matches
     */
    boolean matchesKey(@NotNull String key);

    /**
     * Checks if this node applies in the given context.
     *
     * @param context the context set
     * @return true if applicable
     */
    boolean appliesInContext(@NotNull ContextSet context);

    /**
     * Creates a builder pre-populated with this node's values.
     *
     * @return a builder
     */
    @NotNull NodeBuilder<?, ?> toBuilder();

    /**
     * Checks equality with another node using the default predicate.
     */
    @Override
    boolean equals(Object obj);

    /**
     * Checks equality with another node using a specific predicate.
     *
     * @param other the other node
     * @param predicate the equality predicate
     * @return true if equal
     */
    boolean equals(@NotNull Node other, @NotNull NodeEqualityPredicate predicate);
}
