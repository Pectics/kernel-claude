/*
 * Based on LuckPerms' WildcardProcessor
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.calculator;

import com.google.common.collect.ImmutableMap;
import me.pectics.kernelclaude.perms.node.Node;
import me.pectics.kernelclaude.perms.node.types.PermissionNode;
import me.pectics.kernelclaude.perms.types.Tristate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Processor that handles wildcard permission matching.
 *
 * <p>Supports two types of wildcards:</p>
 * <ul>
 *   <li>{@code *} - matches all permissions</li>
 *   <li>{@code prefix.*} - matches all permissions starting with prefix.</li>
 * </ul>
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>{@code *} grants all permissions</li>
 *   <li>{@code admin.*} grants admin.create, admin.delete, admin.user.ban, etc.</li>
 *   <li>{@code -admin.*} denies all admin permissions (takes precedence)</li>
 * </ul>
 */
public class WildcardProcessor implements PermissionProcessor {

    private static final char WILDCARD = '*';
    private static final char DOT = '.';
    private static final String ROOT_WILDCARD = "*";

    private Map<String, Tristate> source = Map.of();
    private Map<String, Tristate> result = Map.of();

    // Wildcard state tracking
    private volatile boolean hasRootWildcard = false;
    private volatile Tristate rootWildcardValue = Tristate.UNDEFINED;
    private final Map<String, Tristate> wildcardMap = new ConcurrentHashMap<>();

    // Source nodes for recalculation
    private @Unmodifiable Collection<PermissionNode> nodes = List.of();

    @Override
    public void setSource(@NotNull Map<String, Tristate> source) {
        this.source = source;
    }

    /**
     * Sets the source permission nodes for more efficient wildcard detection.
     *
     * @param nodes the permission nodes
     */
    public void setNodes(@NotNull @Unmodifiable Collection<PermissionNode> nodes) {
        this.nodes = nodes;
        recalculateWildcards();
    }

    private void recalculateWildcards() {
        hasRootWildcard = false;
        rootWildcardValue = Tristate.UNDEFINED;
        wildcardMap.clear();

        for (PermissionNode node : nodes) {
            String key = node.getKey();
            Tristate value = Tristate.fromBoolean(node.getValue());

            if (key.equals(ROOT_WILDCARD)) {
                hasRootWildcard = true;
                rootWildcardValue = value;
            } else if (key.endsWith(".*")) {
                String prefix = key.substring(0, key.length() - 2);
                wildcardMap.put(prefix, value);
            }
        }
    }

    @Override
    public @NotNull Map<String, Tristate> getResult() {
        return result;
    }

    @Override
    public void process() {
        Map<String, Tristate> processed = new HashMap<>(source);

        // Apply root wildcard if present
        if (hasRootWildcard && rootWildcardValue != Tristate.UNDEFINED) {
            // Root wildcard grants/denies everything not already defined
            for (Map.Entry<String, Tristate> entry : source.entrySet()) {
                if (entry.getValue() == Tristate.UNDEFINED) {
                    processed.put(entry.getKey(), rootWildcardValue);
                }
            }
        }

        // Apply prefix wildcards
        for (Map.Entry<String, Tristate> wildcard : wildcardMap.entrySet()) {
            String prefix = wildcard.getKey();
            Tristate wildcardValue = wildcard.getValue();

            if (wildcardValue == Tristate.UNDEFINED) {
                continue;
            }

            // Apply to all permissions matching this prefix
            for (String permission : source.keySet()) {
                if (matchesPrefix(permission, prefix)) {
                    Tristate current = processed.get(permission);
                    if (current == Tristate.UNDEFINED) {
                        processed.put(permission, wildcardValue);
                    }
                }
            }
        }

        this.result = processed;
    }

    /**
     * Checks if a permission matches a wildcard prefix.
     *
     * @param permission the permission to check
     * @param prefix the wildcard prefix (without the .*)
     * @return true if matches
     */
    private boolean matchesPrefix(@NotNull String permission, @NotNull String prefix) {
        // Exact match with prefix (permission = prefix)
        if (permission.equals(prefix)) {
            return true;
        }
        // Starts with prefix. (permission = prefix.something)
        if (permission.startsWith(prefix + DOT)) {
            return true;
        }
        return false;
    }

    /**
     * Checks if a given permission would be granted by any wildcard.
     *
     * @param permission the permission to check
     * @return the wildcard that grants it, or null
     */
    public @NotNull Tristate checkWildcard(@NotNull String permission) {
        // Check root wildcard first
        if (hasRootWildcard) {
            return rootWildcardValue;
        }

        // Check prefix wildcards (longest match wins)
        String bestMatch = null;
        Tristate bestValue = Tristate.UNDEFINED;

        for (Map.Entry<String, Tristate> entry : wildcardMap.entrySet()) {
            String prefix = entry.getKey();
            if (matchesPrefix(permission, prefix)) {
                // Longer prefix = more specific = higher priority
                if (bestMatch == null || prefix.length() > bestMatch.length()) {
                    bestMatch = prefix;
                    bestValue = entry.getValue();
                }
            }
        }

        return bestValue;
    }

    @Override
    public void onNodeAdd(@NotNull Node node) {
        // Recalculate wildcards when a permission node is added
        recalculateWildcards();
    }

    @Override
    public void onNodeRemove(@NotNull Node node) {
        // Recalculate wildcards when a permission node is removed
        recalculateWildcards();
    }

    @Override
    public void invalidate() {
        this.result = Map.of();
        this.hasRootWildcard = false;
        this.rootWildcardValue = Tristate.UNDEFINED;
        this.wildcardMap.clear();
    }

    /**
     * Gets whether a root wildcard exists.
     *
     * @return true if root wildcard present
     */
    public boolean hasRootWildcard() {
        return hasRootWildcard;
    }

    /**
     * Gets all registered wildcard prefixes.
     *
     * @return the wildcard map
     */
    public @NotNull @Unmodifiable Map<String, Tristate> getWildcardMap() {
        return ImmutableMap.copyOf(wildcardMap);
    }
}
