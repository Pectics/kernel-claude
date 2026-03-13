/*
 * User Mapper
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.mapper;

import me.pectics.kernelclaude.data.entity.UserEntity;
import org.apache.ibatis.annotations.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * 用户 Mapper，对应数据库表 kc_user
 */
@Mapper
public interface UserMapper {

    @Select("""
    SELECT platform, user_id AS userId, native_id AS nativeId, display_name AS displayName,
           primary_group AS primaryGroup, created_at AS createdAt, updated_at AS updatedAt
    FROM kc_user WHERE user_id = #{userId}
    """)
    @Nullable UserEntity findByUniqueId(@Param("userId") String userId);

    @Select("""
    SELECT platform, user_id AS userId, native_id AS nativeId, display_name AS displayName,
           primary_group AS primaryGroup, created_at AS createdAt, updated_at AS updatedAt
    FROM kc_user WHERE platform = #{platform} AND native_id = #{nativeId}
    """)
    @Nullable UserEntity findByPlatformAndNativeId(@Param("platform") String platform,
                                                   @Param("nativeId") String nativeId);

    @Select("SELECT user_id FROM kc_user")
    @NotNull Set<String> findAllIds();

    @Insert("""
    INSERT INTO kc_user (user_id, platform, native_id, display_name, primary_group, created_at, updated_at)
    VALUES (#{userId}, #{platform}, #{nativeId}, #{displayName}, #{primaryGroup}, #{createdAt}, #{updatedAt})
    ON DUPLICATE KEY UPDATE
        display_name = #{displayName},
        native_id = #{nativeId},
        primary_group = #{primaryGroup},
        updated_at = #{updatedAt}
    """)
    void save(@NotNull UserEntity entity);

    @Delete("DELETE FROM kc_user WHERE user_id = #{userId}")
    int delete(@Param("userId") String userId);

    @Select("SELECT COUNT(*) > 0 FROM kc_user WHERE user_id = #{userId}")
    boolean exists(@Param("userId") String userId);
}
