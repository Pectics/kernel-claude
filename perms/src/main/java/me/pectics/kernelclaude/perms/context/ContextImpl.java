/*
 * Based on LuckPerms' ContextImpl
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.context;

import org.jetbrains.annotations.NotNull;

/**
 * Internal implementation of the Context interface.
 */
final class ContextImpl implements Context {

    private final String key;
    private final String value;
    private final int hashCode;

    ContextImpl(String key, String value) {
        this.key = key;
        this.value = value;
        this.hashCode = key.hashCode() ^ value.hashCode();
    }

    @Override
    public @NotNull String getKey() {
        return this.key;
    }

    @Override
    public @NotNull String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Context that)) return false;
        return this.key.equals(that.getKey()) && this.value.equals(that.getValue());
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Override
    public String toString() {
        return this.key + '=' + this.value;
    }
}
