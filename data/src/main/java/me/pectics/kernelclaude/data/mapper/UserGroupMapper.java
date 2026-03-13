package me.pectics.kernelclaude.data.mapper;

import me.pectics.kernelclaude.data.entity.UserGroupEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户-权限组关联 Mapper
 * <p>
 * 对应数据库表 kc_user_group
 */
@Mapper
public interface UserGroupMapper {

    @Select("""
    SELECT user_id FROM kc_user_group
    WHERE group_id = #{groupId}
    """)
    List<String> findUserIdsByGroup(@Param("groupId") String groupId);

    @Select("""
    SELECT group_id FROM kc_user_group
    WHERE user_id = #{userId}
    """)
    List<String> findGroupIdsByUser(@Param("userId") String userId);

    @Insert("""
    INSERT INTO kc_user_group
        (user_id, group_id)
    VALUES
        (#{userId}, #{groupId})
    """)
    int insert(UserGroupEntity entity);

    @Insert("""
    <script>
    INSERT INTO kc_user_group
        (user_id, group_id)
    VALUES
    <foreach item="groupId" collection="groupIds" separator=",">
        (#{userId}, #{groupId})
    </foreach>
    </script>
    """)
    int insertGroupIdsByUser(@Param("userId") String userId,
                             @Param("groupIds") List<String> groupIds);

    @Insert("""
    <script>
    INSERT INTO kc_user_group
        (user_id, group_id)
    VALUES
    <foreach item="userId" collection="userIds" separator=",">
        (#{userId}, #{groupId})
    </foreach>
    </script>
    """)
    int insertUserIdsByGroup(@Param("groupId") String groupId,
                             @Param("userIds") List<String> userIds);

    @Delete("""
    DELETE FROM kc_user_group
    WHERE user_id = #{userId}
      AND group_id = #{groupId}
    """)
    int delete(@Param("userId") String userId,
               @Param("groupId") String groupId);

    @Delete("""
    <script>
    DELETE FROM kc_user_group
    WHERE user_id = #{userId}
      AND group_id IN
    <foreach item="groupId" collection="groupIds" open="(" separator="," close=")">
        #{groupId}
    </foreach>
    </script>
    """)
    int deleteGroupIdsByUser(@Param("userId") String userId,
                             @Param("groupIds") List<String> groupIds);

    @Delete("""
    <script>
    DELETE FROM kc_user_group
    WHERE group_id = #{groupId}
      AND user_id IN
    <foreach item="userId" collection="userIds" open="(" separator="," close=")">
        #{userId}
    </foreach>
    </script>
    """)
    int deleteUserIdsByGroup(@Param("groupId") String groupId,
                             @Param("userIds") List<String> userIds);

    @Delete("""
    DELETE FROM kc_user_group
    WHERE user_id = #{userId}
    """)
    int deleteAllByUser(@Param("userId") String userId);

    @Delete("""
    DELETE FROM kc_user_group
    WHERE group_id = #{groupId}
    """)
    int deleteAllByGroup(@Param("groupId") String groupId);
}
