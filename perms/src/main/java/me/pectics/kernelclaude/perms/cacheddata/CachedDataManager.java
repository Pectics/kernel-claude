/*
 * Based on LuckPerms' CachedDataManager
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.cacheddata;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import me.pectics.kernelclaude.perms.calculator.PermissionCalculator;
import me.pectics.kernelclaude.perms.context.ContextSatisfyMode;
import me.pectics.kernelclaude.perms.context.ContextSet;
import me.pectics.kernelclaude.perms.context.ImmutableContextSet;
import me.pectics.kernelclaude.perms.model.PermissionHolder;
import me.pectics.kernelclaude.perms.node.Node;
import me.pectics.kernelclaude.perms.node.NodeType;
import me.pectics.kernelclaude.perms.node.types.MetaNode;
import me.pectics.kernelclaude.perms.node.types.PermissionNode;
import me.pectics.kernelclaude.perms.types.Tristate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages cached permission and meta data for a PermissionHolder.
 *
 * <p>This class provides efficient access to permission calculations and
 * meta data lookups by maintaining caches that can be invalidated when
 * the underlying data changes.</p>
 */
public class CachedDataManager {

    private final PermissionHolder holder;

    // Permission calculator
    private final PermissionCalculator permissionCalculator;

    // Meta data cache
    private final Map<ImmutableContextSet, Map<String, String>> metaCache = new ConcurrentHashMap<>();

    // Flattened permission nodes for calculator
    private volatile @Unmodifiable Collection<PermissionNode> permissionNodes = List.of();

    // Flattened meta nodes for cache
    private volatile @Unmodifiable Collection<MetaNode> metaNodes = List.of();

    /**
     * Creates a new CachedDataManager for the given holder.
     *
     * @param holder the permission holder
     */
    public CachedDataManager(@NotNull PermissionHolder holder) {
        this.holder = holder;
        this.permissionCalculator = new PermissionCalculator();
    }

    /**
     * Recalculates and caches data from the holder's nodes.
     *
     * <p>This should be called when the holder's data changes significantly
     * or when initially loading the holder.</p>
     */
    public void recalculate() {
        // Get all inherited nodes
        Collection<Node> allNodes = holder.getNodes();

        // Separate by type
        List<PermissionNode> perms = new ArrayList<>();
        List<MetaNode> metas = new ArrayList<>();

        for (Node node : allNodes) {
            if (node.getType() == NodeType.PERMISSION)
                perms.add(NodeType.PERMISSION.cast(node));
            else if (node.getType() == NodeType.META)
                metas.add(NodeType.META.cast(node));
        }

        this.permissionNodes = ImmutableList.copyOf(perms);
        this.metaNodes = ImmutableList.copyOf(metas);

        // Update calculator
        permissionCalculator.setSourceNodes(permissionNodes);

        // Invalidate meta cache
        metaCache.clear();
    }

    /**
     * Invalidates all cached data.
     */
    public void invalidate() {
        permissionCalculator.invalidateCache();
        metaCache.clear();
    }

    // ==================== Permission Methods ====================

    /**
     * Checks a permission in the given context.
     *
     * @param permission the permission to check
     * @param context the query context
     * @return the result
     */
    public @NotNull Tristate checkPermission(@NotNull String permission, @NotNull ContextSet context) {
        return permissionCalculator.check(permission, context);
    }

    /**
     * Checks a permission with empty context.
     *
     * @param permission the permission to check
     * @return the result
     */
    public @NotNull Tristate checkPermission(@NotNull String permission) {
        return checkPermission(permission, ImmutableContextSet.empty());
    }

    /**
     * Gets all calculated permissions for a context.
     *
     * @param context the query context
     * @return map of permission to value
     */
    public @NotNull @Unmodifiable Map<String, Tristate> getPermissions(@NotNull ContextSet context) {
        return permissionCalculator.calculate(context);
    }

    /**
     * Gets all granted permissions for a context.
     *
     * @param context the query context
     * @return set of granted permission strings
     */
    public @NotNull Set<String> getGrantedPermissions(@NotNull ContextSet context) {
        return permissionCalculator.getGrantedPermissions(context);
    }

    /**
     * Gets the underlying permission calculator.
     *
     * @return the calculator
     */
    public @NotNull PermissionCalculator getPermissionCalculator() {
        return permissionCalculator;
    }

    // ==================== Meta Methods ====================

    /**
     * Gets a meta value for the given key in the given context.
     *
     * @param key the meta key
     * @param context the query context
     * @return the meta value, or null if not set
     */
    public @Nullable String getMetaValue(@NotNull String key, @NotNull ContextSet context) {
        ImmutableContextSet immutableContext = context.immutableCopy();

        // Check cache
        Map<String, String> cached = metaCache.computeIfAbsent(immutableContext, k -> new HashMap<>());
        if (cached.containsKey(key))
            return cached.get(key);

        // Calculate
        String value = calculateMetaValue(key, context);
        cached.put(key, value);
        return value;
    }

    /**
     * Gets a meta value with empty context.
     *
     * @param key the meta key
     * @return the meta value, or null if not set
     */
    public @Nullable String getMetaValue(@NotNull String key) {
        return getMetaValue(key, ImmutableContextSet.empty());
    }

    /**
     * Gets all meta key-value pairs for a context.
     *
     * @param context the query context
     * @return map of meta keys to values
     */
    public @NotNull @Unmodifiable Map<String, String> getMeta(@NotNull ContextSet context) {
        ImmutableContextSet immutableContext = context.immutableCopy();

        Map<String, String> result = metaCache.get(immutableContext);
        if (result != null)
            return result;

        // Calculate all meta
        result = calculateAllMeta(context);
        metaCache.put(immutableContext, result);
        return ImmutableMap.copyOf(result);
    }

    private @Nullable String calculateMetaValue(@NotNull String key, @NotNull ContextSet context) {
        String value = null;

        for (MetaNode node : metaNodes)
            if (node.getMetaKey().equals(key))
                if (node.getContexts().satisfies(context, ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY))
                    // Later nodes override earlier ones
                    value = node.getMetaValue();

        return value;
    }

    private @NotNull Map<String, String> calculateAllMeta(@NotNull ContextSet context) {
        Map<String, String> result = new HashMap<>();

        for (MetaNode node : metaNodes)
            if (node.getContexts().satisfies(context, ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY))
                // Later nodes override earlier ones
                result.put(node.getMetaKey(), node.getMetaValue());

        return result;
    }

    // ==================== Node Change Notifications ====================

    /**
     * Notifies the manager that a node was added.
     *
     * @param node the added node
     */
    public void onNodeAdd(@NotNull Node node) {
        permissionCalculator.onNodeAdd(node);

        if (node.getType() == NodeType.META)
            metaCache.clear();
    }

    /**
     * Notifies the manager that a node was removed.
     *
     * @param node the removed node
     */
    public void onNodeRemove(@NotNull Node node) {
        permissionCalculator.onNodeRemove(node);

        if (node.getType() == NodeType.META)
            metaCache.clear();
    }

    // ==================== Utility Methods ====================

    /**
     * Gets the holder this manager is for.
     *
     * @return the holder
     */
    public @NotNull PermissionHolder getHolder() {
        return holder;
    }

}
