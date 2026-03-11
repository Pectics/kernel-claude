package me.pectics.kernelclaude.event.model;

import java.util.Map;
import java.util.UUID;

/**
 * 标准事件模型
 * <p>
 * 所有平台的事件都会被转换为这个统一格式，便于后续处理和路由。
 *
 * @param eventId        事件唯一 ID
 * @param type           事件类型
 * @param priority       事件优先级
 * @param platform       来源平台（telegram, onebot, discord 等）
 * @param platformEventId 平台原始事件 ID
 * @param sessionId      会话 ID（用于事件路由）
 * @param userId         用户 ID
 * @param timestamp      事件时间戳
 * @param data           事件数据（平台特定的扩展数据）
 */
public record KernelEvent(
        String eventId,
        EventType type,
        EventPriority priority,
        String platform,
        String platformEventId,
        String sessionId,
        String userId,
        long timestamp,
        Map<String, Object> data
) {

    /**
     * 创建一个带有自动生成 ID 和时间戳的消息事件
     */
    public static KernelEvent message(String platform, String sessionId, String userId, Map<String, Object> data) {
        return new KernelEvent(
                UUID.randomUUID().toString(),
                EventType.MESSAGE,
                EventPriority.NORMAL,
                platform,
                null,
                sessionId,
                userId,
                System.currentTimeMillis(),
                data
        );
    }

    /**
     * 创建一个系统事件
     */
    public static KernelEvent system(String sessionId, Map<String, Object> data) {
        return new KernelEvent(
                UUID.randomUUID().toString(),
                EventType.SYSTEM,
                EventPriority.CRITICAL,
                "system",
                null,
                sessionId,
                "system",
                System.currentTimeMillis(),
                data
        );
    }

    /**
     * 获取消息内容（便捷方法）
     */
    @SuppressWarnings("unchecked")
    public String getMessageContent() {
        if (data == null) return null;
        Object content = data.get("content");
        return content != null ? content.toString() : null;
    }
}
