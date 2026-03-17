/*
 * User node mapper for MyBatis
 * Licensed under MIT License
 */
package me.pectics.kernelclaude.data.perms.mapper;

import me.pectics.kernelclaude.data.perms.entity.UserNodeEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * MyBatis mapper for UserNodeEntity.
 */
@Mapper
public interface UserNodeMapper {

    /**
     * Finds all nodes for a user.
     *
     * @param userId the user ID
     * @return list of user node entities
     */
    @Select("SELECT id, user_id, node_key, node_value, expire_at, contexts FROM kc_user_nodes WHERE user_id = #{userId}")
    @NotNull List<UserNodeEntity> findByUserId(@Param("userId") @NotNull String userId);

    /**
     * Deletes all nodes for a user.
     *
     * @param userId the user ID
     * @return affected rows
     */
    @Delete("DELETE FROM kc_user_nodes WHERE user_id = #{userId}")
    int deleteByUserId(@Param("userId") @NotNull String userId);

    /**
     * Inserts a new node.
     *
     * @param entity the node entity
     * @return affected rows
     */
    @Insert("INSERT INTO kc_user_nodes (user_id, node_key, node_value, expire_at, contexts) VALUES (#{entity.userId}, #{entity.nodeKey}, #{entity.nodeValue}, #{entity.expireAt}, #{entity.contexts})")
    int insert(@Param("entity") UserNodeEntity entity);

    /**
     * Batch inserts multiple nodes.
     *
     * @param entities the node entities
     */
    @Insert("<script>" +
            "INSERT INTO kc_user_nodes (user_id, node_key, node_value, expire_at, contexts) VALUES " +
            "<foreach collection='entities' item='e' separator=','>" +
            "(#{e.userId}, #{e.nodeKey}, #{e.nodeValue}, #{e.expireAt}, #{e.contexts})" +
            "</foreach>" +
            "</script>")
    void batchInsert(@Param("entities") @NotNull List<UserNodeEntity> entities);

}
