/*
 * Group Mapper
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.mapper;

import me.pectics.kernelclaude.data.entity.GroupEntity;
import org.apache.ibatis.annotations.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/**
 * 权限组 Mapper，对应数据库表 kc_group
 */
@Mapper
public interface GroupMapper {

    @Select("""
    SELECT group_name AS groupName, display_name AS displayName, weight,
           created_at AS createdAt, updated_at AS updatedAt
    FROM kc_group
    WHERE group_name = #{groupName}
    """)
    @Nullable GroupEntity find(@Param("groupName") String groupName);

    @Select("""
    SELECT group_name AS groupName, display_name AS displayName, weight,
           created_at AS createdAt, updated_at AS updatedAt
    FROM kc_group
    ORDER BY weight DESC
    """)
    @NotNull List<GroupEntity> findAll();

    @Select("SELECT group_name FROM kc_group")
    @NotNull Set<String> findAllNames();

    @Insert("""
    INSERT INTO kc_group (group_name, display_name, weight, created_at, updated_at)
    VALUES (#{groupName}, #{displayName}, #{weight}, #{createdAt}, #{updatedAt})
    ON DUPLICATE KEY UPDATE
        display_name = #{displayName},
        weight = #{weight},
        updated_at = #{updatedAt}
    """)
    void save(@NotNull GroupEntity entity);

    @Delete("DELETE FROM kc_group WHERE group_name = #{groupName}")
    int delete(@Param("groupName") String groupName);

    @Select("SELECT COUNT(*) > 0 FROM kc_group WHERE group_name = #{groupName}")
    boolean exists(@Param("groupName") String groupName);
}
