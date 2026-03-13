/*
 * Based on LuckPerms' InheritanceNode implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.node.types;

import com.google.common.base.Preconditions;
import me.pectics.kernelclaude.perms.context.ImmutableContextSet;
import me.pectics.kernelclaude.perms.node.AbstractNode;
import me.pectics.kernelclaude.perms.node.AbstractNodeBuilder;
import me.pectics.kernelclaude.perms.node.NodeBuilder;
import me.pectics.kernelclaude.perms.node.NodeType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * A node representing group inheritance.
 *
 * <p>Key format: {@code group.<group_name>}</p>
 */
public final class InheritanceNode extends AbstractNode {

    private static final String NODE_MARKER = "group.";

    /**
     * Creates a new builder.
     *
     * @return a builder
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder for the given group.
     *
     * @param groupName the group name
     * @return a builder
     */
    public static @NotNull Builder builder(@NotNull String groupName) {
        return builder().group(groupName);
    }

    /**
     * Creates a simple inheritance node for a group.
     *
     * @param groupName the group name
     * @return the node
     */
    public static @NotNull InheritanceNode of(@NotNull String groupName) {
        return builder(groupName).build();
    }

    /**
     * Parses a key to determine if it's an inheritance node.
     *
     * @param key the key to parse
     * @return a builder pre-populated with the group name, or null if not an inheritance key
     */
    public static @Nullable Builder parse(@NotNull String key) {
        String normalizedKey = key.toLowerCase(Locale.ROOT);
        if (!normalizedKey.startsWith(NODE_MARKER)) {
            return null;
        }
        return builder().group(normalizedKey.substring(NODE_MARKER.length()));
    }

    /**
     * Gets the key format for a group.
     *
     * @param groupName the group name
     * @return the node key
     */
    public static @NotNull String key(@NotNull String groupName) {
        return NODE_MARKER + groupName.toLowerCase(Locale.ROOT);
    }

    private final String groupName;

    private InheritanceNode(String groupName, boolean value, long expireAt, ImmutableContextSet contexts) {
        super(key(groupName), value, expireAt, contexts);
        this.groupName = groupName.toLowerCase(Locale.ROOT);
    }

    @Override
    public @NotNull NodeType<InheritanceNode> getType() {
        return NodeType.INHERITANCE;
    }

    /**
     * Gets the group name.
     *
     * @return the group name
     */
    public @NotNull String getGroupName() {
        return this.groupName;
    }

    @Override
    public @NotNull Builder toBuilder() {
        return new Builder(this.groupName, this.value, this.expireAt, this.contexts);
    }

    /**
     * Builder for InheritanceNode.
     */
    public static final class Builder extends AbstractNodeBuilder<InheritanceNode, Builder> implements NodeBuilder<InheritanceNode, Builder> {

        private String groupName;

        private Builder() {
            this.groupName = null;
        }

        private Builder(String groupName, boolean value, long expireAt, ImmutableContextSet context) {
            super(value, expireAt, context.mutableCopy());
            this.groupName = groupName;
        }

        /**
         * Sets the group name.
         *
         * @param groupName the group name
         * @return this builder
         */
        public @NotNull Builder group(@NotNull String groupName) {
            Preconditions.checkNotNull(groupName, "groupName");
            Preconditions.checkArgument(!groupName.isEmpty(), "Group name cannot be empty");
            this.groupName = groupName;
            return this;
        }

        @Override
        public @NotNull InheritanceNode build() {
            ensureDefined(this.groupName, "groupName");
            return new InheritanceNode(this.groupName, this.value, this.expireAt, this.context.immutableCopy());
        }
    }
}
