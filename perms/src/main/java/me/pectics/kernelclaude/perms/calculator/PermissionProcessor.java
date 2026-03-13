/*
 * Based on LuckPerms' PermissionProcessor
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.calculator;

import me.pectics.kernelclaude.perms.node.Node;
import me.pectics.kernelclaude.perms.types.Tristate;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * A processor in the permission calculation chain.
 *
 * <p>Processors are chained together to form a permission calculator.
 * Each processor can modify the result map based on its logic.</p>
 */
public interface PermissionProcessor {

    /**
     * Sets the source map of nodes to process.
     *
     * @param source the source map
     */
    void setSource(@NotNull Map<String, Tristate> source);

    /**
     * Gets the processed result map.
     *
     * @return the result map
     */
    @NotNull Map<String, Tristate> getResult();

    /**
     * Processes the source map and updates the result.
     */
    void process();

    /**
     * Notifies the processor that a node was added.
     *
     * @param node the added node
     */
    default void onNodeAdd(@NotNull Node node) {}

    /**
     * Notifies the processor that a node was removed.
     *
     * @param node the removed node
     */
    default void onNodeRemove(@NotNull Node node) {}

    /**
     * Invalidates any cached data.
     */
    default void invalidate() {}
}
