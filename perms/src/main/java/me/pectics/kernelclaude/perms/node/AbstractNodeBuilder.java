/*
 * Based on LuckPerms' AbstractNodeBuilder implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.node;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.pectics.kernelclaude.perms.context.Context;
import me.pectics.kernelclaude.perms.context.ContextSet;
import me.pectics.kernelclaude.perms.context.MutableContextSet;
import org.jetbrains.annotations.NotNull;

import com.google.common.base.Preconditions;

import java.time.Duration;
import java.time.Instant;

/**
 * Abstract base implementation of NodeBuilder.
 */
@NoArgsConstructor
@AllArgsConstructor
public abstract class AbstractNodeBuilder<N extends Node, B extends NodeBuilder<N, B>> implements NodeBuilder<N, B> {

    protected boolean value = true;
    protected long expireAt = 0L;
    protected MutableContextSet context = MutableContextSet.create();

    @SuppressWarnings("unchecked")
    protected B self() {
        return (B) this;
    }

    @Override
    public @NotNull B value(boolean value) {
        this.value = value;
        return self();
    }

    @Override
    public @NotNull B expiry(@NotNull Instant expiry) {
        Preconditions.checkNotNull(expiry, "expiry");
        this.expireAt = expiry.getEpochSecond();
        return self();
    }

    @Override
    public @NotNull B expiry(@NotNull Duration duration) {
        Preconditions.checkNotNull(duration, "duration");
        this.expireAt = Instant.now().plus(duration).getEpochSecond();
        return self();
    }

    @Override
    public @NotNull B expiry(long epochSeconds) {
        this.expireAt = epochSeconds;
        return self();
    }

    @Override
    public @NotNull B clearExpiry() {
        this.expireAt = 0L;
        return self();
    }

    @Override
    public @NotNull B context(@NotNull ContextSet contexts) {
        Preconditions.checkNotNull(contexts, "contexts");
        this.context = MutableContextSet.create().addAll(contexts);
        return self();
    }

    @Override
    public @NotNull B withContext(@NotNull String key, @NotNull String value) {
        this.context.add(key, value);
        return self();
    }

    @Override
    public @NotNull B withContext(@NotNull Context context) {
        Preconditions.checkNotNull(context, "context");
        this.context.add(context);
        return self();
    }

    /**
     * Ensures a required field is defined, throwing an exception if not.
     *
     * @param value the value to check
     * @param fieldName the field name for the error message
     * @throws IllegalStateException if value is null
     */
    protected void ensureDefined(Object value, String fieldName) {
        Preconditions.checkState(value != null, "%s is not defined", fieldName);
    }

}
