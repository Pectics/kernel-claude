/*
 * Based on LuckPerms' PermissionNode implementation
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

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A node representing a permission assignment.
 *
 * <p>Permission keys follow the format: {@code module.action[.subaction]}</p>
 * <p>Examples: {@code telegram.message.send}, {@code discord.channel.read}</p>
 *
 * <p>Wildcard support:</p>
 * <ul>
 *     <li>{@code telegram.*} - matches direct children only</li>
 *     <li>{@code telegram.**} - matches all descendants</li>
 * </ul>
 */
public final class PermissionNode extends AbstractNode {

    private static final Pattern PERMISSION_KEY_PATTERN =
            Pattern.compile("^(?:[a-z0-9_]+\\.)*(?:[a-z0-9_]+|\\*{1,2})?$");

    /**
     * Validates a permission key.
     *
     * @param key the key to validate
     * @return true if valid
     */
    public static boolean isValidKey(@NotNull String key) {
        return PERMISSION_KEY_PATTERN.matcher(key).matches();
    }

    /**
     * Creates a new builder.
     *
     * @return a builder
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder with the given permission key.
     *
     * @param permission the permission key
     * @return a builder
     */
    public static @NotNull Builder builder(@NotNull String permission) {
        return builder().permission(permission);
    }

    /**
     * Creates a simple permission node with the given key and value.
     *
     * @param permission the permission key
     * @param value the value
     * @return the node
     */
    public static @NotNull PermissionNode of(@NotNull String permission, boolean value) {
        return builder(permission).value(value).build();
    }

    private PermissionNode(@NotNull String permission, boolean value, long expireAt, ImmutableContextSet contexts) {
        super(permission.toLowerCase(Locale.ROOT), value, expireAt, contexts);
        if (!isValidKey(this.key))
            throw new IllegalArgumentException("Invalid permission key: " + this.key);
    }

    @Override
    public @NotNull NodeType<PermissionNode> getType() {
        return NodeType.PERMISSION;
    }

    /**
     * Gets the permission string.
     *
     * @return the permission
     */
    public @NotNull String getPermission() {
        return getKey();
    }

    @Override
    public @NotNull Builder toBuilder() {
        return new Builder(this.key, this.value, this.expireAt, this.contexts);
    }

    /**
     * Builder for PermissionNode.
     */
    public static final class Builder extends AbstractNodeBuilder<PermissionNode, Builder> implements NodeBuilder<PermissionNode, Builder> {

        private String permission;

        private Builder() {
            this.permission = null;
        }

        private Builder(String permission, boolean value, long expireAt, @NotNull ImmutableContextSet context) {
            super(value, expireAt, context.mutableCopy());
            this.permission = permission;
        }

        /**
         * Sets the permission key.
         *
         * @param permission the permission
         * @return this builder
         */
        public @NotNull Builder permission(@NotNull String permission) {
            this.permission = Preconditions.checkNotNull(permission, "permission");
            return this;
        }

        @Override
        public @NotNull PermissionNode build() {
            ensureDefined(this.permission, "permission");
            return new PermissionNode(this.permission, this.value, this.expireAt, this.context.immutableCopy());
        }

    }

}
