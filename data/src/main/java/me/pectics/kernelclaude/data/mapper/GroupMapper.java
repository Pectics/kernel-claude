package me.pectics.kernelclaude.data.mapper;

import me.pectics.kernelclaude.data.entity.GroupEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 权限组 Mapper
 * <p>
 * 对应数据库表 kc_group
 */
@Mapper
public interface GroupMapper {

    @Select("SELECT * FROM kc_group WHERE group_id = #{groupId}")
    GroupEntity find(@Param("groupId") String groupId);

    @Select("SELECT g.* FROM kc_group g " +
            "INNER JOIN kc_user_group ug ON g.group_id = ug.group_id " +
            "WHERE ug.user_id = #{userId} " +
            "ORDER BY g.weight DESC")
    List<GroupEntity> findByUserId(@Param("userId") String userId);

    @Select("SELECT * FROM kc_group ORDER BY weight DESC")
    List<GroupEntity> findAll();

    @Insert("INSERT INTO kc_group (group_id, display_name, weight, created_at, updated_at) " +
            "VALUES (#{groupId}, #{displayName}, #{weight}, #{createdAt}, #{updatedAt}) " +
            "ON DUPLICATE KEY UPDATE display_name = #{displayName}, weight = #{weight}, updated_at = #{updatedAt}")
    void save(GroupEntity entity);

    @Delete("DELETE FROM kc_group WHERE group_id = #{groupId}")
    int delete(@Param("groupId") String groupId);

    @Select("SELECT COUNT(*) > 0 FROM kc_group WHERE group_id = #{groupId}")
    boolean exists(@Param("groupId") String groupId);
}
