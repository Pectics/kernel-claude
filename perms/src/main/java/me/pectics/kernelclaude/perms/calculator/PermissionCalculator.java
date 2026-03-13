/*
 * Based on LuckPerms' PermissionCalculator
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.calculator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import me.pectics.kernelclaude.perms.context.ContextSatisfyMode;
import me.pectics.kernelclaude.perms.context.ContextSet;
import me.pectics.kernelclaude.perms.context.ImmutableContextSet;
import me.pectics.kernelclaude.perms.node.Node;
import me.pectics.kernelclaude.perms.node.NodeType;
import me.pectics.kernelclaude.perms.node.types.PermissionNode;
import me.pectics.kernelclaude.perms.types.Tristate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Calculates permission results using a chain of processors.
 *
 * <p>The calculator processes nodes through a chain:</p>
 * <ol>
 *   <li>{@link DirectProcessor} - handles exact permission matches</li>
 *   <li>{@link WildcardProcessor} - handles wildcard patterns (*, prefix.*)</li>
 * </ol>
 *
 * <p>Results are cached by context for efficient repeated queries.</p>
 */
public class PermissionCalculator {

    private final List<PermissionProcessor> processors;
    private final WildcardProcessor wildcardProcessor;

    // Source data
    private @Unmodifiable Collection<PermissionNode> sourceNodes = List.of();

    // Cache by context
    private final Map<ImmutableContextSet, Map<String, Tristate>> cache = new ConcurrentHashMap<>();

    /**
     * Creates a new permission calculator with the default processor chain.
     */
    public PermissionCalculator() {
        this.processors = new ArrayList<>();

        // Create processors in order
        var directProcessor = new DirectProcessor();
        this.wildcardProcessor = new WildcardProcessor();

        processors.add(directProcessor);
        processors.add(wildcardProcessor);
    }

    /**
     * Sets the source permission nodes.
     *
     * @param nodes the nodes
     */
    public void setSourceNodes(@NotNull @Unmodifiable Collection<PermissionNode> nodes) {
        this.sourceNodes = nodes;
        this.wildcardProcessor.setNodes(nodes);
        invalidateCache();
    }

    /**
     * Calculates all permission values for a given context.
     *
     * @param context the query context
     * @return map of permission to tristate result
     */
    public @NotNull @Unmodifiable Map<String, Tristate> calculate(@NotNull ContextSet context) {
        ImmutableContextSet immutableContext = context.immutableCopy();

        // Check cache
        Map<String, Tristate> cached = cache.get(immutableContext);
        if (cached != null)
            return cached;

        // Filter nodes by context and build source map
        Map<String, Tristate> sourceMap = new HashMap<>();
        for (PermissionNode node : sourceNodes) {
            if (node.getContexts().satisfies(context, ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY)) {
                String key = node.getKey();
                Tristate value = Tristate.fromBoolean(node.getValue());

                // Later values override earlier ones (for same key)
                sourceMap.put(key, value);
            }
        }

        // Process through chain
        Map<String, Tristate> current = sourceMap;
        for (PermissionProcessor processor : processors) {
            processor.setSource(current);
            processor.process();
            current = processor.getResult();
        }

        // Cache and return
        Map<String, Tristate> result = ImmutableMap.copyOf(current);
        cache.put(immutableContext, result);
        return result;
    }

    /**
     * Checks a specific permission in the given context.
     *
     * @param permission the permission to check
     * @param context the query context
     * @return the result
     */
    public @NotNull Tristate check(@NotNull String permission, @NotNull ContextSet context) {
        Map<String, Tristate> results = calculate(context);

        // Check direct result
        Tristate result = results.get(permission);
        if (result != null)
            return result;

        // Check wildcard
        return wildcardProcessor.checkWildcard(permission);
    }

    /**
     * Gets all granted permissions for a context.
     *
     * @param context the query context
     * @return set of granted permission strings
     */
    public @NotNull Set<String> getGrantedPermissions(@NotNull ContextSet context) {
        Map<String, Tristate> results = calculate(context);
        Set<String> granted = new HashSet<>();

        for (Map.Entry<String, Tristate> entry : results.entrySet())
            if (entry.getValue() == Tristate.TRUE)
                granted.add(entry.getKey());

        return ImmutableSet.copyOf(granted);
    }

    /**
     * Invalidates all cached data.
     */
    public void invalidateCache() {
        cache.clear();
        for (PermissionProcessor processor : processors)
            processor.invalidate();
    }

    /**
     * Notifies the calculator that a node was added.
     *
     * @param node the added node
     */
    public void onNodeAdd(@NotNull Node node) {
        if (node.getType() == NodeType.PERMISSION) {
            for (PermissionProcessor processor : processors)
                processor.onNodeAdd(node);
            cache.clear();
        }
    }

    /**
     * Notifies the calculator that a node was removed.
     *
     * @param node the removed node
     */
    public void onNodeRemove(@NotNull Node node) {
        if (node.getType() == NodeType.PERMISSION) {
            for (PermissionProcessor processor : processors)
                processor.onNodeRemove(node);
            cache.clear();
        }
    }

    /**
     * Gets the wildcard processor for direct access.
     *
     * @return the wildcard processor
     */
    public @NotNull WildcardProcessor getWildcardProcessor() {
        return wildcardProcessor;
    }

    /**
     * Gets all processors in the chain.
     *
     * @return the processor list
     */
    public @NotNull @Unmodifiable List<PermissionProcessor> getProcessors() {
        return ImmutableList.copyOf(processors);
    }
}
