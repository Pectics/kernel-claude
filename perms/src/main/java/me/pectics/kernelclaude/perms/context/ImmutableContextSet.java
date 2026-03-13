/*
 * Based on LuckPerms' ImmutableContextSet implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.context;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSetMultimap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * An immutable context set implementation using Guava's ImmutableSetMultimap.
 */
public final class ImmutableContextSet extends AbstractContextSet {

    private static final ImmutableContextSet EMPTY = new ImmutableContextSet(ImmutableSetMultimap.of());

    private final ImmutableSetMultimap<String, String> map;
    private final int size;

    private ImmutableContextSet(@NotNull ImmutableSetMultimap<String, String> map) {
        this.map = map;
        this.size = map.size();
    }

    // ==================== Static Factory Methods ====================

    public static @NotNull ImmutableContextSet empty() {
        return EMPTY;
    }

    public static @NotNull ImmutableContextSet fromMap(@NotNull Map<String, Set<String>> map) {
        Preconditions.checkNotNull(map, "map");
        if (map.isEmpty())
            return EMPTY;

        ImmutableSetMultimap.Builder<String, String> builder = ImmutableSetMultimap.builder();
        for (Map.Entry<String, Set<String>> entry : map.entrySet())
            for (String value : entry.getValue())
                builder.put(entry.getKey(), value);

        return new ImmutableContextSet(builder.build());
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    // ==================== ContextSet Interface Implementation ====================

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public @NotNull ImmutableContextSet immutableCopy() {
        return this;
    }

    @Override
    public @NotNull MutableContextSet mutableCopy() {
        MutableContextSet copy = new MutableContextSet();
        for (Map.Entry<String, String> entry : map.entries())
            copy.add(entry.getKey(), entry.getValue());

        return copy;
    }

    @Override
    public @NotNull @Unmodifiable Set<Context> toSet() {
        Set<Context> result = new LinkedHashSet<>();
        for (Map.Entry<String, String> entry : map.entries())
            result.add(new ContextImpl(entry.getKey(), entry.getValue()));

        return Collections.unmodifiableSet(result);
    }

    @Override
    public @NotNull @Unmodifiable Map<String, Set<String>> asMap() {
        Map<String, Set<String>> result = new LinkedHashMap<>();
        for (String key : map.keySet())
            result.put(key, Collections.unmodifiableSet(map.get(key)));

        return Collections.unmodifiableMap(result);
    }

    @Override
    public @NotNull @Unmodifiable Iterator<Context> iterator() {
        return toSet().iterator();
    }

    @Override
    public boolean containsKey(@NotNull String key) {
        Preconditions.checkNotNull(key, "key");
        return map.containsKey(key.toLowerCase());
    }

    @Override
    public @NotNull @Unmodifiable Set<String> getValues(@NotNull String key) {
        Preconditions.checkNotNull(key, "key");
        Set<String> values = map.get(key.toLowerCase());
        return Collections.unmodifiableSet(values);
    }

    @Override
    public boolean contains(@NotNull String key, @NotNull String value) {
        return map.containsEntry(sanitizeKey(key), sanitizeValue(value));
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public int size() {
        return size;
    }

    // ==================== otherContainsAll Implementation ====================

    @Override
    protected boolean otherContainsAll(@NotNull ContextSet other, @NotNull ContextSatisfyMode mode) {
        return switch (mode) {
            case ALL_VALUE_MATCH_PER_KEY -> {
                for (Map.Entry<String, String> entry : map.entries())
                    if (!other.contains(entry.getKey(), entry.getValue()))
                        yield false;

                yield true;
            }
            case ANY_VALUE_MATCH_PER_KEY -> {
                // Use other.containsAny for efficiency
                for (Map.Entry<String, Set<String>> entry : asMap().entrySet())
                    if (!other.containsAny(entry.getKey(), entry.getValue()))
                        yield false;

                yield true;
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImmutableContextSet that)) return false;
        return this.map.equals(that.map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public String toString() {
        return "ImmutableContextSet" + map;
    }

    // ==================== Builder Inner Class ====================

    public static final class Builder {
        private final ImmutableSetMultimap.Builder<String, String> builder = ImmutableSetMultimap.builder();

        public @NotNull Builder add(@NotNull String key, @NotNull String value) {
            builder.put(sanitizeKey(key), sanitizeValue(value));
            return this;
        }

        public @NotNull Builder add(@NotNull Context context) {
            Preconditions.checkNotNull(context, "context");
            return add(context.getKey(), context.getValue());
        }

        public @NotNull Builder addAll(@NotNull String key, @NotNull Iterable<String> values) {
            Preconditions.checkNotNull(key, "key");
            Preconditions.checkNotNull(values, "values");
            String sanitizedKey = sanitizeKey(key);
            for (String value : values)
                builder.put(sanitizedKey, sanitizeValue(value));

            return this;
        }

        public @NotNull Builder addAll(@NotNull ContextSet other) {
            Preconditions.checkNotNull(other, "other");
            for (Context context : other)
                add(context);

            return this;
        }

        public @NotNull ImmutableContextSet build() {
            return new ImmutableContextSet(builder.build());
        }

    }

}
