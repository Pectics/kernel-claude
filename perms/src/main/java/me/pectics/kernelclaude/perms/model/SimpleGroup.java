/*
 * Based on LuckPerms' Group
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

import lombok.*;
import me.pectics.kernelclaude.perms.context.ContextSatisfyMode;
import me.pectics.kernelclaude.perms.context.ContextSet;
import me.pectics.kernelclaude.perms.node.Node;
import me.pectics.kernelclaude.perms.node.NodeType;
import me.pectics.kernelclaude.perms.node.types.InheritanceNode;
import me.pectics.kernelclaude.perms.types.Tristate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic implementation of Group.
 */
@EqualsAndHashCode(of = "groupId")
@ToString(of = "groupId")
public class SimpleGroup implements Group {

    private final @Getter @NotNull String groupId;

    private volatile @Getter @NotNull OptionalInt weight = OptionalInt.empty();

    private final NodeMap normalData = new SimpleNodeMap();
    private final NodeMap transientData = new SimpleNodeMap();

    // Inheritance cache
    private final Map<ContextSet, Collection<Group>> inheritanceCache = new ConcurrentHashMap<>();

    // Reference to group manager for inheritance resolution
    private volatile @Setter @Nullable GroupResolver groupResolver;

    /**
     * Functional interface for resolving groups by name.
     */
    @FunctionalInterface
    public interface GroupResolver {
        @Nullable Group resolve(@NotNull String groupId);
    }

    public SimpleGroup(@NotNull String groupId) {
        this.groupId = groupId;
    }

    @Override
    public void setWeight(int weight) {
        this.weight = OptionalInt.of(weight);
    }

    @Override
    public void clearWeight() {
        this.weight = OptionalInt.empty();
    }

    @Override
    public @NotNull NodeMap getData(@NotNull DataType dataType) {
        return switch (dataType) {
            case NORMAL -> normalData;
            case TRANSIENT -> transientData;
        };
    }

    @Override
    public @NotNull @Unmodifiable Collection<Node> getNodes() {
        return ImmutableList.<Node>builder()
                .addAll(normalData.toCollection())
                .addAll(transientData.toCollection())
                .build();
    }

    @Override
    public @NotNull @Unmodifiable SortedSet<Node> getDistinctNodes() {
        return new TreeSet<>(getNodes());
    }

    @Override
    public @NotNull @Unmodifiable Collection<Node> resolveInheritedNodes(@NotNull ContextSet context) {
        Set<Node> result = new LinkedHashSet<>();

        // Add own nodes first (transient has priority)
        addNodesWithContext(result, transientData.toCollection(), context);
        addNodesWithContext(result, normalData.toCollection(), context);

        // Add inherited nodes from parent groups
        for (Group parent : getInheritedGroups(context))
            if (parent != this)
                addNodesWithContext(result, parent.resolveInheritedNodes(context), context);

        return ImmutableList.copyOf(result);
    }

    private void addNodesWithContext(@NotNull Set<Node> result,
                                     @NotNull Collection<Node> nodes,
                                     @NotNull ContextSet queryContext) {
        for (Node node : nodes)
            if (node.getContexts().satisfies(queryContext, ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY))
                result.add(node);
    }

    @Override
    public @NotNull @Unmodifiable Collection<Group> getInheritedGroups(@NotNull ContextSet context) {
        // Check cache
        Collection<Group> cached = inheritanceCache.get(context);
        if (cached != null)
            return cached;

        Set<Group> inherited = new LinkedHashSet<>();
        inherited.add(this); // Include self

        if (groupResolver == null)
            return ImmutableList.copyOf(inherited);

        // Get inheritance nodes
        Collection<InheritanceNode> inheritanceNodes = getNodes().stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .filter(node -> node.getContexts().satisfies(
                        context,
                        ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY
                ))
                .collect(ImmutableList.toImmutableList());

        // Resolve parent groups
        for (InheritanceNode node : inheritanceNodes) {
            val gr = groupResolver;
            if (gr == null)
                continue;
            val parent = gr.resolve(node.getGroupName());
            if (parent != null && !inherited.contains(parent)) {
                inherited.add(parent);
                // Recursively get parent's inherited groups
                inherited.addAll(parent.getInheritedGroups(context));
            }
        }

        Collection<Group> result = ImmutableList.copyOf(inherited);
        inheritanceCache.put(context.immutableCopy(), result);
        return result;
    }

    @Override
    public @NotNull Tristate checkPermission(@NotNull String permission, @NotNull ContextSet context) {
        Collection<Node> nodes = resolveInheritedNodes(context);

        // Check direct permission
        for (Node node : nodes)
            if (node.getType() == NodeType.PERMISSION && node.matchesKey(permission))
                if (context.satisfies(node.getContexts(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY))
                    return Tristate.fromBoolean(node.getValue());

        // Check wildcard permissions
        for (Node node : nodes)
            if (node.getType() == NodeType.PERMISSION && node.getValue())
                if (PermissionHolder.matchPermissionWildcard(permission, node.getKey()))
                    if (context.satisfies(node.getContexts(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY))
                        return Tristate.TRUE;

        return Tristate.UNDEFINED;
    }

    @Override
    public void auditTemporaryNodes() {
        java.time.Instant now = java.time.Instant.now();

        normalData.clear(node -> node.hasExpiry() && node.getExpiry() != null && node.getExpiry().isBefore(now));
        transientData.clear(node -> node.hasExpiry() && node.getExpiry() != null && node.getExpiry().isBefore(now));

        // Invalidate cache
        inheritanceCache.clear();
    }

    /**
     * Invalidates the inheritance cache.
     */
    public void invalidateCache() {
        inheritanceCache.clear();
    }

}
