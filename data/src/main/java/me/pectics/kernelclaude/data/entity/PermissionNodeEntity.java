/*
 * Permission Node Entity
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 权限节点实体，对应数据库表 kc_permission_node
 */
public class PermissionNodeEntity {

    // Holder type constants
    public static final String TYPE_USER = "USER";
    public static final String TYPE_GROUP = "GROUP";

    private @Nullable Long id;
    private final String holderType;
    private final String holderId;
    private final String key;
    private final boolean value;
    private final @Nullable String contexts;
    private final @Nullable Long expiry;

    public PermissionNodeEntity(@NotNull String holderType, @NotNull String holderId,
                                @NotNull String key, boolean value,
                                @Nullable String contexts, @Nullable Long expiry) {
        this.holderType = holderType;
        this.holderId = holderId;
        this.key = key;
        this.value = value;
        this.contexts = contexts;
        this.expiry = expiry;
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }

    @NotNull
    public String getHolderType() {
        return holderType;
    }

    @NotNull
    public String getHolderId() {
        return holderId;
    }

    @NotNull
    public String getKey() {
        return key;
    }

    public boolean getValue() {
        return value;
    }

    @Nullable
    public String getContexts() {
        return contexts;
    }

    @Nullable
    public Long getExpiry() {
        return expiry;
    }

    @Override
    public String toString() {
        return "PermissionNodeEntity{" +
                "id=" + id +
                ", holderType='" + holderType + '\'' +
                ", holderId='" + holderId + '\'' +
                ", key='" + key + '\'' +
                ", value=" + value +
                '}';
    }
}
