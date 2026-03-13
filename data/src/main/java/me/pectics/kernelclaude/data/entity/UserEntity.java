/*
 * User Entity
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 用户实体，对应数据库表 kc_user
 */
public class UserEntity {

    private final String platform;
    private final String userId;        // 计算得出的唯一 ID
    private final String nativeId;      // 平台原生 ID
    private @Nullable String displayName;
    private @NotNull String primaryGroup = "default";  // 主权限组
    private long createdAt;
    private long updatedAt;

    public UserEntity(@NotNull String platform, @NotNull String userId, @NotNull String nativeId) {
        this.platform = platform;
        this.userId = userId;
        this.nativeId = nativeId;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public UserEntity(@NotNull String platform, @NotNull String userId, @NotNull String nativeId,
                      @Nullable String displayName, @NotNull String primaryGroup,
                      long createdAt, long updatedAt) {
        this.platform = platform;
        this.userId = userId;
        this.nativeId = nativeId;
        this.displayName = displayName;
        this.primaryGroup = primaryGroup;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NotNull
    public String getPlatform() {
        return platform;
    }

    @NotNull
    public String getUserId() {
        return userId;
    }

    @NotNull
    public String getNativeId() {
        return nativeId;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    @NotNull
    public String getPrimaryGroup() {
        return primaryGroup;
    }

    public void setPrimaryGroup(@NotNull String primaryGroup) {
        this.primaryGroup = primaryGroup;
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
        return "UserEntity{" +
                "platform='" + platform + '\'' +
                ", userId='" + userId + '\'' +
                ", nativeId='" + nativeId + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
    }
}
