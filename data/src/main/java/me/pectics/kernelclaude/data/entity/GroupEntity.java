package me.pectics.kernelclaude.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 权限组实体
 * <p>
 * 对应数据库表 kc_group
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupEntity {

    /**
     * 权限组 ID（主键）
     */
    private String groupId;

    /**
     * 显示名称
     */
    private String displayName;

    /**
     * 权重
     */
    private Integer weight;

    /**
     * 创建时间（Unix 时间戳）
     */
    private Long createdAt;

    /**
     * 更新时间（Unix 时间戳）
     */
    private Long updatedAt;
}
