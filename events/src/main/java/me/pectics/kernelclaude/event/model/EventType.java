package me.pectics.kernelclaude.event.model;

/**
 * 事件类型枚举
 * <p>
 * 定义系统中所有可能的事件类型
 */
public enum EventType {

    /**
     * 普通消息事件
     */
    MESSAGE,

    /**
     * 消息编辑事件
     */
    MESSAGE_EDIT,

    /**
     * 消息删除事件
     */
    MESSAGE_DELETE,

    /**
     * 用户加入群组
     */
    USER_JOIN,

    /**
     * 用户离开群组
     */
    USER_LEAVE,

    /**
     * 命令事件（如 /help, /start）
     */
    COMMAND,

    /**
     * 系统内部事件
     */
    SYSTEM
}
