package me.pectics.kernelclaude.data.mapper;

import me.pectics.kernelclaude.data.entity.UserEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户 Mapper
 * <p>
 * 对应数据库表 kc_user
 */
@Mapper
public interface UserMapper {

    @Select("""
    SELECT * FROM kc_user
    WHERE user_id = #{userId}
    """)
    UserEntity find(@Param("userId") String userId);

    @Select("""
    SELECT * FROM kc_user
    WHERE platform = #{platform}
      AND native_id = #{nativeId}
    """)
    UserEntity findByPlatformAndNativeId(@Param("platform") String platform,
                                         @Param("nativeId") String nativeId);

    @Select("""
    SELECT * FROM kc_user
    WHERE platform = #{platform}
    """)
    List<UserEntity> findByPlatform(@Param("platform") String platform);

    @Select("""
    SELECT u.* FROM kc_user u
    INNER JOIN kc_user_group ug
        ON u.user_id = ug.user_id
    WHERE ug.group_id = #{groupId}
    """)
    List<UserEntity> findByGroup(@Param("groupId") String groupId);

    @Insert("""
    INSERT INTO kc_user
        (user_id, platform, native_id, display_name, created_at, updated_at)
    VALUES
        (#{userId}, #{platform}, #{nativeId}, #{displayName}, #{createdAt}, #{updatedAt})
    ON DUPLICATE KEY
    UPDATE display_name = #{displayName}, updated_at = #{updatedAt}
    """)
    void save(UserEntity entity);

    @Delete("""
    DELETE FROM kc_user
    WHERE user_id = #{userId}
    """)
    int delete(@Param("userId") String userId);

    @Delete("""
    DELETE FROM kc_user
    WHERE platform = #{platform}
      AND native_id = #{nativeId}
    """)
    int deleteByPlatformAndNativeId(@Param("platform") String platform, @Param("nativeId") String nativeId);

    @Select("""
    SELECT COUNT(*) > 0 FROM kc_user
    WHERE user_id = #{userId}
    """)
    boolean exists(@Param("userId") String userId);

    @Select("""
    SELECT COUNT(*) > 0 FROM kc_user
    WHERE platform = #{platform}
      AND native_id = #{nativeId}
    """)
    boolean existsByPlatformAndNativeId(@Param("platform") String platform, @Param("nativeId") String nativeId);
}
