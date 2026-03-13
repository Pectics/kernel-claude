/*
 * Based on LuckPerms' NodeType implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.node;

import me.pectics.kernelclaude.perms.node.types.InheritanceNode;
import me.pectics.kernelclaude.perms.node.types.MetaNode;
import me.pectics.kernelclaude.perms.node.types.PermissionNode;
import me.pectics.kernelclaude.perms.node.types.WeightNode;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 * Represents a type of node.
 *
 * @param <T> the node class type
 */
public final class NodeType<T extends Node> {

    /**
     * Standard permission node type.
     */
    public static final NodeType<PermissionNode> PERMISSION =
            new NodeType<>("permission", PermissionNode.class, n -> n instanceof PermissionNode);

    /**
     * Group inheritance node type.
     */
    public static final NodeType<InheritanceNode> INHERITANCE =
            new NodeType<>("inheritance", InheritanceNode.class, n -> n instanceof InheritanceNode);

    /**
     * Meta key-value node type.
     */
    public static final NodeType<MetaNode> META =
            new NodeType<>("meta", MetaNode.class, n -> n instanceof MetaNode);

    /**
     * Group weight node type.
     */
    public static final NodeType<WeightNode> WEIGHT =
            new NodeType<>("weight", WeightNode.class, n -> n instanceof WeightNode);

    private final String name;
    private final Class<T> nodeClass;
    private final Predicate<Node> matchesPredicate;

    private NodeType(String name, Class<T> nodeClass, Predicate<Node> matchesPredicate) {
        this.name = name;
        this.nodeClass = nodeClass;
        this.matchesPredicate = matchesPredicate;
    }

    /**
     * Gets the name of this node type.
     *
     * @return the name
     */
    public @NotNull String getName() {
        return this.name;
    }

    /**
     * Gets the node class for this type.
     *
     * @return the node class
     */
    public @NotNull Class<T> getNodeClass() {
        return this.nodeClass;
    }

    /**
     * Checks if a node is of this type.
     *
     * @param node the node to check
     * @return true if matches
     */
    public boolean matches(@NotNull Node node) {
        return this.matchesPredicate.test(node);
    }

    /**
     * Casts a node to this type.
     *
     * @param node the node to cast
     * @return the cast node
     * @throws ClassCastException if the node is not of this type
     */
    @SuppressWarnings("unchecked")
    public @NotNull T cast(@NotNull Node node) {
        if (!matches(node)) {
            throw new ClassCastException("Node " + node + " is not of type " + this.name);
        }
        return (T) node;
    }

    @Override
    public String toString() {
        return "NodeType(" + this.name + ")";
    }
}
