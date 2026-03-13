package me.pectics.kernelclaude.data.entity;

import lombok.*;
import me.pectics.kernelclaude.permission.Context;
import me.pectics.kernelclaude.permission.PermissionNode;
import org.jetbrains.annotations.NotNull;

/**
 * 权限节点实体
 * <p>
 * 对应数据库表 kc_permission_node
 * <p>
 * 持有者类型可以是 USER 或 GROUP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionNodeEntity {

    /**
     * 节点 ID（主键）
     */
    private Long nodeId;

    /**
     * 持有者类型：USER / GROUP
     */
    private String holderType;

    /**
     * 持有者 ID
     */
    private String holderId;

    /**
     * 权限键
     */
    private String key;

    /**
     * 权限值：true=允许，false=拒绝
     */
    private Boolean value;

    /**
     * 上下文条件（JSON 格式存储）
     * <p>
     * 格式: [{"key":"platform","value":"telegram"}, ...]
     */
    private String contexts;

    /**
     * 过期时间（Unix 时间戳），0 表示永不过期
     */
    private Long until;

    /**
     * 转换为 PermissionNode 对象
     *
     * @return PermissionNode 实例
     */
    public PermissionNode toDomain() {
        return new PermissionNode(key, value, Context.fromJson(contexts), until);
    }

    /**
     * 从 PermissionNode 对象创建 PermissionNodeEntity
     *
     * @param node 权限节点对象
     * @return PermissionNodeEntity 实例
     */
    public static PermissionNodeEntity from(@NotNull PermissionNode node) {
        return PermissionNodeEntity.builder()
                .key(node.key())
                .value(node.value())
                .contexts(Context.toJson(node.contexts()))
                .until(node.until())
                .build();
    }

}
