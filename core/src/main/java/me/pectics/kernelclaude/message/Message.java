package me.pectics.kernelclaude.message;

import java.util.Map;

public record Message(
        String id,
        MessagePriority priority,
        String platform,
        String senderId,
        String sessionId,
        String content,
        long timestamp,
        Map<String, Object> metadata
) implements Comparable<Message> {

    @Override
    public int compareTo(Message other) {
        int cmp = Integer.compare(this.priority.getLevel(), other.priority.getLevel());
        if (cmp != 0) return cmp;
        return Long.compare(this.timestamp, other.timestamp);
    }
}
