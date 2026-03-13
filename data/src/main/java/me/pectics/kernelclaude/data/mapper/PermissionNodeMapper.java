/*
 * Permission Node Mapper
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.mapper;

import me.pectics.kernelclaude.data.entity.PermissionNodeEntity;
import org.apache.ibatis.annotations.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 权限节点 Mapper，对应数据库表 kc_permission_node
 */
@Mapper
public interface PermissionNodeMapper {

    @Select("""
    SELECT holder_type, holder_id, permission_key, permission_value, contexts, until
    FROM kc_permission_node
    WHERE holder_type = #{holderType} AND holder_id = #{holderId}
    ORDER BY permission_key
    """)
    @Results(id = "permissionNodeResult", value = {
            @Result(property = "holderType", column = "holder_type"),
            @Result(property = "holderId", column = "holder_id"),
            @Result(property = "key", column = "permission_key"),
            @Result(property = "value", column = "permission_value"),
            @Result(property = "contexts", column = "contexts"),
            @Result(property = "expiry", column = "until")
    })
    @NotNull List<PermissionNodeEntity> findByHolder(@Param("holderType") String holderType,
                                                     @Param("holderId") String holderId);

    default @NotNull List<PermissionNodeEntity> findByUser(@NotNull String userId) {
        return findByHolder(PermissionNodeEntity.TYPE_USER, userId);
    }

    default @NotNull List<PermissionNodeEntity> findByGroup(@NotNull String groupName) {
        return findByHolder(PermissionNodeEntity.TYPE_GROUP, groupName);
    }

    @Insert("""
    INSERT INTO kc_permission_node
        (holder_type, holder_id, permission_key, permission_value, contexts, until)
    VALUES
        (#{holderType}, #{holderId}, #{key}, #{value}, #{contexts}, #{expiry})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(@NotNull PermissionNodeEntity entity);

    @Delete("DELETE FROM kc_permission_node WHERE id = #{id}")
    int delete(@Param("id") long id);

    @Delete("""
    DELETE FROM kc_permission_node
    WHERE holder_type = #{holderType} AND holder_id = #{holderId}
    """)
    int deleteByHolder(@Param("holderType") String holderType, @Param("holderId") String holderId);

    default int deleteByUser(@NotNull String userId) {
        return deleteByHolder(PermissionNodeEntity.TYPE_USER, userId);
    }

    default int deleteByGroup(@NotNull String groupName) {
        return deleteByHolder(PermissionNodeEntity.TYPE_GROUP, groupName);
    }
}
