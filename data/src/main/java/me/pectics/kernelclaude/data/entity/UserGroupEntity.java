package me.pectics.kernelclaude.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户-组关联实体
 * <p>
 * 对应数据库表 kc_user_group
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroupEntity {

    /**
     * 用户 ID（外键，关联 kc_user.user_id）
     */
    private String userId;

    /**
     * 权限组 ID（外键，关联 kc_group.group_id）
     */
    private String groupId;
}
