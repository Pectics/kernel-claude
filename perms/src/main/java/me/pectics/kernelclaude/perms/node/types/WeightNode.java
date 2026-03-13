/*
 * Based on LuckPerms' WeightNode implementation
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.perms.node.types;

import lombok.Getter;
import me.pectics.kernelclaude.perms.context.ImmutableContextSet;
import me.pectics.kernelclaude.perms.node.AbstractNode;
import me.pectics.kernelclaude.perms.node.AbstractNodeBuilder;
import me.pectics.kernelclaude.perms.node.NodeBuilder;
import me.pectics.kernelclaude.perms.node.NodeType;
import org.jetbrains.annotations.NotNull;

/**
 * A node representing the weight of a group.
 *
 * <p>Key format: {@code weight.<number>}</p>
 *
 * <p>Weight determines the priority of groups when resolving conflicts.
 * Higher weight = higher priority.</p>
 */
public final class WeightNode extends AbstractNode {

    private static final String NODE_MARKER = "weight.";

    /**
     * Creates a new builder.
     *
     * @return a builder
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder with the given weight.
     *
     * @param weight the weight
     * @return a builder
     */
    public static @NotNull Builder builder(int weight) {
        return builder().weight(weight);
    }

    /**
     * Creates a simple weight node.
     *
     * @param weight the weight
     * @return the node
     */
    public static @NotNull WeightNode of(int weight) {
        return builder(weight).build();
    }

    private final @Getter int weight;

    private WeightNode(int weight, boolean value, long expireAt, ImmutableContextSet contexts) {
        super(NODE_MARKER + weight, value, expireAt, contexts);
        this.weight = weight;
    }

    @Override
    public @NotNull NodeType<WeightNode> getType() {
        return NodeType.WEIGHT;
    }

    @Override
    public @NotNull Builder toBuilder() {
        return new Builder(this.weight, this.value, this.expireAt, this.contexts);
    }

    /**
     * Builder for WeightNode.
     */
    public static final class Builder extends AbstractNodeBuilder<WeightNode, Builder> implements NodeBuilder<WeightNode, Builder> {

        private Integer weight;

        private Builder() {
            this.weight = null;
        }

        private Builder(int weight, boolean value, long expireAt, @NotNull ImmutableContextSet context) {
            super(value, expireAt, context.mutableCopy());
            this.weight = weight;
        }

        /**
         * Sets the weight.
         *
         * @param weight the weight
         * @return this builder
         */
        public @NotNull Builder weight(int weight) {
            this.weight = weight;
            return this;
        }

        @Override
        public @NotNull WeightNode build() {
            ensureDefined(this.weight, "weight");
            return new WeightNode(this.weight, this.value, this.expireAt, this.context.immutableCopy());
        }

    }

}
