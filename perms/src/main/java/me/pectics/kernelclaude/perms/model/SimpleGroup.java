/*
 * Based on LuckPerms' Group
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

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
public class SimpleGroup implements Group {

    private final String name;
    private volatile String displayName;
    private volatile OptionalInt weight = OptionalInt.empty();

    private final NodeMap normalData;
    private final NodeMap transientData;

    // Inheritance cache
    private final Map<ContextSet, Collection<Group>> inheritanceCache = new ConcurrentHashMap<>();

    // Reference to group manager for inheritance resolution
    private volatile GroupResolver groupResolver;

    /**
     * Functional interface for resolving groups by name.
     */
    @FunctionalInterface
    public interface GroupResolver {
        @Nullable Group resolve(@NotNull String name);
    }

    public SimpleGroup(@NotNull String name) {
        this.name = name;
        this.normalData = new SimpleNodeMap();
        this.transientData = new SimpleNodeMap();
    }

    public void setGroupResolver(@Nullable GroupResolver resolver) {
        this.groupResolver = resolver;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    @Override
    public @Nullable String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    @Override
    public @NotNull OptionalInt getWeight() {
        return weight;
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
        for (Group parent : getInheritedGroups(context)) {
            if (parent != this) {
                addNodesWithContext(result, parent.resolveInheritedNodes(context), context);
            }
        }

        return ImmutableList.copyOf(result);
    }

    private void addNodesWithContext(Set<Node> result, Collection<Node> nodes, ContextSet queryContext) {
        for (Node node : nodes) {
            if (node.getContexts().satisfies(queryContext, me.pectics.kernelclaude.perms.context.ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)) {
                result.add(node);
            }
        }
    }

    @Override
    public @NotNull @Unmodifiable Collection<Group> getInheritedGroups(@NotNull ContextSet context) {
        // Check cache
        Collection<Group> cached = inheritanceCache.get(context);
        if (cached != null) {
            return cached;
        }

        Set<Group> inherited = new LinkedHashSet<>();
        inherited.add(this); // Include self

        if (groupResolver == null) {
            return ImmutableList.copyOf(inherited);
        }

        // Get inheritance nodes
        Collection<InheritanceNode> inheritanceNodes = getNodes().stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .filter(node -> node.getContexts().satisfies(
                        context,
                        me.pectics.kernelclaude.perms.context.ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY
                ))
                .collect(ImmutableList.toImmutableList());

        // Resolve parent groups
        for (InheritanceNode node : inheritanceNodes) {
            Group parent = groupResolver.resolve(node.getGroupName());
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
        for (Node node : nodes) {
            if (node.getType() == NodeType.PERMISSION && node.matchesKey(permission)) {
                if (context.satisfies(node.getContexts(), me.pectics.kernelclaude.perms.context.ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)) {
                    return Tristate.fromBoolean(node.getValue());
                }
            }
        }

        // Check wildcard permissions
        for (Node node : nodes) {
            if (node.getType() == NodeType.PERMISSION && node.getValue()) {
                if (matchesWildcard(permission, node.getKey())) {
                    if (context.satisfies(node.getContexts(), me.pectics.kernelclaude.perms.context.ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)) {
                        return Tristate.TRUE;
                    }
                }
            }
        }

        return Tristate.UNDEFINED;
    }

    private boolean matchesWildcard(String permission, String wildcard) {
        if (wildcard.equals("*")) {
            return true;
        }
        if (wildcard.endsWith(".*")) {
            String prefix = wildcard.substring(0, wildcard.length() - 2);
            return permission.startsWith(prefix + ".");
        }
        return false;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleGroup that = (SimpleGroup) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "SimpleGroup{name='" + name + "'}";
    }
}
