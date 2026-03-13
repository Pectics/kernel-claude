/*
 * Based on LuckPerms' NodeEqualityPredicate implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.node;

import org.jetbrains.annotations.NotNull;

import java.util.function.BiPredicate;

/**
 * Defines how node equality is determined.
 */
public enum NodeEqualityPredicate implements BiPredicate<Node, Node> {

    /**
     * Compares key, value, expiry, and contexts.
     */
    EXACT("exact", (a, b) ->
            a.getKey().equals(b.getKey()) &&
            a.getValue() == b.getValue() &&
            equalsExpiry(a, b) &&
            a.getContexts().equals(b.getContexts())
    ),

    /**
     * Compares key and value only.
     */
    KEY_VALUE("key_value", (a, b) ->
            a.getKey().equals(b.getKey()) &&
            a.getValue() == b.getValue()
    ),

    /**
     * Compares key, value, and expiry.
     */
    KEY_VALUE_EXPIRY("key_value_expiry", (a, b) ->
            a.getKey().equals(b.getKey()) &&
            a.getValue() == b.getValue() &&
            equalsExpiry(a, b)
    ),

    /**
     * Compares key, value, and contexts (ignores expiry).
     */
    KEY_VALUE_CONTEXTS("key_value_contexts", (a, b) ->
            a.getKey().equals(b.getKey()) &&
            a.getValue() == b.getValue() &&
            a.getContexts().equals(b.getContexts())
    ),

    /**
     * Compares key only.
     */
    KEY("key", (a, b) ->
            a.getKey().equals(b.getKey())
    );

    private final String name;
    private final BiPredicate<Node, Node> predicate;

    NodeEqualityPredicate(String name, BiPredicate<Node, Node> predicate) {
        this.name = name;
        this.predicate = predicate;
    }

    /**
     * Gets the name of this predicate.
     *
     * @return the name
     */
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public boolean test(Node a, Node b) {
        return this.predicate.test(a, b);
    }

    private static boolean equalsExpiry(Node a, Node b) {
        if (a.hasExpiry() != b.hasExpiry()) {
            return false;
        }
        if (!a.hasExpiry()) {
            return true;
        }
        return a.getExpiry().equals(b.getExpiry());
    }

    /**
     * Compares two nodes using this predicate.
     *
     * @param a the first node
     * @param b the second node
     * @return true if equal according to this predicate
     */
    public boolean areEqual(@NotNull Node a, @NotNull Node b) {
        return test(a, b);
    }
}
