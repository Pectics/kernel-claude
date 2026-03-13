/*
 * Based on LuckPerms' User
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

import me.pectics.kernelclaude.perms.context.ContextSet;
import me.pectics.kernelclaude.perms.context.ContextSatisfyMode;
import me.pectics.kernelclaude.perms.context.ImmutableContextSet;
import me.pectics.kernelclaude.perms.node.Node;
import me.pectics.kernelclaude.perms.node.NodeType;
import me.pectics.kernelclaude.perms.node.types.InheritanceNode;
import me.pectics.kernelclaude.perms.types.Tristate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import com.google.common.collect.ImmutableList;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Basic implementation of User.
 */
public class SimpleUser implements User {

    private final String uniqueId;
    private final String platform;
    private final String nativeId;
    private volatile String username;
    private volatile String primaryGroup = "default";

    private final NodeMap normalData;
    private final NodeMap transientData;

    // Caches
    private final Map<ImmutableContextSet, Collection<Group>> inheritanceCache = new ConcurrentHashMap<>();
    private final Map<ImmutableContextSet, Collection<Node>> inheritedNodesCache = new ConcurrentHashMap<>();

    // Resolvers
    private volatile GroupResolver groupResolver;
    private volatile PrimaryGroupValidator primaryGroupValidator;

    /**
     * Functional interface for resolving groups by name.
     */
    @FunctionalInterface
    public interface GroupResolver {
        @Nullable Group resolve(@NotNull String name);
    }

    /**
     * Functional interface for validating primary group membership.
     */
    @FunctionalInterface
    public interface PrimaryGroupValidator {
        boolean isMemberOf(@NotNull User user, @NotNull String groupName);
    }

    public SimpleUser(@NotNull String platform, @NotNull String nativeId) {
        this.uniqueId = computeId(platform, nativeId);
        this.platform = platform;
        this.nativeId = nativeId;
        this.normalData = new SimpleNodeMap();
        this.transientData = new SimpleNodeMap();
    }

    public SimpleUser(@NotNull String uniqueId, @NotNull String platform, @NotNull String nativeId) {
        this.uniqueId = uniqueId;
        this.platform = platform;
        this.nativeId = nativeId;
        this.normalData = new SimpleNodeMap();
        this.transientData = new SimpleNodeMap();
    }

    /**
     * Computes a unique ID from platform and native ID.
     */
    private static String computeId(String platform, String nativeId) {
        int hash = (platform + ":" + nativeId).hashCode();
        return String.format("%s-%08x", platform.toLowerCase(), hash & 0xFFFFFFFFL);
    }

    public void setGroupResolver(@Nullable GroupResolver resolver) {
        this.groupResolver = resolver;
    }

    public void setPrimaryGroupValidator(@Nullable PrimaryGroupValidator validator) {
        this.primaryGroupValidator = validator;
    }

    @Override
    public @NotNull String getUniqueId() {
        return uniqueId;
    }

    @Override
    public @NotNull String getPlatform() {
        return platform;
    }

    @Override
    public @NotNull String getNativeId() {
        return nativeId;
    }

    @Override
    public @Nullable String getUsername() {
        return username;
    }

    @Override
    public void setUsername(@Nullable String username) {
        this.username = username;
    }

    @Override
    public @NotNull String getPrimaryGroup() {
        return primaryGroup;
    }

