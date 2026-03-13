/*
 * Group Entity
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 权限组实体，对应数据库表 kc_group
 */
public class GroupEntity {

    private final String groupName;
    private @Nullable String displayName;
    private int weight;
    private long createdAt;
    private long updatedAt;

    public GroupEntity(@NotNull String groupName) {
        this.groupName = groupName;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public GroupEntity(@NotNull String groupName, @Nullable String displayName,
                       int weight, long createdAt, long updatedAt) {
        this.groupName = groupName;
        this.displayName = displayName;
        this.weight = weight;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NotNull
    public String getGroupName() {
        return groupName;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "GroupEntity{" +
                "groupName='" + groupName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", weight=" + weight +
                '}';
    }
}
