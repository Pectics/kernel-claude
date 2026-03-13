/*
 * Based on LuckPerms' MutableContextSet implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.context;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A mutable context set implementation using Guava's synchronized SetMultimap.
 */
public final class MutableContextSet extends AbstractContextSet {

    private final Multimap<String, String> map;

    public MutableContextSet() {
        this.map = Multimaps.synchronizedSetMultimap(HashMultimap.create());
    }

    public static @NotNull MutableContextSet create() {
        return new MutableContextSet();
    }

    public static @NotNull MutableContextSet fromMap(@NotNull Map<String, Set<String>> map) {
        Preconditions.checkNotNull(map, "map");
        MutableContextSet set = new MutableContextSet();
        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            String key = sanitizeKey(entry.getKey());
            for (String value : entry.getValue()) {
                set.map.put(key, sanitizeValue(value));
            }
        }
        return set;
    }

    // ==================== ContextSet Interface Implementation ====================

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public @NotNull ImmutableContextSet immutableCopy() {
        if (this.map.isEmpty()) {
            return ImmutableContextSet.empty();
        }
        ImmutableContextSet.Builder builder = ImmutableContextSet.builder();
        synchronized (this.map) {
            for (Map.Entry<String, String> entry : this.map.entries()) {
                builder.add(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    @Override
    public @NotNull MutableContextSet mutableCopy() {
        MutableContextSet copy = new MutableContextSet();
        synchronized (this.map) {
            for (Map.Entry<String, String> entry : this.map.entries()) {
                copy.map.put(entry.getKey(), entry.getValue());
            }
        }
        return copy;
    }

    @Override
    public @NotNull @Unmodifiable Set<Context> toSet() {
        Set<Context> result = new LinkedHashSet<>();
        synchronized (this.map) {
            for (Map.Entry<String, String> entry : this.map.entries()) {
                result.add(new ContextImpl(entry.getKey(), entry.getValue()));
            }
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public @NotNull @Unmodifiable Map<String, Set<String>> asMap() {
        Map<String, Set<String>> result = new java.util.LinkedHashMap<>();
        synchronized (this.map) {
            for (String key : this.map.keySet()) {
                Collection<String> values = this.map.get(key);
                result.put(key, Collections.unmodifiableSet(new LinkedHashSet<>(values)));
            }
        }
        return Collections.unmodifiableMap(result);
    }

    @Override
    public @NotNull @Unmodifiable Iterator<Context> iterator() {
        return toSet().iterator();
    }

    @Override
    public boolean containsKey(@NotNull String key) {
        Preconditions.checkNotNull(key, "key");
        return this.map.containsKey(sanitizeKey(key));
    }

    @Override
    public @NotNull @Unmodifiable Set<String> getValues(@NotNull String key) {
        Preconditions.checkNotNull(key, "key");
        Collection<String> values = this.map.get(sanitizeKey(key));
        return Collections.unmodifiableSet(new LinkedHashSet<>(values));
    }

    @Override
    public boolean contains(@NotNull String key, @NotNull String value) {
        return this.map.containsEntry(sanitizeKey(key), sanitizeValue(value));
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    @Override
    public int size() {
        return this.map.size();
    }

    // ==================== otherContainsAll - Thread-safe Implementation ====================

    @Override
    protected boolean otherContainsAll(@NotNull ContextSet other, @NotNull ContextSatisfyMode mode) {
        synchronized (this.map) {
            return switch (mode) {
                case ALL_VALUE_MATCH_PER_KEY -> {
                    for (Map.Entry<String, String> entry : this.map.entries()) {
                        if (!other.contains(entry.getKey(), entry.getValue())) {
                            yield false;
                        }
                    }
                    yield true;
                }
                case ANY_VALUE_MATCH_PER_KEY -> {
                    for (Map.Entry<String, Collection<String>> entry : this.map.asMap().entrySet()) {
                        if (!other.containsAny(entry.getKey(), entry.getValue())) {
                            yield false;
                        }
                    }
                    yield true;
                }
            };
        }
    }

    // ==================== Mutation Methods ====================

    public @NotNull MutableContextSet add(@NotNull String key, @NotNull String value) {
        this.map.put(sanitizeKey(key), sanitizeValue(value));
        return this;
    }

    public @NotNull MutableContextSet add(@NotNull Context context) {
        Preconditions.checkNotNull(context, "context");
        return add(context.getKey(), context.getValue());
    }

    public @NotNull MutableContextSet addAll(@NotNull ContextSet other) {
        Preconditions.checkNotNull(other, "other");
        for (Context context : other) {
            add(context);
        }
        return this;
    }

    public boolean remove(@NotNull String key, @NotNull String value) {
        return this.map.remove(sanitizeKey(key), sanitizeValue(value));
    }

    public boolean removeAll(@NotNull String key) {
        Collection<String> removed = this.map.removeAll(sanitizeKey(key));
        return !removed.isEmpty();
    }

    public void clear() {
        this.map.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContextSet that)) return false;
        return this.size() == that.size() && otherContainsAll(that, ContextSatisfyMode.ALL_VALUE_MATCH_PER_KEY);
    }

    @Override
    public int hashCode() {
        return this.map.hashCode();
    }

    @Override
    public String toString() {
        return "MutableContextSet" + this.map;
    }
}
