/*
 * Context set JSON serializer for perms module
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.perms;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.pectics.kernelclaude.perms.context.ImmutableContextSet;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Utility class for serializing and deserializing ContextSet to/from JSON.
 */
public final class ContextSetJsonSerializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ContextSetJsonSerializer() {
        // Utility class
    }

    /**
     * Serializes an ImmutableContextSet to a JSON string.
     *
     * @param contexts the context set
     * @return the JSON string
     */
    public static @NotNull String serialize(@NotNull ImmutableContextSet contexts) {
        if (contexts.isEmpty())
            return "{}";

        Map<String, Set<String>> map = contexts.asMap();
        try {
            return MAPPER.writeValueAsString(map);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize context set", e);
        }
    }

    /**
     * Deserializes a JSON string to an ImmutableContextSet.
     *
     * @param json the JSON string
     * @return the context set
     */
    public static @NotNull ImmutableContextSet deserialize(@NotNull String json) {
        if (json == null || json.isBlank() || "{}".equals(json))
            return ImmutableContextSet.empty();

        try {
            Map<String, Set<String>> map = MAPPER.readValue(
                    json,
                    new TypeReference<Map<String, Set<String>>>() {}
            );
            return ImmutableContextSet.fromMap(map);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize context set: " + json, e);
        }
    }

}
