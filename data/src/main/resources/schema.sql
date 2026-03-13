-- KernelClaude 权限管理系统数据库表结构
-- 数据库前缀: kc_

-- =====================================================
-- 用户表
-- =====================================================
CREATE TABLE IF NOT EXISTS `kc_user` (
    `platform` VARCHAR(64) NOT NULL COMMENT '平台标识',
    `user_id` VARCHAR(128) NOT NULL COMMENT '用户唯一ID（计算值，如 telegram-a1b2c3d4）',
    `native_id` VARCHAR(128) NOT NULL COMMENT '平台原生ID',
    `display_name` VARCHAR(256) DEFAULT NULL COMMENT '显示名称/用户名',
    `primary_group` VARCHAR(64) NOT NULL DEFAULT 'default' COMMENT '主权限组',
    `created_at` BIGINT NOT NULL COMMENT '创建时间（Unix时间戳）',
    `updated_at` BIGINT NOT NULL COMMENT '更新时间（Unix时间戳）',
    PRIMARY KEY (`user_id`),
    UNIQUE INDEX `idx_platform_native` (`platform`, `native_id`),
    INDEX `idx_platform` (`platform`),
    INDEX `idx_primary_group` (`primary_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =====================================================
-- 权限组表
-- =====================================================
CREATE TABLE IF NOT EXISTS `kc_group` (
    `group_name` VARCHAR(64) NOT NULL COMMENT '组名（主键）',
    `display_name` VARCHAR(256) DEFAULT NULL COMMENT '显示名称',
    `weight` INT NOT NULL DEFAULT 0 COMMENT '权重（越高优先级越高）',
    `created_at` BIGINT NOT NULL COMMENT '创建时间（Unix时间戳）',
    `updated_at` BIGINT NOT NULL COMMENT '更新时间（Unix时间戳）',
    PRIMARY KEY (`group_name`),
    INDEX `idx_weight` (`weight` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限组表';

-- =====================================================
-- 权限节点表
-- =====================================================
CREATE TABLE IF NOT EXISTS `kc_permission_node` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `holder_type` VARCHAR(16) NOT NULL COMMENT '持有者类型：USER / GROUP',
    `holder_id` VARCHAR(192) NOT NULL COMMENT '持有者ID（USER: uniqueId, GROUP: groupName）',
    `permission_key` VARCHAR(256) NOT NULL COMMENT '权限键',
    `permission_value` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '权限值：1=允许，0=拒绝',
    `contexts` TEXT DEFAULT NULL COMMENT '上下文条件（JSON格式）',
    `until` BIGINT DEFAULT NULL COMMENT '过期时间（Unix时间戳），NULL表示永不过期',
    PRIMARY KEY (`id`),
    INDEX `idx_holder` (`holder_type`, `holder_id`),
    INDEX `idx_permission_key` (`permission_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限节点表';

-- =====================================================
-- 用户-组关联表
-- =====================================================
CREATE TABLE IF NOT EXISTS `kc_user_group` (
    `user_id` VARCHAR(128) NOT NULL COMMENT '用户唯一ID',
    `group_name` VARCHAR(64) NOT NULL COMMENT '组名',
    PRIMARY KEY (`user_id`, `group_name`),
    INDEX `idx_group` (`group_name`),
    CONSTRAINT `fk_user_group_user` FOREIGN KEY (`user_id`)
        REFERENCES `kc_user` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_group_group` FOREIGN KEY (`group_name`)
        REFERENCES `kc_group` (`group_name`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-组关联表';

-- =====================================================
-- 组继承关系表
-- =====================================================
CREATE TABLE IF NOT EXISTS `kc_group_inheritance` (
    `child_group` VARCHAR(64) NOT NULL COMMENT '子组名',
    `parent_group` VARCHAR(64) NOT NULL COMMENT '父组名',
    PRIMARY KEY (`child_group`, `parent_group`),
    INDEX `idx_parent` (`parent_group`),
    CONSTRAINT `fk_inheritance_child` FOREIGN KEY (`child_group`)
        REFERENCES `kc_group` (`group_name`) ON DELETE CASCADE,
    CONSTRAINT `fk_inheritance_parent` FOREIGN KEY (`parent_group`)
        REFERENCES `kc_group` (`group_name`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='组继承关系表';

-- =====================================================
-- 默认数据
-- =====================================================

-- 插入默认管理员组
INSERT INTO `kc_group` (`group_name`, `display_name`, `weight`, `created_at`, `updated_at`)
VALUES ('admin', '管理员', 100, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000)
ON DUPLICATE KEY UPDATE `display_name` = VALUES(`display_name`);

-- 插入默认用户组
INSERT INTO `kc_group` (`group_name`, `display_name`, `weight`, `created_at`, `updated_at`)
VALUES ('default', '默认组', 0, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000)
ON DUPLICATE KEY UPDATE `display_name` = VALUES(`display_name`);
