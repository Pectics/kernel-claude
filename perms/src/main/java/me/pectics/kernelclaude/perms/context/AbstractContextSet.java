/*
 * Based on LuckPerms' AbstractContextSet
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.context;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Objects;

/**
 * Internal abstract base for context set implementations.
 * <p>
 * Uses the Template Method pattern: {@link #isSatisfiedBy} defines the skeleton algorithm,
 * and subclasses implement {@link #otherContainsAll} with their own iteration strategy.
 */
abstract class AbstractContextSet implements ContextSet {

    @Override
    public boolean isSatisfiedBy(@NotNull ContextSet other, @NotNull ContextSatisfyMode mode) {
        // Fast path for same instance
        if (this == other) {
            return true;
        }

        Objects.requireNonNull(other, "other");
        Objects.requireNonNull(mode, "mode");

        // Empty set is always satisfied
        if (this.isEmpty()) {
            return true;
        }

        // If this set isn't empty, but the other one is, then it can't be satisfied
        if (other.isEmpty()) {
            return false;
        }

        // Fast path for ALL_VALUES_PER_KEY: if this has more entries than other, impossible
        if (mode == ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY && this.size() > other.size()) {
            return false;
        }

        // Delegate to subclass implementation
        return otherContainsAll(other, mode);
    }

    /**
     * Checks if the other context set contains all required entries according to the mode.
     * Subclasses implement this with their own iteration strategy.
     *
     * @param other the other context set
     * @param mode the satisfaction mode
     * @return true if all required entries are contained
     */
    protected abstract boolean otherContainsAll(@NotNull ContextSet other, @NotNull ContextSatisfyMode mode);

    static String sanitizeKey(String key) {
        Preconditions.checkNotNull(key, "key is null");
        Preconditions.checkArgument(Context.isValidKey(key), "key is (effectively) empty");
        return key.toLowerCase(Locale.ROOT);
    }

    static String sanitizeValue(String value) {
        Preconditions.checkNotNull(value, "value is null");
        Preconditions.checkArgument(Context.isValidValue(value), "value is (effectively) empty");
        return value.toLowerCase(Locale.ROOT);
    }
}
