package me.pectics.kernelclaude.data.mapper;

import me.pectics.kernelclaude.data.entity.GroupSuperEntity;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 权限组继承关系 Mapper
 * <p>
 * 对应数据库表 kc_group_super
 */
@Mapper
public interface GroupSuperMapper {

    @Select("SELECT group_id FROM kc_group_super WHERE super_id = #{superId}")
    List<String> findGroupIdsBySuper(@Param("superId") String superId);

    @Select("SELECT super_id FROM kc_group_super WHERE group_id = #{groupId}")
    List<String> findSuperIdsByGroup(@Param("groupId") String groupId);

    @Insert("INSERT IGNORE INTO kc_group_super (group_id, super_id) " +
            "VALUES (#{groupId}, #{superId})")
    int insert(GroupSuperEntity entity);

    @Delete("DELETE FROM kc_group_super " +
            "WHERE group_id = #{groupId} AND super_id = #{superId}")
    int delete(@Param("groupId") String groupId,
               @Param("superId") String superId);

    @Delete("DELETE FROM kc_group_super " +
            "WHERE group_id = #{groupId}")
    int deleteByGroup(@Param("groupId") String groupId);

    @Delete("DELETE FROM kc_group_super " +
            "WHERE super_id = #{superId}")
    int deleteBySuper(@Param("superId") String superId);
}
