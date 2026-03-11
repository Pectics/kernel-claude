package me.pectics.kernelclaude.data.mapper;

import me.pectics.kernelclaude.data.entity.PermissionNodeEntity;
import me.pectics.kernelclaude.permission.PermissionHolder;
import org.apache.ibatis.annotations.*;

import java.util.List;

import static me.pectics.kernelclaude.permission.PermissionHolder.Type.USER;
import static me.pectics.kernelclaude.permission.PermissionHolder.Type.GROUP;

/**
 * 权限节点 Mapper
 */
@Mapper
public interface PermissionNodeMapper {

    @Select("""
    SELECT * FROM kc_permission_node
    WHERE holder_type = #{holderType}
      AND holder_id = #{holderId}
    ORDER BY `key`, `value`, until ASC
    """)
    List<PermissionNodeEntity> findByHolder(@Param("holderType") PermissionHolder.Type holderType,
                                            @Param("holderId") String holderId);

    default List<PermissionNodeEntity> findByUser(@Param("userId") String userId) {
        return findByHolder(USER, userId);
    }

    default List<PermissionNodeEntity> findByGroup(@Param("groupId") String groupId) {
        return findByHolder(GROUP, groupId);
    }

    @Insert("""
    INSERT INTO kc_permission_node
        (holder_type, holder_id, `key`, `value`, contexts, until)
    VALUES
        (#{holderType}, #{holderId}, #{key}, #{value}, #{contexts}, #{until})
    """)
    @Options(useGeneratedKeys = true, keyProperty = "nodeId")
    void insert(PermissionNodeEntity entity);

    @Insert("""
    <script>
    INSERT INTO kc_permission_node
        (holder_type, holder_id, `key`, `value`, contexts, until)
    VALUES
    <foreach item="e" collection="entities" separator=",">
        (#{e.holderType}, #{e.holderId}, #{e.key}, #{e.value}, #{e.contexts}, #{e.until})
    </foreach>
    </script>
    """)
    @Options(useGeneratedKeys = true, keyProperty = "nodeId")
    void insertBatch(@Param("entities") List<PermissionNodeEntity> entities);

    @Delete("""
    DELETE FROM kc_permission_node
    WHERE node_id = #{nodeId}
    """)
    int delete(@Param("nodeId") Long nodeId);

    @Delete("""
    <script>
    DELETE FROM kc_permission_node
    WHERE node_id IN
    <foreach item='nodeId' collection='nodeIds' open='(' separator=',' close=')'>
        #{nodeId}
    </foreach>
    </script>
    """)
    int deleteBatch(@Param("nodeIds") List<Long> nodeIds);

    @Delete("""
    DELETE FROM kc_permission_node
    WHERE holder_type = #{holderType}
      AND holder_id = #{holderId}
    """)
    int deleteAllByHolder(@Param("holderType") PermissionHolder.Type holderType,
                          @Param("holderId") String holderId);

    @Delete("""
    DELETE FROM kc_permission_node
    WHERE holder_type = #{holderType}
      AND holder_id = #{holderId}
      AND `key` = #{key}
    """)
    int deleteAllByHolderAndKey(@Param("holderType") PermissionHolder.Type holderType,
                                @Param("holderId") String holderId,
                                @Param("key") String key);

    default int deleteAllByUser(@Param("userId") String userId) {
        return deleteAllByHolder(USER, userId);
    }

    default int deleteAllByUserAndKey(@Param("userId") String userId,
                                      @Param("key") String key) {
        return deleteAllByHolderAndKey(USER, userId, key);
    }

    default int deleteAllByGroup(@Param("groupId") String groupId) {
        return deleteAllByHolder(GROUP, groupId);
    }

    default int deleteAllByGroupAndKey(@Param("groupId") String groupId,
                                       @Param("key") String key) {
        return deleteAllByHolderAndKey(GROUP, groupId, key);
    }

}
