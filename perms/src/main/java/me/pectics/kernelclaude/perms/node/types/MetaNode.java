/*
 * Based on LuckPerms' MetaNode implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.node.types;

import com.google.common.base.Preconditions;
import lombok.Getter;
import me.pectics.kernelclaude.perms.context.ImmutableContextSet;
import me.pectics.kernelclaude.perms.node.AbstractNode;
import me.pectics.kernelclaude.perms.node.AbstractNodeBuilder;
import me.pectics.kernelclaude.perms.node.NodeBuilder;
import me.pectics.kernelclaude.perms.node.NodeType;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * A node representing a metadata key-value pair.
 *
 * <p>Key format: {@code meta.<key>.<value>}</p>
 *
 * <p>Meta nodes are used to store arbitrary key-value data on users and groups.</p>
 */
public final class MetaNode extends AbstractNode {

    private static final String NODE_MARKER = "meta.";

    /**
     * Creates a new builder.
     *
     * @return a builder
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder with the given key-value pair.
     *
     * @param key the meta key
     * @param value the meta value
     * @return a builder
     */
    public static @NotNull Builder builder(@NotNull String key, @NotNull String value) {
        return builder().key(key).metaValue(value);
    }

    /**
     * Creates a simple meta node.
     *
     * @param key the meta key
     * @param value the meta value
     * @return the node
     */
    public static @NotNull MetaNode of(@NotNull String key, @NotNull String value) {
        return builder(key, value).build();
    }

    /**
     * Gets the key format for a meta entry.
     *
     * @param metaKey the meta key
     * @param metaValue the meta value
     * @return the node key
     */
    public static @NotNull String key(@NotNull String metaKey, @NotNull String metaValue) {
        return NODE_MARKER + metaKey.toLowerCase(Locale.ROOT) + "." + escapeValue(metaValue);
    }

    private static @NotNull String escapeValue(@NotNull String value) {
        // Simple escaping - replace dots with a placeholder
        // In a full implementation, you might want more sophisticated escaping
        return value.replace(".", "\\.");
    }

    private final @Getter String metaKey;
    private final @Getter String metaValue;

    private MetaNode(String metaKey, String metaValue, boolean value, long expireAt, ImmutableContextSet contexts) {
        super(key(metaKey, metaValue), value, expireAt, contexts);
        this.metaKey = metaKey.toLowerCase(Locale.ROOT);
        this.metaValue = metaValue;
    }

    @Override
    public @NotNull NodeType<MetaNode> getType() {
        return NodeType.META;
    }

    @Override
    public @NotNull Builder toBuilder() {
        return new Builder(this.metaKey, this.metaValue, this.value, this.expireAt, this.contexts);
    }

    /**
     * Builder for MetaNode.
     */
    public static final class Builder extends AbstractNodeBuilder<MetaNode, Builder> implements NodeBuilder<MetaNode, Builder> {

        private String metaKey;
        private String metaValue;

        private Builder() {
            this.metaKey = null;
            this.metaValue = null;
        }

        private Builder(String metaKey, String metaValue, boolean value, long expireAt, @NotNull ImmutableContextSet context) {
            super(value, expireAt, context.mutableCopy());
            this.metaKey = metaKey;
            this.metaValue = metaValue;
        }

        /**
         * Sets the meta key.
         *
         * @param metaKey the meta key
         * @return this builder
         */
        public @NotNull Builder key(@NotNull String metaKey) {
            Preconditions.checkNotNull(metaKey, "metaKey");
            Preconditions.checkArgument(!metaKey.isEmpty(), "Meta key cannot be empty");
            this.metaKey = metaKey;
            return this;
        }

        /**
         * Sets the meta value.
         *
         * @param metaValue the meta value
         * @return this builder
         */
        public @NotNull Builder metaValue(@NotNull String metaValue) {
            Preconditions.checkNotNull(metaValue, "metaValue");
            this.metaValue = metaValue;
            return this;
        }

        // Override value() to avoid confusion with metaValue
        @Override
        public @NotNull Builder value(boolean nodeValue) {
            return super.value(nodeValue);
        }

        @Override
        public @NotNull MetaNode build() {
            ensureDefined(this.metaKey, "metaKey");
            ensureDefined(this.metaValue, "metaValue");
            return new MetaNode(this.metaKey, this.metaValue, this.value, this.expireAt, this.context.immutableCopy());
        }

    }

}
