/*
 * Based on LuckPerms' PermissionHolder
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

import com.google.common.collect.ImmutableList;
import me.pectics.kernelclaude.perms.context.ContextSet;
import me.pectics.kernelclaude.perms.node.Node;
import me.pectics.kernelclaude.perms.node.NodeType;
import me.pectics.kernelclaude.perms.types.Tristate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.SortedSet;

/**
 * Represents an object that holds permissions.
 *
 * <p>A permission holder can be a {@link User} or a {@link Group}.</p>
 */
public interface PermissionHolder {

    /**
     * Gets the unique identifier for this holder.
     *
     * @return the identifier
     */
    @NotNull String getIdentifier();

    /**
     * Gets a friendly name for this holder.
     *
     * @return a friendly name for display
     */
    @NotNull String getFriendlyName();

    /**
     * Gets the node map for a specific data type.
     *
     * @param dataType the data type
     * @return the node map
     */
    @NotNull NodeMap getData(@NotNull DataType dataType);

    /**
     * Gets the normal (persisted) data.
     *
     * @return the normal node map
     */
    default @NotNull NodeMap data() {
        return getData(DataType.NORMAL);
    }

    /**
     * Gets the transient (session-only) data.
     *
     * @return the transient node map
     */
    default @NotNull NodeMap transientData() {
        return getData(DataType.TRANSIENT);
    }

    /**
     * Gets all nodes from both normal and transient data.
     *
     * <p>May contain duplicates if the same node exists in both.</p>
     *
     * @return a collection of nodes
     */
    @NotNull @Unmodifiable Collection<Node> getNodes();

    /**
     * Gets all nodes of a specific type.
     *
     * @param type the node type
     * @return a collection of nodes
     */
    default <T extends Node> @NotNull @Unmodifiable Collection<T> getNodes(@NotNull NodeType<T> type) {
        return getNodes().stream()
                .filter(type::matches)
                .map(type::cast)
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Gets all distinct nodes (no duplicates).
     *
     * @return a sorted set of nodes
     */
    @NotNull @Unmodifiable SortedSet<Node> getDistinctNodes();

    /**
     * Resolves all inherited nodes for a given context.
     *
     * @param context the query context
     * @return a collection of inherited nodes
     */
    @NotNull @Unmodifiable Collection<Node> resolveInheritedNodes(@NotNull ContextSet context);

    /**
     * Resolves inherited nodes of a specific type.
     *
     * @param type the node type
     * @param context the query context
     * @return a collection of inherited nodes
     */
    default <T extends Node> @NotNull @Unmodifiable Collection<T> resolveInheritedNodes(
            @NotNull NodeType<T> type, @NotNull ContextSet context) {
        return resolveInheritedNodes(context).stream()
                .filter(type::matches)
                .map(type::cast)
                .collect(ImmutableList.toImmutableList());
    }

    /**
     * Gets all groups this holder inherits from.
     *
     * @param context the query context
     * @return a collection of inherited groups
     */
    @NotNull @Unmodifiable Collection<Group> getInheritedGroups(@NotNull ContextSet context);

    /**
     * Checks a permission in the given context.
     *
     * @param permission the permission to check
     * @param context the query context
     * @return the result
     */
    @NotNull Tristate checkPermission(@NotNull String permission, @NotNull ContextSet context);

    /**
     * Checks a permission with an empty context.
     *
     * @param permission the permission to check
     * @return the result
     */
    default @NotNull Tristate checkPermission(@NotNull String permission) {
        return checkPermission(permission, me.pectics.kernelclaude.perms.context.ImmutableContextSet.empty());
    }

    /**
     * Removes any expired temporary nodes.
     */
    void auditTemporaryNodes();
}
