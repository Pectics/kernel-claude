/*
 * Based on LuckPerms' Context implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A context is a key-value pair representing an environmental condition
 * under which a permission or node applies.
 *
 * <p>For example, {@code platform=telegram} indicates a permission that only
 * applies on the Telegram platform.</p>
 *
 * <p>Context keys and values are case-insensitive and will be normalized
 * to lowercase. They cannot be null or empty.</p>
 */
public interface Context extends Comparable<Context> {

    /**
     * Tests whether a key string is valid for use as a context key.
     *
     * <p>A key is valid if it is not null, not empty, and not all whitespace.</p>
     *
     * @param key the key to test
     * @return true if valid
     */
    static boolean isValidKey(@Nullable String key) {
        if (key == null || key.isEmpty())
            return false;

        for (int i = 0, n = key.length(); i < n; i++)
            if (key.charAt(i) != ' ')
                return true;

        return false;
    }

    /**
     * Tests whether a value string is valid for use as a context value.
     *
     * @param value the value to test
     * @return true if valid
     */
    static boolean isValidValue(@Nullable String value) {
        return isValidKey(value);
    }

    /**
     * Gets the context key.
     *
     * @return the key
     */
    @NotNull String getKey();

    /**
     * Gets the context value.
     *
     * @return the value
     */
    @NotNull String getValue();

    @Override
    default int compareTo(@NotNull Context o) {
        int keyCompare = this.getKey().compareTo(o.getKey());
        if (keyCompare != 0)
            return keyCompare;
        return this.getValue().compareTo(o.getValue());
    }

}
