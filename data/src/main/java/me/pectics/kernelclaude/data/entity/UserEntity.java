package me.pectics.kernelclaude.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.pectics.kernelclaude.permission.Context;
import me.pectics.kernelclaude.permission.PermissionNode;
import me.pectics.kernelclaude.permission.User;
import me.pectics.kernelclaude.permission.impl.SimpleUser;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 用户实体
 * <p>
 * 对应数据库表 kc_user
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    /**
     * 用户 ID（主键）
     */
    private String userId;

    /**
     * 平台标识
     */
    private String platform;

    /**
     * 原生用户 ID
     */
    private String nativeId;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 创建时间（Unix 时间戳）
     */
    private Long createdAt;

    /**
     * 更新时间（Unix 时间戳）
     */
    private Long updatedAt;

    public User toDomain() {
        // 创建用户对象
        SimpleUser user = new SimpleUser(
                entity.getPlatform(),
                entity.getNativeId(),
                entity.getDisplayName()
        );

        // userId 就是 holderId
        String userId = entity.getUserId();

        // 加载权限节点
        List<PermissionNodeEntity> nodeEntities = permissionNodeMapper.findByHolder(HOLDER_TYPE_USER, userId);
        for (PermissionNodeEntity nodeEntity : nodeEntities) {
            PermissionNode node = new PermissionNode(
                    nodeEntity.getKey(),
                    nodeEntity.getValue(),
                    Context.fromJson(nodeEntity.getContexts()),
                    nodeEntity.getUntil()
            );
            user.addPermissionNode(node);
        }

        // 加载组关联
        List<String> groupIds = userGroupMapper.findGroupIdsByUser(userId);
        for (String groupId : groupIds) {
            user.inherit(groupId);
        }

        return user;
    }

    /**
     * 从 User 对象创建 UserEntity
     *
     * @param user 用户对象
     * @return UserEntity 实例
     */
    public static UserEntity from(@NotNull User user) {
        return UserEntity.builder()
                .userId(user.getId())
                .platform(user.getPlatform())
                .nativeId(user.getNativeId())
                .displayName(user.getDisplayName())
                .updatedAt(System.currentTimeMillis())
                .build();
    }

}
