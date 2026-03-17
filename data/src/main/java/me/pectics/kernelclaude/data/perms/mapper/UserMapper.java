/*
 * User mapper for MyBatis
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.perms.mapper;

import me.pectics.kernelclaude.data.perms.entity.UserEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * MyBatis mapper for UserEntity.
 */
@Mapper
public interface UserMapper {

    /**
     * Finds a user by unique ID.
     *
     * @param userId the unique ID
     * @return the user entity, or null if not found
     */
    @Select("SELECT user_id, platform, native_id, primary_group FROM kc_users WHERE user_id = #{userId}")
    @Nullable UserEntity findByUserId(@Param("userId") @NotNull String userId);

    /**
     * Finds a user by platform and native ID.
     *
     * @param platform the platform
     * @param nativeId the native ID
     * @return the user entity, or null if not found
     */
    @Select("SELECT user_id, platform, native_id, primary_group FROM kc_users WHERE platform = #{platform} AND native_id = #{nativeId}")
    @Nullable UserEntity findByPlatformAndNativeId(
            @Param("platform") @NotNull String platform,
            @Param("nativeId") @NotNull String nativeId);

    /**
     * Gets all user IDs.
     *
     * @return list of user IDs
     */
    @Select("SELECT user_id FROM kc_users")
    @NotNull List<String> findAllUserIds();

    /**
     * Inserts a new user.
     *
     * @param entity the user entity
     * @return affected rows
     */
    @Insert("INSERT INTO kc_users (user_id, platform, native_id, primary_group) VALUES (#{entity.userId}, #{entity.platform}, #{entity.nativeId}, #{entity.primaryGroup})")
    int insert(@Param("entity") UserEntity entity);

    /**
     * Updates an existing user.
     *
     * @param entity the user entity
     * @return affected rows
     */
    @Update("UPDATE kc_users SET platform = #{entity.platform}, native_id = #{entity.nativeId}, primary_group = #{entity.primaryGroup} WHERE user_id = #{entity.userId}")
    int update(@Param("entity") UserEntity entity);

    /**
     * Deletes a user by ID.
     *
     * @param userId the unique ID
     * @return affected rows
     */
    @Delete("DELETE FROM kc_users WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") @NotNull String userId);

}
