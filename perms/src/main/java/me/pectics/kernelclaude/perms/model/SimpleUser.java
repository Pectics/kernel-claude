/*
 * Based on LuckPerms' User
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
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
@EqualsAndHashCode(of = "userId")
@ToString(of = {"userId", "platform", "nativeId"})
public class SimpleUser implements User {

    private final @Getter @NotNull UUID userId;
    private final @Getter @NotNull String platform;
    private final @Getter @NotNull String nativeId;

    private volatile @Getter @NotNull String primaryGroup = "default";

    private final NodeMap normalData = new SimpleNodeMap();
    private final NodeMap transientData = new SimpleNodeMap();

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
        this.userId = User.computeId(platform, nativeId);
        this.platform = platform;
        this.nativeId = nativeId;
    }

    public void setGroupResolver(@Nullable GroupResolver resolver) {
        this.groupResolver = resolver;
    }

    public void setPrimaryGroupValidator(@Nullable PrimaryGroupValidator validator) {
        this.primaryGroupValidator = validator;
    }

    @Override
    public @NotNull DataMutateResult setPrimaryGroup(@NotNull String groupName) {
        // Validate that user is a member of the group
        if (primaryGroupValidator != null && !primaryGroupValidator.isMemberOf(this, groupName))
            throw new IllegalStateException("User <" + platform + ':' + nativeId + "> is not a member of group: " + groupName);

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
        if (cached != null)
            return cached;

        Set<Node> result = new LinkedHashSet<>();

        // Add own nodes first (transient has priority)
        addNodesWithContext(result, transientData.toCollection(), context);
        addNodesWithContext(result, normalData.toCollection(), context);

        // Add inherited nodes from groups
        for (Group group : getInheritedGroups(context))
            addNodesWithContext(result, group.resolveInheritedNodes(context), context);

        Collection<Node> immutableResult = ImmutableList.copyOf(result);
        inheritedNodesCache.put(immutableContext, immutableResult);
        return immutableResult;
    }

    private void addNodesWithContext(@NotNull Set<Node> result,
                                     @NotNull Collection<Node> nodes,
                                     @NotNull ContextSet queryContext) {
        for (Node node : nodes)
            if (queryContext.satisfies(node.getContexts(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY))
                result.add(node);
    }

    @Override
    public @NotNull @Unmodifiable Collection<Group> getInheritedGroups(@NotNull ContextSet context) {
        ImmutableContextSet immutableContext = context.immutableCopy();

        // Check cache
        Collection<Group> cached = inheritanceCache.get(immutableContext);
        if (cached != null)
            return cached;

        if (groupResolver == null)
            return ImmutableList.of();

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
        for (Node node : nodes)
            if (node.getType() == NodeType.PERMISSION && node.matchesKey(permission))
                if (context.satisfies(node.getContexts(), ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY))
                    return Tristate.of(node.getValue());

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
        Instant now = Instant.now();

        normalData.clear(node -> node.hasExpiry() && node.getExpiry() != null && node.getExpiry().isBefore(now));
        transientData.clear(node -> node.hasExpiry() && node.getExpiry() != null && node.getExpiry().isBefore(now));

        // Invalidate caches
        inheritanceCache.clear();
        inheritedNodesCache.clear();
    }

}
