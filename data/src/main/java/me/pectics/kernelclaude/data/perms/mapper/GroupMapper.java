/*
 * Group mapper for MyBatis
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.perms.mapper;

import me.pectics.kernelclaude.data.perms.entity.GroupEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * MyBatis mapper for GroupEntity.
 */
@Mapper
public interface GroupMapper {

    /**
     * Finds a group by ID.
     *
     * @param groupId the group ID
     * @return the group entity, or null if not found
     */
    @Select("SELECT group_id FROM kc_groups WHERE group_id = #{groupId}")
    @Nullable GroupEntity findByGroupId(@Param("groupId") @NotNull String groupId);

    /**
     * Gets all group IDs.
     *
     * @return list of group IDs
     */
    @Select("SELECT group_id FROM kc_groups")
    @NotNull List<String> findAllGroupIds();

    /**
     * Inserts a new group.
     *
     * @param entity the group entity
     * @return affected rows
     */
    @Insert("INSERT INTO kc_groups (group_id) VALUES (#{entity.groupId})")
    int insert(@Param("entity") GroupEntity entity);

    /**
     * Deletes a group by ID.
     *
     * @param groupId the group ID
     * @return affected rows
     */
    @Delete("DELETE FROM kc_groups WHERE group_id = #{groupId}")
    int deleteByGroupId(@Param("groupId") @NotNull String groupId);

}