    @Override
    public @NotNull DataMutateResult setPrimaryGroup(@NotNull String groupName) {
        // Validate that user is a member of the group
        if (primaryGroupValidator != null) {
            if (!primaryGroupValidator.isMemberOf(this, groupName)) {
                throw new IllegalStateException("User is not a member of group: " + groupName);
            }
        }
        this.primaryGroup = groupName;
        return DataMutateResult.SUCCESS;
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
        ImmutableContextSet immutableContext = context.immutableCopy();

        // Check cache
        Collection<Node> cached = inheritedNodesCache.get(immutableContext);
        if (cached != null) {
            return cached;
        }

        Set<Node> result = new LinkedHashSet<>();

        // Add own nodes first (transient has priority)
        addNodesWithContext(result, transientData.toCollection(), context);
        addNodesWithContext(result, normalData.toCollection(), context);

        // Add inherited nodes from groups
        for (Group group : getInheritedGroups(context)) {
            addNodesWithContext(result, group.resolveInheritedNodes(context), context);
        }

        Collection<Node> immutableResult = ImmutableList.copyOf(result);
        inheritedNodesCache.put(immutableContext, immutableResult);
        return immutableResult;
    }

    private void addNodesWithContext(Set<Node> result, Collection<Node> nodes, ContextSet queryContext) {
        for (Node node : nodes) {
            if (queryContext.satisfies(node.getContexts(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)) {
                result.add(node);
            }
        }
    }

    @Override
    public @NotNull @Unmodifiable Collection<Group> getInheritedGroups(@NotNull ContextSet context) {
        ImmutableContextSet immutableContext = context.immutableCopy();

        // Check cache
        Collection<Group> cached = inheritanceCache.get(immutableContext);
        if (cached != null) {
            return cached;
        }

        if (groupResolver == null) {
            return ImmutableList.of();
        }

        Set<Group> inherited = new LinkedHashSet<>();

        // Get inheritance nodes
        Collection<InheritanceNode> inheritanceNodes = getNodes().stream()
                .filter(NodeType.INHERITANCE::matches)
                .map(NodeType.INHERITANCE::cast)
                .filter(node -> node.getContexts().satisfies(context, ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY))
                .collect(ImmutableList.toImmutableList());

        // Add primary group first
        Group primary = groupResolver.resolve(primaryGroup);
        if (primary != null) {
            inherited.add(primary);
            inherited.addAll(primary.getInheritedGroups(context));
        }

        // Add other groups from inheritance nodes
        for (InheritanceNode node : inheritanceNodes) {
            Group group = groupResolver.resolve(node.getGroupName());
            if (group != null && !inherited.contains(group)) {
                inherited.add(group);
                inherited.addAll(group.getInheritedGroups(context));
            }
        }

        Collection<Group> result = ImmutableList.copyOf(inherited);
        inheritanceCache.put(immutableContext, result);
        return result;
    }

    @Override
    public @NotNull Tristate checkPermission(@NotNull String permission, @NotNull ContextSet context) {
        Collection<Node> nodes = resolveInheritedNodes(context);

        // Check direct permission
        for (Node node : nodes) {
            if (node.getType() == NodeType.PERMISSION && node.matchesKey(permission)) {
                if (context.satisfies(node.getContexts(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)) {
                    return Tristate.of(node.getValue());
                }
            }
        }

        // Check wildcard permissions
        for (Node node : nodes) {
            if (node.getType() == NodeType.PERMISSION && node.getValue()) {
                if (matchesWildcard(permission, node.getKey())) {
                    if (context.satisfies(node.getContexts(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)) {
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
        Instant now = Instant.now();

        normalData.clear(node -> node.hasExpiry() && node.getExpiry() != null && node.getExpiry().isBefore(now));
        transientData.clear(node -> node.hasExpiry() && node.getExpiry() != null && node.getExpiry().isBefore(now));

        // Invalidate caches
        inheritanceCache.clear();
        inheritedNodesCache.clear();
    }

    /**
     * Invalidates all caches.
     */
    public void invalidateCache() {
        inheritanceCache.clear();
        inheritedNodesCache.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleUser that = (SimpleUser) o;
        return uniqueId.equals(that.uniqueId);
    }

    @Override
    public int hashCode() {
        return uniqueId.hashCode();
    }

    @Override
    public String toString() {
        return "SimpleUser{" +
                "uniqueId='" + uniqueId + '\'' +
                ", platform='" + platform + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
