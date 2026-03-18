/*
 * Node entity mapper for converting between entities and domain objects
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.perms;

import me.pectics.kernelclaude.data.perms.entity.GroupNodeEntity;
import me.pectics.kernelclaude.data.perms.entity.UserNodeEntity;
import me.pectics.kernelclaude.perms.context.ImmutableContextSet;
import me.pectics.kernelclaude.perms.node.Node;
import me.pectics.kernelclaude.perms.node.types.InheritanceNode;
import me.pectics.kernelclaude.perms.node.types.MetaNode;
import me.pectics.kernelclaude.perms.node.types.PermissionNode;
import me.pectics.kernelclaude.perms.node.types.WeightNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

/**
 * Utility class for mapping between Node entities and domain Node objects.
 */
public final class NodeEntityMapper {

    private static final String GROUP_MARKER = "group.";
    private static final String META_MARKER = "meta.";
    private static final String WEIGHT_MARKER = "weight.";

    private NodeEntityMapper() {
        // Utility class
    }

    /**
     * Converts a UserNodeEntity to a Node.
     *
     * @param entity the entity
     * @return the node, or null if parsing fails
     */
    public static @Nullable Node toNode(@NotNull UserNodeEntity entity) {
        return parseNode(entity.getNodeKey(), entity.getNodeValue(), entity.getExpireAt(), entity.getContexts());
    }

    /**
     * Converts a GroupNodeEntity to a Node.
     *
     * @param entity the entity
     * @return the node, or null if parsing fails
     */
    public static @Nullable Node toNode(@NotNull GroupNodeEntity entity) {
        return parseNode(entity.getNodeKey(), entity.getNodeValue(), entity.getExpireAt(), entity.getContexts());
    }

    /**
     * Converts a Node to a UserNodeEntity.
     *
     * @param userId the user ID
     * @param node  the node
     * @return the entity
     */
    public static @NotNull UserNodeEntity toUserNodeEntity(@NotNull UUID userId, @NotNull Node node) {
        return new UserNodeEntity(
                null,
                userId,
                node.getKey(),
                node.getValue(),
                node.hasExpiry() && node.getExpiry() != null ? node.getExpiry().getEpochSecond() : 0L,
                ContextSetJsonSerializer.serialize(node.getContexts())
        );
    }

    /**
     * Converts a Node to a GroupNodeEntity.
     *
     * @param groupId the group ID
     * @param node   the node
     * @return the entity
     */
    public static @NotNull GroupNodeEntity toGroupNodeEntity(@NotNull String groupId, @NotNull Node node) {
        return new GroupNodeEntity(
                null,
                groupId,
                node.getKey(),
                node.getValue(),
                node.hasExpiry() && node.getExpiry() != null ? node.getExpiry().getEpochSecond() : 0L,
                ContextSetJsonSerializer.serialize(node.getContexts())
        );
    }

    /**
     * Parses a node from database fields.
     *
     * @param key      the node key
     * @param value   the node value
     * @param expireAt the expiry timestamp
     * @param contexts the JSON contexts
     * @return the node, or null if parsing fails
     */
    private static @Nullable Node parseNode(
            @NotNull String key,
            boolean value,
            long expireAt,
            @NotNull String contexts) {

        ImmutableContextSet contextSet = ContextSetJsonSerializer.deserialize(contexts);
        String lowerKey = key.toLowerCase(Locale.ROOT);

        // Check for inheritance node (group.<name>)
        if (lowerKey.startsWith(GROUP_MARKER)) {
            String groupName = lowerKey.substring(GROUP_MARKER.length());
            return InheritanceNode.builder(groupName)
                    .value(value)
                    .expiry(expireAt)
                    .context(contextSet.mutableCopy())
                    .build();
        }

        // Check for weight node (weight.<number>)
        if (lowerKey.startsWith(WEIGHT_MARKER)) {
            try {
                String weightStr = lowerKey.substring(WEIGHT_MARKER.length());
                int weight = Integer.parseInt(weightStr);
                return WeightNode.builder(weight)
                        .value(value)
                        .expiry(expireAt)
                        .context(contextSet.mutableCopy())
                        .build();
            } catch (NumberFormatException e) {
                // Invalid weight format, treat as permission
            }
        }

        // Check for meta node (meta.<key>.<value>)
        if (lowerKey.startsWith(META_MARKER)) {
            String remainder = lowerKey.substring(META_MARKER.length());
            int dotIndex = remainder.indexOf('.');
            if (dotIndex > 0) {
                String metaKey = remainder.substring(0, dotIndex);
                String metaValue = remainder.substring(dotIndex + 1);
                // Unescape the meta value
                metaValue = metaValue.replace("\\.", ".");
                return MetaNode.builder(metaKey, metaValue)
                        .value(value)
                        .expiry(expireAt)
                        .context(contextSet.mutableCopy())
                        .build();
            }
        }

        // Default: permission node
        return PermissionNode.builder(key)
                .value(value)
                .expiry(expireAt)
                .context(contextSet.mutableCopy())
                .build();
    }

}
