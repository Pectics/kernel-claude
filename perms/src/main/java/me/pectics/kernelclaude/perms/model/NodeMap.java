/*
 * Based on LuckPerms' NodeMap
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

import me.pectics.kernelclaude.perms.context.ContextSet;
import me.pectics.kernelclaude.perms.context.ImmutableContextSet;
import me.pectics.kernelclaude.perms.node.Node;
import me.pectics.kernelclaude.perms.node.NodeEqualityPredicate;
import me.pectics.kernelclaude.perms.types.Tristate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A map-like structure for storing nodes within a permission holder.
 *
 * <p>Changes to a NodeMap are not persisted unless explicitly saved.</p>
 */
public interface NodeMap {

    /**
     * Gets a map of contexts to their associated nodes.
     *
     * @return an immutable map
     */
    @NotNull @Unmodifiable Map<ImmutableContextSet, Collection<Node>> toMap();

    /**
     * Gets all nodes as a flat collection.
     *
     * @return an immutable collection
     */
    @NotNull @Unmodifiable Collection<Node> toCollection();

    /**
     * Checks if this map contains a node.
     *
     * @param node the node to check
     * @param predicate the equality predicate
     * @return the result as a tristate
     */
    @NotNull Tristate contains(@NotNull Node node, @NotNull NodeEqualityPredicate predicate);

    /**
     * Adds a node.
     *
     * @param node the node to add
     * @return the result of the operation
     */
    @NotNull DataMutateResult add(@NotNull Node node);

    /**
     * Removes a node.
     *
     * @param node the node to remove
     * @return the result of the operation
     */
    @NotNull DataMutateResult remove(@NotNull Node node);

    /**
     * Clears all nodes.
     */
    void clear();

    /**
     * Clears nodes matching a predicate.
     *
     * @param test the predicate
     */
    void clear(@NotNull Predicate<? super Node> test);

    /**
     * Clears all nodes in a specific context.
     *
     * @param contextSet the context
     */
    void clear(@NotNull ContextSet contextSet);

    /**
     * Clears nodes in a context matching a predicate.
     *
     * @param contextSet the context
     * @param test the predicate
     */
    void clear(@NotNull ContextSet contextSet, @NotNull Predicate<? super Node> test);

    /**
     * Gets the number of nodes.
     *
     * @return the size
     */
    int size();

    /**
     * Checks if the map is empty.
     *
     * @return true if empty
     */
    boolean isEmpty();
}
