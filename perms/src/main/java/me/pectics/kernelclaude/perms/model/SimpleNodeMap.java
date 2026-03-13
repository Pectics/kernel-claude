/*
 * Based on LuckPerms' NodeMap
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import me.pectics.kernelclaude.perms.context.ContextSet;
import me.pectics.kernelclaude.perms.context.ImmutableContextSet;
import me.pectics.kernelclaude.perms.node.Node;
import me.pectics.kernelclaude.perms.node.NodeEqualityPredicate;
import me.pectics.kernelclaude.perms.types.Tristate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Thread-safe implementation of NodeMap.
 */
public class SimpleNodeMap implements NodeMap {

    private final Map<ImmutableContextSet, List<Node>> nodesByContext = new ConcurrentHashMap<>();

    @Override
    public @NotNull @Unmodifiable Map<ImmutableContextSet, Collection<Node>> toMap() {
        ImmutableMap.Builder<ImmutableContextSet, Collection<Node>> builder = ImmutableMap.builder();
        for (Map.Entry<ImmutableContextSet, List<Node>> entry : nodesByContext.entrySet()) {
            builder.put(entry.getKey(), ImmutableList.copyOf(entry.getValue()));
        }
        return builder.build();
    }

    @Override
    public @NotNull @Unmodifiable Collection<Node> toCollection() {
        return nodesByContext.values().stream()
                .flatMap(List::stream)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    public @NotNull Tristate contains(@NotNull Node node, @NotNull NodeEqualityPredicate predicate) {
        ImmutableContextSet contexts = node.getContexts();

        // Check specific context first
        List<Node> nodesInContext = nodesByContext.get(contexts);
        if (nodesInContext != null)
            for (Node existing : nodesInContext)
                if (predicate.areEqual(node, existing))
                    return Tristate.fromBoolean(existing.getValue());

        // Check if any context contains the node
        for (List<Node> nodes : nodesByContext.values())
            for (Node existing : nodes)
                if (predicate.areEqual(node, existing))
                    return Tristate.fromBoolean(existing.getValue());

        return Tristate.UNDEFINED;
    }

    @Override
    public @NotNull DataMutateResult add(@NotNull Node node) {
        ImmutableContextSet contexts = node.getContexts();
        List<Node> nodesInContext = nodesByContext.computeIfAbsent(
                contexts,
                _ -> new CopyOnWriteArrayList<>());

        // Check for exact duplicate
        for (Node existing : nodesInContext)
            if (NodeEqualityPredicate.EXACT.areEqual(node, existing))
                return DataMutateResult.FAIL_ALREADY_EXISTS;

        nodesInContext.add(node);
        return DataMutateResult.SUCCESS;
    }

    @Override
    public @NotNull DataMutateResult remove(@NotNull Node node) {
        ImmutableContextSet contexts = node.getContexts();
        List<Node> nodesInContext = nodesByContext.get(contexts);

        if (nodesInContext == null)
            return DataMutateResult.FAIL_DOES_NOT_EXIST;

        // Use removeIf to avoid CopyOnWriteArrayList iterator.remove() issue
        AtomicBoolean removed = new AtomicBoolean(false);
        nodesInContext.removeIf(existing -> {
            if (NodeEqualityPredicate.EXACT.areEqual(node, existing)) {
                removed.set(true);
                return true;
            }
            return false;
        });

        if (removed.get()) {
            // Clean up empty context lists
            if (nodesInContext.isEmpty())
                nodesByContext.remove(contexts);

            return DataMutateResult.SUCCESS;
        }

        return DataMutateResult.FAIL_DOES_NOT_EXIST;
    }

    @Override
    public void clear() {
        nodesByContext.clear();
    }

    @Override
    public void clear(@NotNull Predicate<? super Node> test) {
        Set<ImmutableContextSet> toRemove = new HashSet<>();

        for (Map.Entry<ImmutableContextSet, List<Node>> entry : nodesByContext.entrySet()) {
            List<Node> nodes = entry.getValue();
            nodes.removeIf(test);

            if (nodes.isEmpty())
                toRemove.add(entry.getKey());
        }

        // Remove empty context entries
        for (ImmutableContextSet key : toRemove)
            nodesByContext.remove(key);
    }

    @Override
    public void clear(@NotNull ContextSet contextSet) {
        ImmutableContextSet immutable = contextSet.immutableCopy();
        nodesByContext.remove(immutable);
    }

    @Override
    public void clear(@NotNull ContextSet contextSet, @NotNull Predicate<? super Node> test) {
        ImmutableContextSet immutable = contextSet.immutableCopy();
        List<Node> nodes = nodesByContext.get(immutable);

        if (nodes != null) {
            nodes.removeIf(test);
            if (nodes.isEmpty())
                nodesByContext.remove(immutable);
        }
    }

    @Override
    public int size() {
        return nodesByContext.values().stream()
                .mapToInt(List::size)
                .sum();
    }

    @Override
    public boolean isEmpty() {
        return nodesByContext.isEmpty() || nodesByContext.values().stream()
                .allMatch(List::isEmpty);
    }
}
