/*
 * Based on LuckPerms' AbstractNode implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.node;

import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.ToString;
import me.pectics.kernelclaude.perms.context.ContextSet;
import me.pectics.kernelclaude.perms.context.ImmutableContextSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Abstract base implementation of Node.
 */
@ToString(of = {"key", "value", "expireAt", "contexts"})
public abstract class AbstractNode implements Node {

    /**
     * The character separating parts of a node key.
     */
    public static final char NODE_SEPARATOR = '.';
    public static final String NODE_SEPARATOR_STRING = String.valueOf(NODE_SEPARATOR);

    protected final @Getter String key;
    protected final boolean value;
    protected final long expireAt; // 0L for no expiry, otherwise epoch seconds
    protected final @Getter ImmutableContextSet contexts;

    private final int hashCode;

    protected AbstractNode(String key, boolean value, long expireAt, ImmutableContextSet contexts) {
        this.key = Preconditions.checkNotNull(key, "key").toLowerCase();
        this.value = value;
        this.expireAt = expireAt;
        this.contexts = Preconditions.checkNotNull(contexts, "contexts");
        this.hashCode = calculateHashCode();
    }

    @Override
    public boolean getValue() {
        return value;
    }

    @Override
    public boolean hasExpiry() {
        return this.expireAt != 0L;
    }

    @Override
    public @Nullable Instant getExpiry() {
        return hasExpiry() ? Instant.ofEpochSecond(this.expireAt) : null;
    }

    @Override
    public @Nullable Duration getExpiryDuration() {
        Instant expiry = getExpiry();
        if (expiry == null)
            return null;
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return Duration.between(now, expiry);
    }

    @Override
    public boolean hasExpired() {
        Instant expiry = getExpiry();
        return expiry != null && expiry.isBefore(Instant.now());
    }

    @Override
    public boolean matchesKey(@NotNull String key) {
        String normalizedKey = key.toLowerCase();
        if (this.key.equals(normalizedKey))
            return true;
        if (this.key.equals("**"))
            return true;

        // Recursive wildcard (**) matches all descendants
        if (this.key.endsWith(".**")) {
            String prefix = this.key.substring(0, this.key.length() - 2);
            return normalizedKey.startsWith(prefix);
        }

        // Non-recursive wildcard (*) matches direct children only
        if (this.key.endsWith(".*")) {
            String prefix = this.key.substring(0, this.key.length() - 1);
            if (!normalizedKey.startsWith(prefix))
                return false;
            String remain = normalizedKey.substring(prefix.length());
            // Should not contain any more separators
            return !remain.equals("**") && remain.indexOf(NODE_SEPARATOR) == -1;
        }

        return false;
    }

    @Override
    public boolean appliesInContext(@NotNull ContextSet context) {
        return this.contexts.isEmpty() || this.contexts.isSatisfiedBy(context);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Node other)) return false;
        return equals(other, NodeEqualityPredicate.EXACT);
    }

    @Override
    public boolean equals(@NotNull Node other, @NotNull NodeEqualityPredicate predicate) {
        return predicate.test(this, other);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    private int calculateHashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + this.key.hashCode();
        result = result * PRIME + (this.value ? 79 : 97);
        result = result * PRIME + Long.hashCode(this.expireAt);
        result = result * PRIME + this.contexts.hashCode();
        return result;
    }

}
