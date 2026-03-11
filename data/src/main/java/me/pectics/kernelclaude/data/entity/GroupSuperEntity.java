package me.pectics.kernelclaude.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 组继承关系实体
 * <p>
 * 对应数据库表 kc_group_super
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupSuperEntity {

    /**
     * 权限组 ID（外键，关联 kc_group.group_id）
     */
    private String groupId;

    /**
     * 继承的权限组 ID（外键，关联 kc_group.group_id）
     */
    private String superId;
}
