/*
 * Based on LuckPerms' ContextSet implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A set of contexts (key-value pairs) representing environmental conditions.
 *
 * <p>Context sets are used to determine when a permission or node should apply.
 * A permission node with contexts will only be active when the query context
 * satisfies all of its required contexts.</p>
 *
 * <p>Two implementations are provided:</p>
 * <ul>
 *     <li>{@link MutableContextSet} - allows modification after creation</li>
 *     <li>{@link ImmutableContextSet} - cannot be modified after creation</li>
 * </ul>
 */
public interface ContextSet extends Iterable<Context> {

    /**
     * Gets if this context set is immutable.
     *
     * @return true if immutable
     */
    boolean isImmutable();

    /**
     * Returns an immutable copy of this context set.
     *
     * @return an immutable copy
     */
    @NotNull ImmutableContextSet immutableCopy();

    /**
     * Returns a mutable copy of this context set.
     *
     * @return a mutable copy
     */
    @NotNull MutableContextSet mutableCopy();

    /**
     * Returns a set of all context entries.
     *
     * @return an immutable set of contexts
     */
    @NotNull @Unmodifiable Set<Context> toSet();

    /**
     * Returns a map representation of this context set.
     *
     * <p>Note: a key can map to multiple values, so this returns
     * {@code Map<String, Set<String>>}.</p>
     *
     * @return an immutable map
     */
    @NotNull @Unmodifiable Map<String, Set<String>> asMap();

    /**
     * Returns an iterator over all context entries.
     *
     * @return an iterator
     */
    @Override
    @NotNull @Unmodifiable Iterator<Context> iterator();

    /**
     * Checks if this set contains at least one value for the given key.
     *
     * @param key the key to check
     * @return true if the key exists
     */
    boolean containsKey(@NotNull String key);

    /**
     * Gets all values for a given key.
     *
     * @param key the key to look up
     * @return a set of values (may be empty)
     */
    @NotNull @Unmodifiable Set<String> getValues(@NotNull String key);

    /**
     * Gets any value for a given key.
     *
     * @param key the key to look up
     * @return an optional containing any matching value
     */
    default @NotNull Optional<String> getAnyValue(@NotNull String key) {
        return getValues(key).stream().findAny();
    }

    /**
     * Checks if this set contains a specific key-value pair.
     *
     * @param key the key
     * @param value the value
     * @return true if the pair exists
     */
    boolean contains(@NotNull String key, @NotNull String value);

    /**
     * Checks if this set contains a specific context.
     *
     * @param context the context to check
     * @return true if the context exists
     */
    default boolean contains(@NotNull Context context) {
        return contains(context.getKey(), context.getValue());
    }

    /**
     * Checks if this set contains any of the given values for the specified key.
     *
     * @param key the key to check
     * @param values the values to check
     * @return true if any of the key-value pairs exist
     */
    default boolean containsAny(@NotNull String key, @NotNull Iterable<String> values) {
        for (String value : values)
            if (contains(key, value))
                return true;

        return false;
    }

    /**
     * Checks if this context set is "satisfied" by another set.
     *
     * <p>Uses {@link ContextSatisfyMode#ANY_VALUE_MATCH_PER_KEY} mode.</p>
     *
     * @param other the other context set
     * @return true if satisfied
     */
    default boolean isSatisfiedBy(@NotNull ContextSet other) {
        return isSatisfiedBy(other, ContextSatisfyMode.ANY_VALUE_MATCH_PER_KEY);
    }

    /**
     * Checks if this context set is "satisfied" by another set.
     *
     * @param other the other context set
     * @param mode the satisfaction mode
     * @return true if satisfied
     */
    boolean isSatisfiedBy(@NotNull ContextSet other, @NotNull ContextSatisfyMode mode);

    /**
     * Checks if this context set satisfies another set (inverse of isSatisfiedBy).
     *
     * @param other the other context set
     * @param mode the satisfaction mode
     * @return true if this satisfies the other set
     */
    default boolean satisfies(@NotNull ContextSet other, @NotNull ContextSatisfyMode mode) {
        return other.isSatisfiedBy(this, mode);
    }

    /**
     * Checks if this set is empty.
     *
     * @return true if empty
     */
    boolean isEmpty();

    /**
     * Gets the number of context entries.
     *
     * @return the size
     */
    int size();
}
