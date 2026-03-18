/*
 * Group node mapper for MyBatis
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.perms.mapper;

import me.pectics.kernelclaude.data.perms.entity.GroupNodeEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * MyBatis mapper for GroupNodeEntity.
 */
@Mapper
public interface GroupNodeMapper {

    /**
     * Finds all nodes for a group.
     *
     * @param groupId the group ID
     * @return list of group node entities
     */
    @Select("""
    SELECT id, group_id, node_key, node_value, expire_at, contexts FROM kc_group_nodes
    WHERE group_id = #{groupId}
    """)
    @NotNull List<GroupNodeEntity> findByGroupId(@Param("groupId") @NotNull String groupId);

    /**
     * Deletes all nodes for a group.
     *
     * @param groupId the group ID
     * @return affected rows
     */
    @Delete("""
    DELETE FROM kc_group_nodes
    WHERE group_id = #{groupId}
    """)
    int deleteByGroupId(@Param("groupId") @NotNull String groupId);

    /**
     * Inserts a new node.
     *
     * @param entity the node entity
     * @return affected rows
     */
    @Insert("""
    INSERT INTO kc_group_nodes
        (group_id, node_key, node_value, expire_at, contexts)
    VALUES
        (#{entity.groupId}, #{entity.nodeKey}, #{entity.nodeValue}, #{entity.expireAt}, #{entity.contexts})
    """)
    int insert(@Param("entity") GroupNodeEntity entity);

    /**
     * Batch inserts multiple nodes.
     *
     * @param entities the node entities
     */
    @Insert("""
    <script>
    INSERT INTO kc_group_nodes
        (group_id, node_key, node_value, expire_at, contexts)
    VALUES
    <foreach collection='entities' item='e' separator=','>
        (#{e.groupId}, #{e.nodeKey}, #{e.nodeValue}, #{e.expireAt}, #{e.contexts})
    </foreach>
    </script>
    """)
    void batchInsert(@Param("entities") @NotNull List<GroupNodeEntity> entities);

}
