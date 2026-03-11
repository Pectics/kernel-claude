package me.pectics.kernelclaude.event.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 事件优先级枚举
 * <p>
 * 用于事件队列的优先级排序
 */
@Getter
@AllArgsConstructor
public enum EventPriority {

    /**
     * 关键事件 - 系统级事件（健康检查、关闭信号）
     */
    CRITICAL(1),

    /**
     * 高优先级 - 管理员命令
     */
    HIGH(2),

    /**
     * 普通优先级 - 普通用户消息
     */
    NORMAL(3),

    /**
     * 低优先级 - 定时任务、批量操作
     */
    LOW(4);

    /**
     * 优先级级别，数值越小优先级越高
     */
    private final int level;

}
